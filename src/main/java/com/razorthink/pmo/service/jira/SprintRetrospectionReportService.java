package com.razorthink.pmo.service.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Worklog;
import com.atlassian.util.concurrent.Promise;
import com.razorthink.pmo.bean.reports.*;
import com.razorthink.pmo.commons.exceptions.DataException;
import com.razorthink.pmo.utils.ConvertToCSV;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.greenhopper.SprintIssue;
import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SprintRetrospectionReportService {

    private static final Logger logger = LoggerFactory.getLogger(SprintRetrospectionReportService.class);

    @Autowired
    private Environment env;

    @Autowired
    private IncompletedIssuesService incompletedIssuesService;

    @Autowired
    private ProjectClients projectClients;


    /**
     * Generates a Sprint Retrospection report of the sprint specified in the argument
     *
     * @param params sprint name, subproject name, projectUrlID
     * @return Complete url of the sprint Retrospection report generated
     * @throws DataException If some internal error occurs
     */
    public GenericReportResponse getSprintRetrospectionReport(BasicReportRequestParams params) {
        logger.debug("getSprintRetrospectionReport");

        JiraClientsPOJO jiraClientsPOJO = projectClients.getJiraClientForProjectUrlId(params.getProjectUrlId());

        String project = params.getSubProjectName();
        String sprint = params.getSprintName();
        List<SprintRetrospection> sprintRetrospectionReport = processSprintRetrospectionRelatedIssues(jiraClientsPOJO.getJqlClient(), jiraClientsPOJO.getJiraClient(), project, sprint);

        String filename = project + "_" + sprint + "_retrospection_report.csv";
        filename = filename.replace(" ", "_");
        ConvertToCSV exportToCSV = new ConvertToCSV();
        exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, sprintRetrospectionReport);

        GenericReportResponse response = new GenericReportResponse();
        response.setDownloadLink(env.getProperty("csv.aliaspath") + filename);
        response.setReportAsJson(sprintRetrospectionReport);
        return response;
    }

    private List<SprintRetrospection> processSprintRetrospectionRelatedIssues(JiraRestClient restClient, JiraClient jiraClient, String project, String sprint) {
        int rvId = 0;
        int sprintId = 0;
        Double actualHours = 0.0;
        Double estimatedHours = 0.0;
        Integer totalTasks = 0;
        Integer incompletedTasks = 0;
        Double availableHours = 0.0;
        Double surplus = 0.0;
        DateTime startDt = null;
        DateTime endDt = null;
        DateTime tempDate = null;
        DateTime completeDate = null;
        String timezone = null;
        String jql = null;
        List<SprintRetrospection> sprintRetrospectionReport = new ArrayList<>();
        List<String> incompleteIssueKeys = new ArrayList<>();

        Set<String> assignee = new TreeSet<>();
        if (project == null || sprint == null) {
            logger.error("Error: Missing required paramaters");
            throw new DataException(HttpStatus.BAD_REQUEST.toString(), "Missing required paramaters");
        }
        Iterable<Issue> retrievedIssue = restClient.getSearchClient().searchJql(" sprint = '" + sprint
                + "' AND project = '" + project + "' AND assignee is not EMPTY ORDER BY assignee", 1000, 0, null)
                .claim().getIssues();
        Pattern pattern = Pattern.compile(
                "\\[\".*\\[id=(.*),rapidViewId=(.*),.*,name=(.*),goal=.*,startDate=(.*),endDate=(.*),completeDate=(.*),.*\\]");
        Matcher matcher = pattern
                .matcher(retrievedIssue.iterator().next().getFieldByName("Sprint").getValue().toString());
        while (matcher.find()) {
            if (matcher.group(3).equals(sprint)) {
                timezone = matcher.group(4).substring(23);
                logger.info(timezone);
                startDt = new DateTime(matcher.group(4), DateTimeZone.forID(ZoneId.of(timezone).toString()));
                endDt = new DateTime(matcher.group(5), DateTimeZone.forID(ZoneId.of(timezone).toString()));
                if (!matcher.group(6).equals("<null>")) {
                    completeDate = new DateTime(matcher.group(6), DateTimeZone.forID(ZoneId.of(timezone).toString()));
                }
                sprintId = Integer.parseInt(matcher.group(1));
                rvId = Integer.parseInt(matcher.group(2));
            }
        }
        processIncompletedIssues(jiraClient, rvId, sprintId, incompleteIssueKeys);
        processRetrievedIssues(restClient, project, sprint, startDt, endDt, completeDate, timezone, sprintRetrospectionReport, incompleteIssueKeys, assignee, retrievedIssue);
        return sprintRetrospectionReport;
    }

    private void processIncompletedIssues(JiraClient jiraClient, int rvId, int sprintId, List<String> incompleteIssueKeys) {
        try {
            IncompletedIssues incompletedIssues = incompletedIssuesService.get(jiraClient.getRestClient(), rvId, sprintId);
            for (SprintIssue issueValue : incompletedIssues.getIncompleteIssues()) {
                incompleteIssueKeys.add(issueValue.getKey());
            }
        } catch (JiraException e) {
            logger.error("Error:" + e.getMessage());
            throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
        }
    }

    private void processRetrievedIssues(JiraRestClient restClient, String project, String sprint, DateTime startDt, DateTime endDt, DateTime completeDate, String timezone, List<SprintRetrospection> sprintRetrospectionReport, List<String> incompleteIssueKeys, Set<String> assignee, Iterable<Issue> retrievedIssue) {
        Double availableHours,actualHours,estimatedHours,surplus;
        String jql;
        Integer totalTasks, incompletedTasks;
        DateTime tempDate;

        for (Issue issueValue : retrievedIssue) {
            availableHours = 0.0;
            if (!assignee.contains(issueValue.getAssignee().getDisplayName())) {
                jql = " sprint = '" + sprint
                        + "' AND project = '" + project + "' AND timespent > 0 AND assignee is not EMPTY ORDER BY assignee";
                Iterable<Issue> assigneeIssue = restClient.getSearchClient().searchJql(jql, 1000, 0, null).claim()
                        .getIssues();
                SprintRetrospection sprintRetrospection = new SprintRetrospection();
                actualHours = 0.0;
                estimatedHours = 0.0;
                totalTasks = 0;
                incompletedTasks = 0;
                for (Issue assigneeIssueValue : assigneeIssue) {
                    Promise<Issue> issue = restClient.getIssueClient().getIssue(assigneeIssueValue.getKey());
                    try {
                        if (issue.get().getTimeTracking() != null) {
                            if (issue.get().getTimeTracking().getOriginalEstimateMinutes() != null) {
                                if (issueValue.getAssignee().getName()
                                        .equals(assigneeIssueValue.getAssignee().getName())) {
                                    estimatedHours += issue.get().getTimeTracking().getOriginalEstimateMinutes();
                                }
                            }
                            if (issue.get().getTimeTracking().getTimeSpentMinutes() != null) {
                                Iterable<Worklog> worklogList = issue.get().getWorklogs();
                                for (Worklog worklog : worklogList) {
                                    if ((worklog.getUpdateDate().compareTo(startDt) >= 0 && ((completeDate != null
                                            && (worklog.getUpdateDate().compareTo(completeDate) <= 0))
                                            || completeDate == null
                                            && ((worklog.getUpdateDate().compareTo(endDt) <= 0))))
                                            && worklog.getUpdateAuthor().getName()
                                            .equals(issueValue.getAssignee().getName())) {
                                        actualHours += worklog.getMinutesSpent();
                                    }
                                }
                            }
                        }
                        if (incompleteIssueKeys.contains(issue.get().getKey())) {
                            incompletedTasks++;
                        }
                        totalTasks++;
                    } catch (InterruptedException | ExecutionException e) {
                        logger.error("Error:" + e.getMessage());
                        throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
                    }
                }
                tempDate = new DateTime(startDt.getMillis(), DateTimeZone.forID(ZoneId.of(timezone).toString()));
                while (tempDate.compareTo(endDt) <= 0) {
                    if (tempDate.getDayOfWeek() != DateTimeConstants.SATURDAY
                            && tempDate.getDayOfWeek() != DateTimeConstants.SUNDAY) {
                        availableHours += 1;
                    }
                    tempDate = tempDate.plusDays(1);

                }
                availableHours *= 8D;
                estimatedHours /= 60D;
                actualHours /= 60D;

                surplus = availableHours - estimatedHours;
                sprintRetrospection.setAssignee(issueValue.getAssignee().getDisplayName());
                sprintRetrospection
                        .setEstimatedHours(Double.parseDouble(new DecimalFormat("##.##").format(estimatedHours)));
                sprintRetrospection.setTimeTaken(Double.parseDouble(new DecimalFormat("##.##").format(actualHours)));
                sprintRetrospection.setAvailableHours(availableHours);
                sprintRetrospection.setSurplus(surplus);
                sprintRetrospection.setBuffer(
                        Double.parseDouble(new DecimalFormat("##.##").format((surplus / availableHours) * 100)));
                if (actualHours != 0) {
                    sprintRetrospection.setEfficiency(Double.parseDouble(new DecimalFormat("##.##")
                            .format(100 + ((estimatedHours - actualHours) / actualHours * 100))));
                } else {
                    sprintRetrospection.setEfficiency(0D);
                }
                sprintRetrospection.setTotalTasks(totalTasks);
                sprintRetrospection.setIncompletedIssues(incompletedTasks);
                sprintRetrospectionReport.add(sprintRetrospection);
                assignee.add(issueValue.getAssignee().getDisplayName());
            }
        }
    }
}
