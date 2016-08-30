package com.razorthink.pmo.service.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.util.concurrent.Promise;
import com.razorthink.pmo.bean.reports.*;
import com.razorthink.pmo.commons.exceptions.DataException;
import com.razorthink.pmo.utils.ConvertToCSV;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.greenhopper.SprintIssue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SprintReportTimeGainedService {

    @Autowired
    private Environment env;

    @Autowired
    private RemovedIssuesService removedIssuesService;

    @Autowired
    private ProjectClients projectClients;

    private static final Logger logger = LoggerFactory.getLogger(SprintReportTimeGainedService.class);

    /**
     * Generates a minimal report of the sprint specified in the argument including
     * issues removed from sprint and issues added during sprint
     *
     * @param params     sprint name, subproject name and projecturlID
     * @return Complete url of the minimal sprint report generated
     * @throws DataException If some internal error occurs
     */
    public GenericReportResponse getMininmalSprintReportTimeGained(BasicReportRequestParams params) {
        logger.debug("getMininmalSprintReport");

        JiraClientsPOJO jiraClientsPOJO = projectClients.getJiraClientForProjectUrlId(params.getProjectUrlId());

        String sprint = params.getSprintName();
        String project = params.getSubProjectName();
        Integer maxResults = 1000;
        Integer startAt = 0;
        int rvId = 0;
        int sprintId = 0;
        if (project == null || sprint == null) {
            logger.error("Error: Missing required paramaters");
            throw new DataException(HttpStatus.BAD_REQUEST.toString(), "Missing required paramaters");
        }
        List<SprintReportTimeGained> sprintReportList = new ArrayList<>();
        SprintReportTimeGained sprintReport;
        Iterable<Issue> retrievedIssue = jiraClientsPOJO.getJqlClient().getSearchClient()
                .searchJql(" sprint = '" + sprint + "' AND project = '" + project + "'", 1000, 0, null).claim()
                .getIssues();
        Pattern pattern = Pattern.compile("\\[\".*\\[id=(.*),rapidViewId=(.*),.*,name=(.*),startDate=(.*),.*\\]");
        Matcher matcher = pattern
                .matcher(retrievedIssue.iterator().next().getFieldByName("Sprint").getValue().toString());
        if (matcher.find()) {
            sprintId = Integer.parseInt(matcher.group(1));
            rvId = Integer.parseInt(matcher.group(2));
        }
        processRetrievedIssues(jiraClientsPOJO.getJqlClient(), sprint, project, maxResults, startAt, sprintReportList, retrievedIssue);
        String headerString = "Removed Issues";
        appendHeaderToReport(sprintReportList, headerString);
        processRemovedIssues(jiraClientsPOJO.getJqlClient(), jiraClientsPOJO.getJiraClient(), rvId, sprintId, sprintReportList);
        String filename = project + "_" + sprint + "_minimal_report_time_gained.csv";
        filename = filename.replace(" ", "_");
        ConvertToCSV exportToCSV = new ConvertToCSV();
        exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, sprintReportList);
        GenericReportResponse response = new GenericReportResponse();
        response.setDownloadLink(env.getProperty("csv.aliaspath") + filename);
        response.setReportAsJson(sprintReportList);
        return response;
    }

    private void processRemovedIssues(JiraRestClient restClient, JiraClient jiraClient, int rvId, int sprintId, List<SprintReportTimeGained> sprintReportList) {
        SprintReportTimeGained sprintReport;
        String headerString;
        try {
            RemovedIssues removedIssues = removedIssuesService.get(jiraClient.getRestClient(), rvId, sprintId);
            processPuntedIssues(restClient, sprintReportList, removedIssues);
            headerString = "Issues Added during Sprint";
            appendHeaderToReport(sprintReportList, headerString);
            processIssuesAdded(restClient, sprintReportList, removedIssues);
        } catch (JiraException e) {
            logger.error("Error:" + e.getMessage());
            throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
        }
    }

    private void processIssuesAdded(JiraRestClient restClient, List<SprintReportTimeGained> sprintReportList, RemovedIssues removedIssues) {
        SprintReportTimeGained sprintReport;
        for (String issueValue : removedIssues.getIssuesAdded()) {
            Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue);
            sprintReport = new SprintReportTimeGained();
            try {

                sprintReport.setIssueKey(issue.get().getKey());
                sprintReport.setIssueType(issue.get().getIssueType().getName());
                sprintReport.setStatus(issue.get().getStatus().getName());
                sprintReport.setIssueSummary(issue.get().getSummary());
                if (issue.get().getAssignee() != null) {
                    sprintReport.setAssignee(issue.get().getAssignee().getDisplayName());
                } else {
                    sprintReport.setAssignee("unassigned");
                }
                if (issue.get().getTimeTracking() != null) {
                    Integer estimatedMinutes = issue.get().getTimeTracking().getOriginalEstimateMinutes();
                    Integer loggedMinutes = issue.get().getTimeTracking().getTimeSpentMinutes();

                    Double gainedHours = getGainedHours(estimatedMinutes, loggedMinutes,issue.get().getStatus().getName());
                    if(gainedHours==null)
                        continue;
                    sprintReport.setTimeSaved(new DecimalFormat("##.##").format(gainedHours));
                }
                sprintReportList.add(sprintReport);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error:" + e.getMessage());
                throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
            }
        }
    }

    private void processPuntedIssues(JiraRestClient restClient, List<SprintReportTimeGained> sprintReportList, RemovedIssues removedIssues) {
        SprintReportTimeGained sprintReport;
        for (SprintIssue issueValue : removedIssues.getPuntedIssues()) {
            Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
            sprintReport = new SprintReportTimeGained();
            try {
                sprintReport.setIssueKey(issue.get().getKey());
                sprintReport.setIssueType(issue.get().getIssueType().getName());
                sprintReport.setStatus(issue.get().getStatus().getName());
                sprintReport.setIssueSummary(issue.get().getSummary());
                if (issue.get().getAssignee() != null) {
                    sprintReport.setAssignee(issue.get().getAssignee().getDisplayName());
                } else {
                    sprintReport.setAssignee("unassigned");
                }
                if (issue.get().getTimeTracking() != null) {

                    Integer estimatedMinutes = issue.get().getTimeTracking().getOriginalEstimateMinutes();
                    Integer loggedMinutes = issue.get().getTimeTracking().getTimeSpentMinutes();

                    Double gainedHours = getGainedHours(estimatedMinutes, loggedMinutes,issue.get().getStatus().getName());
                    if(gainedHours==null)
                        continue;
                    sprintReport.setTimeSaved(new DecimalFormat("##.##").format(gainedHours));
                }
                sprintReportList.add(sprintReport);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error:" + e.getMessage());
                throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
            }
        }
    }

    private void appendHeaderToReport(List<SprintReportTimeGained> sprintReportList, String headerString) {
        SprintReportTimeGained sprintReport;
        for (int i = 0; i < 2; i++) {
            sprintReport = new SprintReportTimeGained();
            sprintReport.setAssignee(" ");
            sprintReport.setIssueKey(" ");
            sprintReport.setIssueSummary(" ");
            sprintReport.setIssueType(" ");
            sprintReport.setTimeSaved(" ");
            sprintReport.setStatus(" ");
            sprintReportList.add(sprintReport);
        }
        sprintReport = new SprintReportTimeGained();
        sprintReport.setAssignee(" ");
        sprintReport.setIssueSummary(" ");
        sprintReport.setIssueType(" ");
        sprintReport.setTimeSaved(" ");
        sprintReport.setStatus(" ");
        sprintReport.setIssueKey(headerString);
        sprintReportList.add(sprintReport);
        sprintReport = new SprintReportTimeGained();
        sprintReport.setAssignee(" ");
        sprintReport.setIssueKey(" ");
        sprintReport.setIssueSummary(" ");
        sprintReport.setIssueType(" ");
        sprintReport.setTimeSaved(" ");
        sprintReport.setStatus(" ");
        sprintReportList.add(sprintReport);
    }

    private void processRetrievedIssues(JiraRestClient restClient, String sprint, String project, Integer maxResults, Integer startAt, List<SprintReportTimeGained> sprintReportList, Iterable<Issue> retrievedIssue) {
        SprintReportTimeGained sprintReport;
        while (retrievedIssue.iterator().hasNext()) {
            for (Issue issueValue : retrievedIssue) {
                Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
                sprintReport = new SprintReportTimeGained();
                try {
                    sprintReport.setIssueKey(issue.get().getKey());
                    sprintReport.setIssueType(issue.get().getIssueType().getName());
                    sprintReport.setStatus(issue.get().getStatus().getName());
                    sprintReport.setIssueSummary(issue.get().getSummary());
                    if (issue.get().getAssignee() != null) {
                        sprintReport.setAssignee(issue.get().getAssignee().getDisplayName());
                    } else {
                        sprintReport.setAssignee("unassigned");
                    }
                    if (issue.get().getTimeTracking() != null) {

                        Integer estimatedMinutes = issue.get().getTimeTracking().getOriginalEstimateMinutes();
                        Integer loggedMinutes = issue.get().getTimeTracking().getTimeSpentMinutes();

                        Double gainedHours = getGainedHours(estimatedMinutes, loggedMinutes,issue.get().getStatus().getName());
                        if(gainedHours==null)
                            continue;
                        sprintReport.setTimeSaved(new DecimalFormat("##.##").format(gainedHours));
                    }
                    sprintReportList.add(sprintReport);
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error:" + e.getMessage());
                    throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
                }
            }
            startAt += 1000;
            maxResults += 1000;
            retrievedIssue = restClient.getSearchClient()
                    .searchJql(" sprint = '" + sprint + "' AND project = '" + project + "'", maxResults, startAt, null)
                    .claim().getIssues();
        }
    }

    private Double getGainedHours(Integer estimatedMinutes, Integer loggedMinutes, String status) {
        if (status != null && (status.toLowerCase().contains("ready") || (status.toLowerCase().contains("qa")))) {
            Integer timeGainedMinutes = 0;
            if(estimatedMinutes==null)
                estimatedMinutes = new Integer(0);
            if (loggedMinutes == null)
            {
                timeGainedMinutes = estimatedMinutes;
            }
            else
            {
                timeGainedMinutes = (estimatedMinutes - timeGainedMinutes);
            }
            if(timeGainedMinutes >= 0)
            {
                return ((double)timeGainedMinutes/60D);
            }
        }
        return null;
    }
}
