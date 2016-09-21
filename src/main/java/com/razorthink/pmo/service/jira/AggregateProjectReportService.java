package com.razorthink.pmo.service.jira;

import com.razorthink.pmo.bean.projecturls.RapidView;
import com.razorthink.pmo.bean.projecturls.Sprint;
import com.razorthink.pmo.bean.projecturls.SprintAndRapidViewId;
import com.razorthink.pmo.bean.reports.*;
import com.razorthink.pmo.bean.reports.jira.IssuePOJO;
import com.razorthink.pmo.bean.reports.jira.greenhopper.Contents;
import com.razorthink.pmo.commons.config.Constants;
import com.razorthink.pmo.commons.exceptions.WebappException;
import com.razorthink.pmo.repositories.ProjectUrlsRepository;
import com.razorthink.pmo.tables.ProjectUrls;
import com.razorthink.pmo.utils.ConvertToCSV;
import com.razorthink.pmo.utils.JiraRestUtil;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AggregateProjectReportService {

    @Autowired
    private Environment env;

    private static final Logger logger = LoggerFactory.getLogger(AggregateProjectReportService.class);

    @Autowired
    private ProjectUrlsRepository projectUrlsRepository;

    /**
     * Generates an Aggregate report of the project specified in the argument
     *
     * @param basicReportRequestParams contains subproject name and board name
     * @return Complete url of the Aggregate Project report generated, and reportAsJson
     * @throws WebappException If some internal error occurs
     */
    public GenericReportResponse getAggregateProjectReport( BasicReportRequestParams basicReportRequestParams ) throws WebappException {

        logger.debug("getAggregateProjectReport");

        ProjectUrls projectUrl = projectUrlsRepository.findOne(basicReportRequestParams.getProjectUrlId());
        String project = basicReportRequestParams.getSubProjectName();
        String rapidViewName = basicReportRequestParams.getRapidViewName();

        if (project == null || rapidViewName == null) {
            logger.error("Error: Missing required paramaters");
            throw new WebappException(Constants.Jira.MISSING_REQUIRED_PARAMETERS);
        }

        AggregateProjectReport aggregateProjectReport = new AggregateProjectReport();
        List<SprintDetails> sprintDetailsList = new ArrayList<>();
        Integer totalTasks = processIssuesAndGetTotalTasks( projectUrl, project, rapidViewName, aggregateProjectReport, sprintDetailsList);

        String filename = project + Constants.Jira.AGGREGATE_PROJECT_REPORT_EXTENSION;
        filename = filename.replace(" ", "_");
        ConvertToCSV exportToCSV = new ConvertToCSV();
        if(aggregateProjectReport.getSprintDetails()==null)
        {
            List<SprintDetails> sprintDetailsTemp = new ArrayList<>();
            aggregateProjectReport.setSprintDetails(sprintDetailsTemp);
        }
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
        String downloadLink = null;
        try {
            downloadLink = env.getProperty(Constants.Jira.DOWNLOAD_LINK_BASE_PATH_PROPERTY) + URLEncoder.encode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            downloadLink = "unsupported encoding format error";
        }
        response.setDownloadLink(downloadLink);
        response.setReportAsJson(aggregateProjectReport.getSprintDetails());
        return response;
    }

    private Integer processIssuesAndGetTotalTasks(ProjectUrls projectUrl, String project, String rapidViewName, AggregateProjectReport aggregateProjectReport, List<SprintDetails> sprintDetailsList) throws WebappException {
        int rvId;
        DateTime completeDate;
        Integer totalEstimates;
        Integer noEstimatesCount;
        Integer noDescriptionCount;
        Integer startAt;
        Integer maxValue;
        Double accuracy;
        Double estimatedHours = 0d;
        Double loggedHours = 0d;
        Integer issuesWithoutStory = 0;
        Boolean flag = true;
        int sprintId = 0;
        DateTime startDt = null;
        DateTime endDt = null;
        int totalTasks = 0;
        try {
            List<RapidView> rapidviewsLIst = JiraRestUtil.getBoards(projectUrl);

            for (RapidView rapidView : rapidviewsLIst) {
                if (rapidView.getRapidViewName().equals(rapidViewName)) {
                    flag = false;
                    rvId = rapidView.getRapidViewId();
                    List<Sprint> sprintList = rapidView.getSprintList();
                    if (sprintList.size() > 0) {
                        aggregateProjectReport.setIs_Sprint_followed(true);
                    } else {
                        aggregateProjectReport.setIs_Sprint_followed(false);
                    }
                    for (Sprint sprint : sprintList) {
                        SprintDetails sprintDetails = new SprintDetails();
                        sprintDetails.setName(sprint.getSprintName());
                        completeDate = null;

                        String jqlQuery = " sprint = " + sprint.getSprintId() + " AND project = '" + project + "'";
                        List<IssuePOJO> retrievedIssue = JiraRestUtil.findIssuesWithJQLQuery(projectUrl,jqlQuery,1000,0);

                        SprintAndRapidViewId sprintAndRapidViewIdDetails = JiraRestUtil.getSprintDetails(rapidViewName,sprint.getSprintName(), rapidviewsLIst);
                        startDt = sprintAndRapidViewIdDetails.getSprint().getStartDate();
                        endDt = sprintAndRapidViewIdDetails.getSprint().getEndDate();
                        completeDate = sprintAndRapidViewIdDetails.getSprint().getCompleteDate();

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

                        if (retrievedIssue.iterator().hasNext()) {

                            sprintId = sprintAndRapidViewIdDetails.getSprint().getSprintId();
                            totalEstimates = 0;
                            noEstimatesCount = 0;
                            noDescriptionCount = 0;
                            startAt = 0;
                            maxValue = 1000;
                            while (retrievedIssue.iterator().hasNext()) {
                                for (IssuePOJO issue : retrievedIssue) {
                                    totalEstimates++;
                                    //Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
                                    if (issue.getFields().getIssuetype().getName().equalsIgnoreCase("Task")) {
                                        totalTasks++;
                                        if (issue.getFields().getIssuelinks().isEmpty()) {
                                            issuesWithoutStory++;
                                        }
                                    }
                                    if (issue.getFields().getTimeoriginalestimate() != null) {
                                        estimatedHours += (issue.getFields().getTimeoriginalestimate()/(60d*60d));
                                    } else {
                                        noEstimatesCount++;
                                    }
                                    if (issue.getFields().getTimespent() != null) {
                                        loggedHours += (issue.getFields().getTimespent()/(60d*60d));
                                    }
                                    if (issue.getFields().getDescription() == null) {
                                        noDescriptionCount++;
                                    }
                                }
                                startAt += 1000;
                                maxValue += 1000;
                                retrievedIssue = JiraRestUtil.findIssuesWithJQLQuery(projectUrl," sprint = " + sprint.getSprintId() + " AND project = '" + project + "'",maxValue,startAt);
                            }
                            Contents contents = JiraRestUtil.getRemovedAndIncompleteIssues(projectUrl,rvId,sprintId);

                            Integer changed = contents.getIssueKeysAddedDuringSprint().keySet().size()
                                    + contents.getPuntedIssues().size();

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
                            sprintDetails.setEstimatedVsActualAccuracy(" 0 %");
                            sprintDetails.setSprintChanges("0 / 0");
                            sprintDetails.setEstimateProvidedStatus("0 / 0");
                            sprintDetails.setTaskDescription_Statistics("0 / 0");
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
