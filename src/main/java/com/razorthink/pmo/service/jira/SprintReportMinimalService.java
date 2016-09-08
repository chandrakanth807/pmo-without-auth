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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SprintReportMinimalService {

    @Autowired
    private Environment env;

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
    public GenericReportResponse getMininmalSprintReport(BasicReportRequestParams params) throws WebappException {
        logger.debug("getMininmalSprintReport");

        ProjectUrls projectUrl = projectUrlsRepository.findOne(params.getProjectUrlId());
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
        List<IssuePOJO> retrievedIssue = JiraRestUtil.findIssuesWithJQLQuery(projectUrl, " sprint = '" + sprint + "' AND project = '" + project + "'",1000,0);
        String sprintString = JiraRestUtil.findSprintDetailsWithJQLQuery(projectUrl, " sprint = '" + sprint + "' AND project = '" + project + "'");
        Pattern pattern = Pattern.compile("\\[\".*\\[id=(.*),rapidViewId=(.*),.*,name=(.*),startDate=(.*),.*\\]");

        Matcher matcher = pattern.matcher(sprintString);

        if (matcher.find()) {
            sprintId = Integer.parseInt(matcher.group(1));
            rvId = Integer.parseInt(matcher.group(2));
        }
        Contents contents = JiraRestUtil.getRemovedAndIncompleteIssues(projectUrl,rvId,sprintId);
        Set<String> addedIssuesSet = contents.getIssueKeysAddedDuringSprint().keySet();
        Map<String, IssuePOJO> issuesAddedMap = processRetrievedIssuesAndReturnIssuesAddedDetails(projectUrl , sprint, project, maxResults, sprintReportList, retrievedIssue, addedIssuesSet);
        addHeaderString(sprintReportList,"Removed Issues");

        processPuntedIssues(projectUrl, sprintReportList, contents);
        addHeaderString(sprintReportList,"Issues Added during Sprint");
        processAddedIssues(projectUrl, sprintReportList, issuesAddedMap);

        String filename = project + "_" + sprint + "_minimal_report.csv";
        filename = filename.replace(" ", "_");
        ConvertToCSV exportToCSV = new ConvertToCSV();
        exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, sprintReportList);
        GenericReportResponse response = new GenericReportResponse();
        String downloadLink = null;
        try {
            downloadLink = env.getProperty(Constants.Jira.DOWNLOAD_LINK_BASE_PATH_PROPERTY) + URLEncoder.encode(filename, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            downloadLink = "unsupported encoding format error";
        }
        response.setDownloadLink(downloadLink);
        response.setReportAsJson(sprintReportList);
        return response;
    }

    private void processPuntedIssues( ProjectUrls projectUrl, List<SprintReport> sprintReportList, Contents contents) {
        SprintReport sprintReport;
        for (IssueGreenHopperPOJO issue : contents.getPuntedIssues()) {
            //Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
            sprintReport = new SprintReport();


                sprintReport.setIssueKey(issue.getKey());
                sprintReport.setIssueType(issue.getTypeName());
                sprintReport.setStatus(issue.getStatusName());
                sprintReport.setIssueSummary(issue.getSummary());
                if (issue.getAssigneeName() != null) {
                    sprintReport.setAssignee(issue.getAssigneeName());
                } else {
                    sprintReport.setAssignee("unassigned");
                }
                if (issue.getEstimateStatistic() != null) {
                        sprintReport.setEstimatedHours(new DecimalFormat("##.##")
                                .format(issue.getEstimateStatistic().getStatFieldValue().getValue() / (60D*60d)));
                    } else {
                        sprintReport.setEstimatedHours("0");
                    }
                    sprintReport.setLoggedHours("0");

                sprintReportList.add(sprintReport);
        }
    }

    private void processAddedIssues(ProjectUrls projectUrl, List<SprintReport> sprintReportList, Map<String,IssuePOJO> issuesAddedMap) {
        SprintReport sprintReport;
        for (Map.Entry<String,IssuePOJO> issueMapEntry : issuesAddedMap.entrySet()) {
            //Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue);
            IssuePOJO issue = issueMapEntry.getValue();
            sprintReport = new SprintReport();

                sprintReport.setIssueKey(issue.getKey());
                sprintReport.setIssueType(issue.getFields().getIssuetype().getName());
                sprintReport.setStatus(issue.getFields().getStatus().getName());
                sprintReport.setIssueSummary(issue.getFields().getSummary());
                if (issue.getFields().getAssignee() != null) {
                    sprintReport.setAssignee(issue.getFields().getAssignee().getDisplayName());
                } else {
                    sprintReport.setAssignee("unassigned");
                }

                    if (issue.getFields().getTimeoriginalestimate() != null) {
                        sprintReport.setEstimatedHours(new DecimalFormat("##.##")
                                .format(issue.getFields().getTimeoriginalestimate() / (60D*60d)));
                    } else {
                        sprintReport.setEstimatedHours("0");
                    }
                    if (issue.getFields().getTimespent() != null) {
                        sprintReport.setLoggedHours(new DecimalFormat("##.##")
                                .format(issue.getFields().getTimespent() / (60D*60d)));
                    } else {
                        sprintReport.setLoggedHours("0");
                    }

                sprintReportList.add(sprintReport);
        }
    }

    private Map<String,IssuePOJO> processRetrievedIssuesAndReturnIssuesAddedDetails(ProjectUrls projectUrl, String sprint, String project, Integer maxResults, List<SprintReport> sprintReportList, List<IssuePOJO> retrievedIssue, Set<String> addedIssues) {
        SprintReport sprintReport;
        int startAt = 0;
        Map<String, IssuePOJO> issuesAddedMap = new HashMap<>();
        while (retrievedIssue.iterator().hasNext()) {
            for (IssuePOJO issue : retrievedIssue) {

                if(addedIssues.contains(issue.getKey()))
                    issuesAddedMap.put(issue.getKey(), issue);
                //Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
                sprintReport = new SprintReport();
                    sprintReport.setIssueKey(issue.getKey());
                    sprintReport.setIssueType(issue.getFields().getIssuetype().getName());
                    sprintReport.setStatus(issue.getFields().getStatus().getName());
                    sprintReport.setIssueSummary(issue.getFields().getSummary());
                    if (issue.getFields().getAssignee() != null) {
                        sprintReport.setAssignee(issue.getFields().getAssignee().getDisplayName());
                    } else {
                        sprintReport.setAssignee("unassigned");
                    }
                        if (issue.getFields().getTimeoriginalestimate() != null) {
                            sprintReport.setEstimatedHours(new DecimalFormat("##.##")
                                    .format(issue.getFields().getTimeoriginalestimate() / (60D* 60d)));
                        } else {
                            sprintReport.setEstimatedHours("0");
                        }
                        if (issue.getFields().getTimespent() != null) {
                            sprintReport.setLoggedHours(new DecimalFormat("##.##")
                                    .format(issue.getFields().getTimespent() / (60D* 60d)));
                        } else {
                            sprintReport.setLoggedHours("0");
                        }

                    sprintReportList.add(sprintReport);
            }
            startAt += 1000;
            maxResults += 1000;
            retrievedIssue = JiraRestUtil.findIssuesWithJQLQuery(projectUrl, " sprint = '" + sprint + "' AND project = '" + project + "'",maxResults,startAt);
        }
        return issuesAddedMap;
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
