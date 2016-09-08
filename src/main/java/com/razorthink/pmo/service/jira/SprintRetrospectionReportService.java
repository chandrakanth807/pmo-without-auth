package com.razorthink.pmo.service.jira;


import com.razorthink.pmo.bean.reports.*;
import com.razorthink.pmo.bean.reports.jira.IssuePOJO;
import com.razorthink.pmo.bean.reports.jira.greenhopper.Contents;
import com.razorthink.pmo.bean.reports.jira.greenhopper.IssueGreenHopperPOJO;
import com.razorthink.pmo.commons.config.Constants;
import com.razorthink.pmo.commons.exceptions.DataException;

import com.razorthink.pmo.commons.exceptions.WebappException;
import com.razorthink.pmo.repositories.ProjectUrlsRepository;
import com.razorthink.pmo.tables.ProjectUrls;
import com.razorthink.pmo.utils.ConvertToCSV;
import com.razorthink.pmo.utils.JiraRestUtil;


import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SprintRetrospectionReportService {

    private static final Logger logger = LoggerFactory.getLogger(SprintRetrospectionReportService.class);

    @Autowired
    private Environment env;

    @Autowired
    private ProjectUrlsRepository projectUrlsRepository;


    /**
     * Generates a Sprint Retrospection report of the sprint specified in the argument
     *
     * @param params sprint name, subproject name, projectUrlID
     * @return Complete url of the sprint Retrospection report generated
     * @throws DataException If some internal error occurs
     */
    public GenericReportResponse getSprintRetrospectionReport(BasicReportRequestParams params) throws WebappException {
        logger.debug("getSprintRetrospectionReport");

        ProjectUrls projectUrl = projectUrlsRepository.findOne(params.getProjectUrlId());
        //JiraClientsPOJO jiraClientsPOJO = projectClients.getJiraClientForProjectUrlId(params.getProjectUrlId());

        String project = params.getSubProjectName();
        String sprint = params.getSprintName();
        List<SprintRetrospection> sprintRetrospectionReport = processSprintRetrospectionRelatedIssues(projectUrl, project, sprint);

        String filename = project + "_" + sprint + "_retrospection_report.csv";
        filename = filename.replace(" ", "_");
        ConvertToCSV exportToCSV = new ConvertToCSV();
        exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, sprintRetrospectionReport);

        GenericReportResponse response = new GenericReportResponse();
        String downloadLink = null;
        try {
            downloadLink = env.getProperty(Constants.Jira.DOWNLOAD_LINK_BASE_PATH_PROPERTY) + URLEncoder.encode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            downloadLink = "unsupported encoding format error";
        }
        response.setDownloadLink(downloadLink);
        response.setReportAsJson(sprintRetrospectionReport);
        return response;
    }

    private List<SprintRetrospection> processSprintRetrospectionRelatedIssues(ProjectUrls projectUrl, String project, String sprint) throws WebappException {
        int rvId = 0;
        int sprintId = 0;
        DateTime startDt = null;
        DateTime endDt = null;
        DateTime completeDate = null;
        String timezone = null;

        List<SprintRetrospection> sprintRetrospectionReport = new ArrayList<>();
        List<String> incompleteIssueKeys = new ArrayList<>();

        Set<String> assignee = new TreeSet<>();
        if (project == null || sprint == null) {
            logger.error("Error: Missing required paramaters");
            throw new DataException(HttpStatus.BAD_REQUEST.toString(), "Missing required paramaters");
        }

        List<IssuePOJO> retrievedIssue = JiraRestUtil.findIssuesWithJQLQuery(projectUrl, " sprint = '" + sprint
                + "' AND project = '" + project + "' AND assignee is not EMPTY ORDER BY assignee", 1000, 0);

        String sprintString = JiraRestUtil.findSprintDetailsWithJQLQuery(projectUrl," sprint = '" + sprint
                        + "' AND project = '" + project + "' AND assignee is not EMPTY ORDER BY assignee");
        Pattern pattern = Pattern.compile(
                "\\[\".*\\[id=(.*),rapidViewId=(.*),.*,name=(.*),goal=.*,startDate=(.*),endDate=(.*),completeDate=(.*),.*\\]");

        Matcher matcher = pattern.matcher(sprintString);
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
        processIncompletedIssues(projectUrl, rvId, sprintId, incompleteIssueKeys);
        processRetrievedIssues(projectUrl, project, sprint, startDt, endDt, completeDate, timezone, sprintRetrospectionReport, incompleteIssueKeys, assignee, retrievedIssue);
        return sprintRetrospectionReport;
    }

    private void processIncompletedIssues(ProjectUrls projectUrl, int rvId, int sprintId, List<String> incompleteIssueKeys) {
        Contents contents = JiraRestUtil.getRemovedAndIncompleteIssues(projectUrl, rvId, sprintId);
        List<IssueGreenHopperPOJO> issuesNotComplete = contents.getIssuesNotCompletedInCurrentSprint();
        for (IssueGreenHopperPOJO issueValue : issuesNotComplete) {
            incompleteIssueKeys.add(issueValue.getKey());
        }
    }

    private void processRetrievedIssues(ProjectUrls projectUrl, String project, String sprint, DateTime startDt, DateTime endDt, DateTime completeDate, String timezone, List<SprintRetrospection> sprintRetrospectionReport, List<String> incompleteIssueKeys, Set<String> assignee, List<IssuePOJO> retrievedIssue) {

        int startAt = 0;
        int maxResults = 1000;

        Map<String, SprintRetrospection> assigneeSpecificDetailsMap = new HashMap<>();
        double availableHours = calculateAvailableHours(startDt, endDt, timezone);
        while (retrievedIssue != null && retrievedIssue.size() != 0) {
            for (IssuePOJO issueValue : retrievedIssue) {

                if (issueValue.getFields().getAssignee()!=null)
                {
                    SprintRetrospection sprintRetrospection = assigneeSpecificDetailsMap.get(issueValue.getFields().getAssignee().getDisplayName());
                    boolean newEntry = false;
                    if(sprintRetrospection == null)
                    {
                        sprintRetrospection = new SprintRetrospection();
                        newEntry = true;
                    }

                    Double originalEstimate = issueValue.getFields().getTimeoriginalestimate();
                    Double timeSpent = issueValue.getFields().getTimespent();
                    if(originalEstimate !=null)
                    {
                        originalEstimate = originalEstimate/(60d*60d);
                        double es = sprintRetrospection.getEstimatedHours();
                        sprintRetrospection.setEstimatedHours(es + originalEstimate);
                    }
                    if(timeSpent !=null)
                    {
                        timeSpent = timeSpent/(60d *60d);
                        double ts = sprintRetrospection.getTimeTaken();
                        sprintRetrospection.setTimeTaken(ts + timeSpent);
                    }
                    if(incompleteIssueKeys.contains(issueValue.getKey()))
                    {
                        int temp = sprintRetrospection.getIncompletedIssues();
                        sprintRetrospection.setIncompletedIssues(temp + 1);
                    }
                    int temp = sprintRetrospection.getTotalTasks();
                    sprintRetrospection.setTotalTasks(temp + 1);
                    if(newEntry)
                        assigneeSpecificDetailsMap.put(issueValue.getFields().getAssignee().getDisplayName(), sprintRetrospection);

                }
            }
            startAt += maxResults;
            retrievedIssue = JiraRestUtil.findIssuesWithJQLQuery(projectUrl, " sprint = '" + sprint
                    + "' AND project = '" + project + "' AND assignee is not EMPTY ORDER BY assignee", maxResults, startAt);
        }


        for(Map.Entry<String,SprintRetrospection> entry : assigneeSpecificDetailsMap.entrySet() )
        {
            SprintRetrospection sprintRetrospection = entry.getValue();
            sprintRetrospection.setAssignee(entry.getKey());

            sprintRetrospection.setAvailableHours(availableHours);
            sprintRetrospection.setSurplus(sprintRetrospection.getAvailableHours() - sprintRetrospection.getEstimatedHours());
            sprintRetrospection.setBuffer(new DecimalFormat("##.##").format((sprintRetrospection.getSurplus() / availableHours) * 100)+" %");
            double efficiency = ((sprintRetrospection.getEstimatedHours() - sprintRetrospection.getTimeTaken()) / sprintRetrospection.getEstimatedHours())*100;
            sprintRetrospection.setEfficiency(new DecimalFormat("##.##").format(100d + efficiency)+" %");

        }
        sprintRetrospectionReport.addAll(assigneeSpecificDetailsMap.values());

    }

    private double calculateAvailableHours(DateTime startDt, DateTime endDt, String timezone) {
        double availableHours = 0d;
        DateTime tempDate = new DateTime(startDt.getMillis(), DateTimeZone.forID(ZoneId.of(timezone).toString()));
        while (tempDate.compareTo(endDt) <= 0) {
            if (tempDate.getDayOfWeek() != DateTimeConstants.SATURDAY
                    && tempDate.getDayOfWeek() != DateTimeConstants.SUNDAY) {
                availableHours += 1;
            }
            tempDate = tempDate.plusDays(1);

        }
        availableHours *= 8D;
        return availableHours;
    }
}