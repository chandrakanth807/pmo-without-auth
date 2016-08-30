package com.razorthink.pmo.service.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueField;
import com.atlassian.util.concurrent.Promise;
import com.razorthink.pmo.bean.reports.*;
import com.razorthink.pmo.commons.config.Constants;
import com.razorthink.pmo.commons.exceptions.WebappException;
import com.razorthink.pmo.utils.ConvertToCSV;
import com.razorthink.pmo.utils.JSONUtils;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;
import net.rcarz.jiraclient.greenhopper.RapidView;
import net.rcarz.jiraclient.greenhopper.Sprint;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AggregateProjectReportService {

    @Autowired
    private Environment env;

    @Autowired
    private RemovedIssuesService removedIssuesService;

    private static final Logger logger = LoggerFactory.getLogger(AggregateProjectReportService.class);

    @Autowired
    private ProjectClients projectClients;

    /**
     * Generates an Aggregate report of the project specified in the argument
     *
     * @param basicReportRequestParams contains subproject name and board name
     * @return Complete url of the Aggregate Project report generated, and reportAsJson
     * @throws WebappException If some internal error occurs
     */
    public GenericReportResponse getAggregateProjectReport( BasicReportRequestParams basicReportRequestParams ) throws WebappException {

        logger.debug("getAggregateProjectReport");

        JiraClientsPOJO jiraClientsPOJO = projectClients.getJiraClientForProjectUrlId(basicReportRequestParams.getProjectUrlId());
        String project = basicReportRequestParams.getSubProjectName();
        String rapidViewName = basicReportRequestParams.getRapidViewName();
        if (project == null || rapidViewName == null) {
            logger.error("Error: Missing required paramaters");
            throw new WebappException(Constants.Jira.MISSING_REQUIRED_PARAMETERS);
        }
        Integer estimatedHours = 0;
        Integer loggedHours = 0;
        Integer noEstimatesCount = 0;
        Integer totalEstimates = 0;
        Integer noDescriptionCount = 0;
        Integer issuesWithoutStory = 0;
        Integer totalTasks = 0;
        Integer startAt = 0;
        Integer maxValue = 1000;
        Boolean flag = true;
        int rvId = 0;
        int sprintId = 0;
        DateTime startDt = null;
        DateTime endDt = null;
        DateTime completeDate = null;
        Double accuracy = 0.0;
        AggregateProjectReport aggregateProjectReport = new AggregateProjectReport();
        List<SprintDetails> sprintDetailsList = new ArrayList<>();
        totalTasks = processIssuesAndGetTotalTasks(jiraClientsPOJO.getJqlClient(), jiraClientsPOJO.getJiraClient(), jiraClientsPOJO.getGh(), project, rapidViewName, estimatedHours, loggedHours, issuesWithoutStory, totalTasks, flag, sprintId, startDt, endDt, aggregateProjectReport, sprintDetailsList);

        String filename = project + Constants.Jira.AGGREGATE_PROJECT_REPORT_EXTENSION;
        filename = filename.replace(" ", "_");
        ConvertToCSV exportToCSV = new ConvertToCSV();
        exportToCSV.exportToCSV(env.getProperty(Constants.Jira.CSV_DOWNLOAD_DIRECTORY_PATH_PROPERTY) + filename, aggregateProjectReport.getSprintDetails());
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(env.getProperty(Constants.Jira.CSV_DOWNLOAD_DIRECTORY_PATH_PROPERTY) + filename, true);
            fileWriter.write("Is Sprint followed?," + aggregateProjectReport.getIs_Sprint_followed() + "\n");
            fileWriter.write("Backlog Count," + aggregateProjectReport.getBacklogCount() + "\n");
            fileWriter
                    .write("Issues without Story," + aggregateProjectReport.getIssuesWithoutStory() + " / " + totalTasks);
        } catch (IOException e) {
            logger.error("Error:" + e.getMessage());
            throw new WebappException(e.getMessage());
        } finally {
            try {
                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                logger.error("Error:" + e.getMessage());
                throw new WebappException(e.getMessage());
            }
        }
        GenericReportResponse response = new GenericReportResponse();
        response.setDownloadLink(env.getProperty(Constants.Jira.DOWNLOAD_LINK_BASE_PATH_PROPERTY) + filename);
        response.setReportAsJson(aggregateProjectReport.getSprintDetails());
        return response;
    }

    private Integer processIssuesAndGetTotalTasks(JiraRestClient restClient, JiraClient jiraClient, GreenHopperClient gh, String project, String rapidViewName, Integer estimatedHours, Integer loggedHours, Integer issuesWithoutStory, Integer totalTasks, Boolean flag, int sprintId, DateTime startDt, DateTime endDt, AggregateProjectReport aggregateProjectReport, List<SprintDetails> sprintDetailsList) throws WebappException {
        int rvId;
        DateTime completeDate;
        Integer totalEstimates;
        Integer noEstimatesCount;
        Integer noDescriptionCount;
        Integer startAt;
        Integer maxValue;
        Double accuracy;
        try {
            List<RapidView> rapidviewsLIst = gh.getRapidViews();
            for (RapidView rapidView : rapidviewsLIst) {
                if (rapidView.getName().equals(rapidViewName)) {
                    flag = false;
                    rvId = rapidView.getId();
                    List<Sprint> sprintList = rapidView.getSprints();
                    if (sprintList.size() > 0) {
                        aggregateProjectReport.setIs_Sprint_followed(true);
                    } else {
                        aggregateProjectReport.setIs_Sprint_followed(false);
                    }
                    for (Sprint sprint : sprintList) {
                        SprintDetails sprintDetails = new SprintDetails();
                        sprintDetails.setName(sprint.getName());
                        completeDate = null;
                        Iterable<Issue> retrievedIssue = restClient.getSearchClient()
                                .searchJql(" sprint = " + sprint.getId() + " AND project = '" + project + "'", 1000, 0,
                                        null)
                                .claim().getIssues();
                        if (retrievedIssue.iterator().hasNext()) {
                            Pattern pattern = Pattern.compile(
                                    "[\\[,]\".*?\\[.*?=(\\d+),.*?=(\\d+),.*?name=(.*?),.*?=.*?,.*?=(.*?),.*?=(.*?),.*?=(.*?),.*?]\"");
                            Matcher matcher = pattern.matcher(
                                    retrievedIssue.iterator().next().getFieldByName("Sprint").getValue().toString());
                            logger.info(
                                    retrievedIssue.iterator().next().getFieldByName("Sprint").getValue().toString());
                            while (matcher.find()) {
                                logger.info("matched pattern is " + matcher.group());
                                if (matcher.group(3).equals(sprint.getName())) {
                                    startDt = new DateTime(matcher.group(4));
                                    endDt = new DateTime(matcher.group(5));
                                    if (!matcher.group(6).equals("<null>")) {
                                        completeDate = new DateTime(matcher.group(6));
                                    }
                                    sprintId = Integer.parseInt(matcher.group(1));
                                }
                            }
                            sprintDetails.setStartDate(startDt.toString("MM/dd/yyyy"));
                            sprintDetails.setEndDate(endDt.toString("MM/dd/yyyy"));
                            if (completeDate != null) {
                                int days = Days.daysBetween(endDt, completeDate).getDays();
                                if (days >= 1) {
                                    sprintDetails.setDeliveryStatus("Delayed by " + days + " day");
                                    if (days == 1) {
                                        sprintDetails.setDeliveryStatus("Delayed by " + days + " day");
                                    }
                                } else {
                                    sprintDetails.setDeliveryStatus("Completed on time");
                                }
                            } else {
                                sprintDetails.setDeliveryStatus("In Progress");
                            }
                            totalEstimates = 0;
                            noEstimatesCount = 0;
                            noDescriptionCount = 0;
                            startAt = 0;
                            maxValue = 1000;
                            while (retrievedIssue.iterator().hasNext()) {
                                for (Issue issueValue : retrievedIssue) {
                                    totalEstimates++;
                                    Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
                                    if (issue.get().getIssueType().getName().equals("Task")) {
                                        totalTasks++;
                                        if (!issue.get().getIssueLinks().iterator().hasNext()) {
                                            issuesWithoutStory++;
                                        }
                                    }
                                    if (issue.get().getTimeTracking() != null) {
                                        if (issue.get().getTimeTracking().getOriginalEstimateMinutes() != null) {
                                            estimatedHours += issue.get().getTimeTracking()
                                                    .getOriginalEstimateMinutes();
                                        } else {
                                            noEstimatesCount++;
                                        }
                                        if (issue.get().getTimeTracking().getTimeSpentMinutes() != null) {
                                            loggedHours += issue.get().getTimeTracking().getTimeSpentMinutes();
                                        }
                                    }
                                    if (issue.get().getDescription() == null) {
                                        noDescriptionCount++;
                                    }
                                }
                                startAt += 1000;
                                maxValue += 1000;
                                retrievedIssue = restClient.getSearchClient()
                                        .searchJql(" sprint = " + sprint.getId() + " AND project = '" + project + "'",
                                                maxValue, startAt, null)
                                        .claim().getIssues();
                            }
                            RemovedIssues removedIssues = removedIssuesService.get(jiraClient.getRestClient(), rvId, sprintId);
                            Integer changed = removedIssues.getIssuesAdded().size()
                                    + removedIssues.getPuntedIssues().size();
                            sprintDetails.setSprintChanges(changed + " / " + totalEstimates);
                            accuracy = ((estimatedHours * 1D) / loggedHours) * 100;
                            sprintDetails.setEstimatedVsActualAccuracy(accuracy.intValue() + " %");
                            sprintDetails.setEstimateProvidedStatus(
                                    (totalEstimates - noEstimatesCount) + " / " + totalEstimates);
                            sprintDetails.setTaskDescription_Statistics(
                                    (totalEstimates - noDescriptionCount) + " / " + totalEstimates);
                            sprintDetailsList.add(sprintDetails);
                            aggregateProjectReport.setSprintDetails(sprintDetailsList);
                        } else {
                            sprintDetailsList.add(sprintDetails);
                        }
                    }
                }
            }
            if (flag) {
                logger.error("Error:" + "Rapidview does not exist ");
                throw new WebappException("Invalid RapidView");
            }
            aggregateProjectReport.setIssuesWithoutStory(issuesWithoutStory);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error:" + e.getMessage());
            throw new WebappException(e.getMessage());
        }
        return totalTasks;
    }
}
