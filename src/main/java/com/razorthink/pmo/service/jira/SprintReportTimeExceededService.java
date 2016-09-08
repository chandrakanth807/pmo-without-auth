package com.razorthink.pmo.service.jira;


import com.razorthink.pmo.bean.reports.*;
import com.razorthink.pmo.bean.reports.jira.IssuePOJO;
import com.razorthink.pmo.commons.config.Constants;
import com.razorthink.pmo.commons.exceptions.DataException;
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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@Service
public class SprintReportTimeExceededService {

    @Autowired
    private Environment env;

    @Autowired
    private ProjectUrlsRepository projectUrlsRepository;

    private static final Logger logger = LoggerFactory.getLogger(SprintReportTimeExceededService.class);

    /**
     * Generates a minimal report of the sprint specified in the argument including
     * issues removed from sprint and issues added during sprint
     *
     * @param params sprint name, subproject name and projecturlID
     * @return Complete url of the minimal sprint report generated
     * @throws DataException If some internal error occurs
     */
    public GenericReportResponse getMininmalSprintReportTimeExceeded(BasicReportRequestParams params) {
        logger.debug("getMininmalSprintReportTimeExceeded");

        ProjectUrls projectUrl = projectUrlsRepository.findOne(params.getProjectUrlId());

        String sprint_name = params.getSprintName();
        String project = params.getSubProjectName();
        Integer maxResults = 1000;
        Integer startAt = 0;
        /*int rvId = 0;
        int sprintId = 0;*/
        if (project == null || sprint_name == null) {
            logger.error("Error: Missing required paramaters");
            throw new DataException(HttpStatus.BAD_REQUEST.toString(), Constants.Jira.MISSING_REQUIRED_PARAMETERS);
        }
        List<SprintReportTimeExceeded> sprintReportList = new ArrayList<>();
        SprintReportTimeExceeded sprintReport;

        List<IssuePOJO> retrievedIssue = JiraRestUtil.findIssuesWithJQLQuery(projectUrl, " sprint = '" + sprint_name + "' AND project = '" + project + "'", 1000, 0);

       /* Pattern pattern = Pattern.compile("\\[\".*\\[id=(.*),rapidViewId=(.*),.*,name=(.*),startDate=(.*),.*\\]");
        Matcher matcher = pattern.matcher(
                "[\"" + retrievedIssue.get(0).getFields().getCustomfield_10003().get(0) + "\"]");
        if (matcher.find()) {
            sprintId = Integer.parseInt(matcher.group(1));
            rvId = Integer.parseInt(matcher.group(2));
        }*/
        processRetrievedIssues(projectUrl, sprint_name, project, maxResults, startAt, sprintReportList, retrievedIssue);

        String filename = project + "_" + sprint_name + "_minimal_report_time_exceeded.csv";
        filename = filename.replace(" ", "_");
        ConvertToCSV exportToCSV = new ConvertToCSV();
        exportToCSV.exportToCSV(env.getProperty("csv.filename") + filename, sprintReportList);
        GenericReportResponse response = new GenericReportResponse();
        response.setDownloadLink(env.getProperty("csv.aliaspath") + filename);
        response.setReportAsJson(sprintReportList);
        return response;
    }

    private void processRetrievedIssues(ProjectUrls projectUrl, String sprint, String project, Integer maxResults, Integer startAt, List<SprintReportTimeExceeded> sprintReportList, List<IssuePOJO> retrievedIssue) {
        SprintReportTimeExceeded sprintReport;
        while (retrievedIssue.iterator().hasNext()) {
            for (IssuePOJO issue : retrievedIssue) {
                //Promise<Issue> issue = restClient.getIssueClient().getIssue(issueValue.getKey());
                sprintReport = new SprintReportTimeExceeded();

                sprintReport.setIssueKey(issue.getKey());
                sprintReport.setIssueType(issue.getFields().getIssuetype().getName());
                sprintReport.setStatus(issue.getFields().getStatus().getName());
                sprintReport.setIssueSummary(issue.getFields().getSummary());
                if (issue.getFields().getAssignee() != null) {
                    sprintReport.setAssignee(issue.getFields().getAssignee().getDisplayName());
                } else {
                    sprintReport.setAssignee("unassigned");
                }

                Double estimatedSeconds = issue.getFields().getTimeoriginalestimate();
                Double loggedSeconds = issue.getFields().getTimespent();
                Double exceededHours = getTimeExceeded(estimatedSeconds, loggedSeconds, issue.getFields().getStatus().getName());
                if (exceededHours == null)
                    continue;
                sprintReport.setTimeExceeded(new DecimalFormat("##.##").format(exceededHours));

                sprintReportList.add(sprintReport);

            }
            startAt += 1000;
            maxResults += 1000;
            retrievedIssue = JiraRestUtil.findIssuesWithJQLQuery(projectUrl, " sprint = '" + sprint + "' AND project = '" + project + "'", maxResults, startAt);
        }
    }

    private Double getTimeExceeded(Double estimatedSeconds, Double loggedSeconds, String status) {
        if (status != null && (status.toLowerCase().contains("ready") || (status.toLowerCase().contains("qa"))) && loggedSeconds!=null && estimatedSeconds != null ) {
            Double timeExceededSeconds = (loggedSeconds - estimatedSeconds);

            if (timeExceededSeconds > 0) {
                return (timeExceededSeconds / (60D*60D));
            }
        }
        return null;
    }
}
