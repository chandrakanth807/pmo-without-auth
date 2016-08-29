package com.razorthink.pmo.service.jira;


import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.util.concurrent.Promise;
import com.razorthink.pmo.bean.reports.*;
import com.razorthink.pmo.commons.config.Constants;
import com.razorthink.pmo.commons.exceptions.DataException;
import com.razorthink.pmo.repositories.ProjectUrlsRepository;
import com.razorthink.pmo.tables.ProjectUrls;
import com.razorthink.pmo.utils.ConvertToCSV;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;
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
public class SprintReportTimeExceededService {

    @Autowired
    private Environment env;

    @Autowired
    private RemovedIssuesService removedIssuesService;

    @Autowired
    private IncompletedIssuesService incompletedIssuesService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private AdvancedLoginService advancedLoginService;

    @Autowired
    private ProjectUrlsRepository projectUrlsRepository;

    private static final Logger logger = LoggerFactory.getLogger(SprintReportTimeExceededService.class);

    /**
     * Generates a minimal report of the sprint specified in the argument including
     * issues removed from sprint and issues added during sprint
     *
     * @param params     sprint name, subproject name and projecturlID
     * @return Complete url of the minimal sprint report generated
     * @throws DataException If some internal error occurs
     */
    public GenericReportResponse getMininmalSprintReportTimeExceeded(BasicReportRequestParams params) {
        logger.debug("getMininmalSprintReport");
        ProjectUrls projectUrlDetails = projectUrlsRepository.findOne(params.getProjectUrlId());
        Credls credentials = new Credls(projectUrlDetails.getUserName(), projectUrlDetails.getPassword(), projectUrlDetails.getUrl());
        JiraRestClient restClient = loginService.getRestClient(credentials);
        JiraClient jiraClient = advancedLoginService.getJiraClient(credentials);
        GreenHopperClient gh = advancedLoginService.getGreenHopperClient(jiraClient);

        String sprint = params.getSprintName();
        String project = params.getSubProjectName();
        Integer maxResults = 1000;
        Integer startAt = 0;
        int rvId = 0;
        int sprintId = 0;
        if (project == null || sprint == null) {
            logger.error("Error: Missing required paramaters");
            throw new DataException(HttpStatus.BAD_REQUEST.toString(), Constants.Jira.MISSING_REQUIRED_PARAMETERS);
        }
        List<SprintReportTimeExceeded> sprintReportList = new ArrayList<>();
        SprintReportTimeExceeded sprintReport;
        Iterable<Issue> retrievedIssue = restClient.getSearchClient()
                .searchJql(" sprint = '" + sprint + "' AND project = '" + project + "'", 1000, 0, null).claim()
                .getIssues();
        Pattern pattern = Pattern.compile("\\[\".*\\[id=(.*),rapidViewId=(.*),.*,name=(.*),startDate=(.*),.*\\]");
        Matcher matcher = pattern
                .matcher(retrievedIssue.iterator().next().getFieldByName("Sprint").getValue().toString());
        if (matcher.find()) {
            sprintId = Integer.parseInt(matcher.group(1));
            rvId = Integer.parseInt(matcher.group(2));
        }
        processRetrievedIssues(restClient, sprint, project, maxResults, startAt, sprintReportList, retrievedIssue);
        String headerString = "Removed Issues";
        appendHeading(sprintReportList, headerString);
        processRemovedIssues(restClient, jiraClient, rvId, sprintId, sprintReportList);
        String filename = project + "_" + sprint + "_minimal_report_time_exceeded.csv";
        filename = filename.replace(" ", "_");
        ConvertToCSV exportToCSV = new ConvertToCSV();
        exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, sprintReportList);
        GenericReportResponse response = new GenericReportResponse();
        response.setDownloadLink(env.getProperty("csv.aliaspath") + filename);
        response.setReportAsJson(sprintReportList);
        return response;
    }

    private void processRemovedIssues(JiraRestClient restClient, JiraClient jiraClient, int rvId, int sprintId, List<SprintReportTimeExceeded> sprintReportList) {
        SprintReportTimeExceeded sprintReport;
        String headerString;
        try {
            RemovedIssues removedIssues = removedIssuesService.get(jiraClient.getRestClient(), rvId, sprintId);
            processPuntedIssues(restClient, sprintReportList, removedIssues);
            headerString = "Issues Added during Sprint";
            appendHeading(sprintReportList, headerString);

            processIssuesAdded(restClient, sprintReportList, removedIssues);
        } catch (JiraException e) {
            logger.error("Error:" + e.getMessage());
            throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
        }
    }

    private void processIssuesAdded(JiraRestClient restClient, List<SprintReportTimeExceeded> sprintReportList, RemovedIssues removedIssues) {
        SprintReportTimeExceeded sprintReport;
        for (String issueValue : removedIssues.getIssuesAdded()) {
            Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue);
            sprintReport = new SprintReportTimeExceeded();
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

                    Double exceededHours = getTimeExceeded(estimatedMinutes, loggedMinutes, issue.get().getStatus().getName());
                    if (exceededHours == null)
                        continue;
                    sprintReport.setTimeExceeded(new DecimalFormat("##.##").format(exceededHours));
                }
                sprintReportList.add(sprintReport);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error:" + e.getMessage());
                throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
            }
        }
    }

    private void processPuntedIssues(JiraRestClient restClient, List<SprintReportTimeExceeded> sprintReportList, RemovedIssues removedIssues) {
        SprintReportTimeExceeded sprintReport;
        for (SprintIssue issueValue : removedIssues.getPuntedIssues()) {
            Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
            sprintReport = new SprintReportTimeExceeded();
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

                    Double exceededHours = getTimeExceeded(estimatedMinutes, loggedMinutes, issue.get().getStatus().getName());
                    if (exceededHours == null)
                        continue;
                    sprintReport.setTimeExceeded(new DecimalFormat("##.##").format(exceededHours));

                }
                sprintReportList.add(sprintReport);
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error:" + e.getMessage());
                throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
            }
        }
    }

    private void appendHeading(List<SprintReportTimeExceeded> sprintReportList, String headerString) {
        SprintReportTimeExceeded sprintReport;
        for (int i = 0; i < 2; i++) {
            sprintReport = new SprintReportTimeExceeded();
            sprintReport.setAssignee(" ");
            sprintReport.setIssueKey(" ");
            sprintReport.setIssueSummary(" ");
            sprintReport.setIssueType(" ");
            sprintReport.setTimeExceeded(" ");
            sprintReport.setStatus(" ");
            sprintReportList.add(sprintReport);
        }
        sprintReport = new SprintReportTimeExceeded();
        sprintReport.setAssignee(" ");
        sprintReport.setIssueSummary(" ");
        sprintReport.setIssueType(" ");
        sprintReport.setTimeExceeded(" ");
        sprintReport.setStatus(" ");
        sprintReport.setIssueKey(headerString);
        sprintReportList.add(sprintReport);
        sprintReport = new SprintReportTimeExceeded();
        sprintReport.setAssignee(" ");
        sprintReport.setIssueKey(" ");
        sprintReport.setIssueSummary(" ");
        sprintReport.setIssueType(" ");
        sprintReport.setTimeExceeded(" ");
        sprintReport.setStatus(" ");
        sprintReportList.add(sprintReport);
    }

    private void processRetrievedIssues(JiraRestClient restClient, String sprint, String project, Integer maxResults, Integer startAt, List<SprintReportTimeExceeded> sprintReportList, Iterable<Issue> retrievedIssue) {
        SprintReportTimeExceeded sprintReport;
        while (retrievedIssue.iterator().hasNext()) {
            for (Issue issueValue : retrievedIssue) {
                Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
                sprintReport = new SprintReportTimeExceeded();
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

                        Double exceededHours = getTimeExceeded(estimatedMinutes, loggedMinutes, issue.get().getStatus().getName());
                        if (exceededHours == null)
                            continue;
                        sprintReport.setTimeExceeded(new DecimalFormat("##.##").format(exceededHours));
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

    private Double getTimeExceeded(Integer estimatedMinutes, Integer loggedMinutes, String status) {
        if (status != null && (status.toLowerCase().contains("ready") || (status.toLowerCase().contains("qa")))) {
            Integer timeExceededMinutes = 0;
            if (loggedMinutes == null)
                return null;
            if(estimatedMinutes==null)
                estimatedMinutes = new Integer(0);
            timeExceededMinutes = (loggedMinutes - estimatedMinutes);

            if (timeExceededMinutes > 0) {
                return ((double) timeExceededMinutes / 60D);
            }
        }
        return null;
    }
}
