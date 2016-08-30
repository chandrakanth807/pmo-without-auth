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
public class SprintReportMinimalService {

    @Autowired
    private Environment env;

    @Autowired
    private RemovedIssuesService removedIssuesService;

   @Autowired
   private ProjectClients projectClients;


    private static final Logger logger = LoggerFactory.getLogger(SprintReportMinimalService.class);

    /**
     * Generates a minimal report of the sprint specified in the argument including
     * issues removed from sprint and issues added during sprint
     *
     * @param params sprint name, subproject name and projecturlID
     * @return Complete url of the minimal sprint report generated
     * @throws DataException If some internal error occurs
     */
    public GenericReportResponse getMininmalSprintReport(BasicReportRequestParams params) {
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
        List<SprintReport> sprintReportList = new ArrayList<>();
        SprintReport sprintReport;
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
        addHeaderString(sprintReportList,"Removed Issues");
        processRemovedIssues(jiraClientsPOJO.getJqlClient(), jiraClientsPOJO.getJiraClient(), rvId, sprintId, sprintReportList);
        String filename = project + "_" + sprint + "_minimal_report.csv";
        filename = filename.replace(" ", "_");
        ConvertToCSV exportToCSV = new ConvertToCSV();
        exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, sprintReportList);
        GenericReportResponse response = new GenericReportResponse();
        response.setDownloadLink(env.getProperty("csv.aliaspath") + filename);
        response.setReportAsJson(sprintReportList);
        return response;
    }

    private void processRemovedIssues(JiraRestClient restClient, JiraClient jiraClient, int rvId, int sprintId, List<SprintReport> sprintReportList) {
        SprintReport sprintReport;
        try {
            RemovedIssues removedIssues = removedIssuesService.get(jiraClient.getRestClient(), rvId, sprintId);
            processPuntedIssues(restClient, sprintReportList, removedIssues);
            addHeaderString(sprintReportList,"Issues Added during Sprint");
            processAddedIssues(restClient, sprintReportList, removedIssues);
        } catch (JiraException e) {
            logger.error("Error:" + e.getMessage());
            throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
        }
    }

    private void processPuntedIssues(JiraRestClient restClient, List<SprintReport> sprintReportList, RemovedIssues removedIssues) {
        SprintReport sprintReport;
        for (SprintIssue issueValue : removedIssues.getPuntedIssues()) {
            Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
            sprintReport = new SprintReport();
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
                    if (issue.get().getTimeTracking().getOriginalEstimateMinutes() != null) {
                        sprintReport.setEstimatedHours(new DecimalFormat("##.##")
                                .format(issue.get().getTimeTracking().getOriginalEstimateMinutes() / 60D));
                    } else {
                        sprintReport.setEstimatedHours("0");
                    }
                    if (issue.get().getTimeTracking().getTimeSpentMinutes() != null) {
                        sprintReport.setLoggedHours(new DecimalFormat("##.##")
                                .format(issue.get().getTimeTracking().getTimeSpentMinutes() / 60D));
                    } else {
                        sprintReport.setLoggedHours("0");
                    }
                }
                sprintReportList.add(sprintReport);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error:" + e.getMessage());
                throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
            }
        }
    }

    private void processAddedIssues(JiraRestClient restClient, List<SprintReport> sprintReportList, RemovedIssues removedIssues) {
        SprintReport sprintReport;
        for (String issueValue : removedIssues.getIssuesAdded()) {
            Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue);
            sprintReport = new SprintReport();
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
                    if (issue.get().getTimeTracking().getOriginalEstimateMinutes() != null) {
                        sprintReport.setEstimatedHours(new DecimalFormat("##.##")
                                .format(issue.get().getTimeTracking().getOriginalEstimateMinutes() / 60D));
                    } else {
                        sprintReport.setEstimatedHours("0");
                    }
                    if (issue.get().getTimeTracking().getTimeSpentMinutes() != null) {
                        sprintReport.setLoggedHours(new DecimalFormat("##.##")
                                .format(issue.get().getTimeTracking().getTimeSpentMinutes() / 60D));
                    } else {
                        sprintReport.setLoggedHours("0");
                    }
                }
                sprintReportList.add(sprintReport);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error:" + e.getMessage());
                throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
            }
        }
    }

    private void processRetrievedIssues(JiraRestClient restClient, String sprint, String project, Integer maxResults, Integer startAt, List<SprintReport> sprintReportList, Iterable<Issue> retrievedIssue) {
        SprintReport sprintReport;
        while (retrievedIssue.iterator().hasNext()) {
            for (Issue issueValue : retrievedIssue) {
                Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
                sprintReport = new SprintReport();
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
                        if (issue.get().getTimeTracking().getOriginalEstimateMinutes() != null) {
                            sprintReport.setEstimatedHours(new DecimalFormat("##.##")
                                    .format(issue.get().getTimeTracking().getOriginalEstimateMinutes() / 60D));
                        } else {
                            sprintReport.setEstimatedHours("0");
                        }
                        if (issue.get().getTimeTracking().getTimeSpentMinutes() != null) {
                            sprintReport.setLoggedHours(new DecimalFormat("##.##")
                                    .format(issue.get().getTimeTracking().getTimeSpentMinutes() / 60D));
                        } else {
                            sprintReport.setLoggedHours("0");
                        }
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

    private void addHeaderString(List<SprintReport> sprintReportList, String issueKeyHeader) {
        SprintReport sprintReport;
        for (int i = 0; i < 2; i++) {
            sprintReport = new SprintReport();
            sprintReport.setAssignee(" ");
            sprintReport.setEstimatedHours(" ");
            sprintReport.setIssueKey(" ");
            sprintReport.setIssueSummary(" ");
            sprintReport.setIssueType(" ");
            sprintReport.setLoggedHours(" ");
            sprintReport.setStatus(" ");
            sprintReportList.add(sprintReport);
        }
        sprintReport = new SprintReport();
        sprintReport.setIssueKey(issueKeyHeader);
        sprintReport.setAssignee(" ");
        sprintReport.setEstimatedHours(" ");
        sprintReport.setIssueSummary(" ");
        sprintReport.setIssueType(" ");
        sprintReport.setLoggedHours(" ");
        sprintReport.setStatus(" ");
        sprintReportList.add(sprintReport);
        sprintReport = new SprintReport();
        sprintReport.setAssignee(" ");
        sprintReport.setEstimatedHours(" ");
        sprintReport.setIssueKey(" ");
        sprintReport.setIssueSummary(" ");
        sprintReport.setIssueType(" ");
        sprintReport.setLoggedHours(" ");
        sprintReport.setStatus(" ");
        sprintReportList.add(sprintReport);
    }
}
