package com.razorthink.pmo.service.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.util.concurrent.Promise;
import com.razorthink.pmo.bean.reports.*;
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
public class SprintReportMinimalService {

    @Autowired
    private Environment env;

    @Autowired
    private RemovedIssuesService removedIssuesService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private AdvancedLoginService advancedLoginService;

    @Autowired
    private ProjectUrlsRepository projectUrlsRepository;


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
            throw new DataException(HttpStatus.BAD_REQUEST.toString(), "Missing required paramaters");
        }
        List<SprintReport> sprintReportList = new ArrayList<>();
        SprintReport sprintReport;
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
        sprintReport.setAssignee(" ");
        sprintReport.setEstimatedHours(" ");
        sprintReport.setIssueSummary(" ");
        sprintReport.setIssueType(" ");
        sprintReport.setLoggedHours(" ");
        sprintReport.setStatus(" ");
        sprintReport.setIssueKey("Removed Issues");
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
        try {
            RemovedIssues removedIssues = removedIssuesService.get(jiraClient.getRestClient(), rvId, sprintId);
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
            sprintReport.setIssueKey("Issues Added during Sprint");
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
        } catch (JiraException e) {
            logger.error("Error:" + e.getMessage());
            throw new DataException(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
        }
        String filename = project + "_" + sprint + "_minimal_report.csv";
        filename = filename.replace(" ", "_");
        ConvertToCSV exportToCSV = new ConvertToCSV();
        exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, sprintReportList);
        GenericReportResponse response = new GenericReportResponse();
        response.setDownloadLink(env.getProperty("csv.aliaspath") + filename);
        response.setReportAsJson(sprintReportList);
        return response;
    }
}
