package com.razorthink.pmo.controller.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.pmo.bean.reports.BasicReportRequestParams;
import com.razorthink.pmo.bean.reports.Credls;
import com.razorthink.pmo.commons.exceptions.WebappException;
import com.razorthink.pmo.controller.AbstractWebappController;
import com.razorthink.pmo.repositories.ProjectUrlsRepository;
import com.razorthink.pmo.service.*;
import com.razorthink.pmo.tables.ProjectUrls;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Path;

@RestController
@RequestMapping("/rest/jira/projects")
public class ReportsController extends AbstractWebappController {

    private final Logger logger = LoggerFactory.getLogger(ReportsController.class);

    @Autowired
    private AggregateProjectReportService aggregateProjectReportService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private AdvancedLoginService advancedLoginService;

    @Autowired
    private ProjectUrlsRepository projectUrlsRepository;

    @Autowired
    private SprintReportMinimalService sprintReportMinimalService;

    @Autowired
    private SprintRetrospectionReportService sprintRetrospectionReportService;


    @RequestMapping(value = "/{projectUrlId}/boards/{rapidViewName}/subProject/{subProjectName}/reports/board-summary", method = RequestMethod.GET)
    public ResponseEntity getAggregateProjectReport(@PathVariable("projectUrlId") Integer projectUrlId, @PathVariable("rapidViewName") String rapidViewName, @PathVariable("subProjectName") String subProjectName) {
        try {
            ProjectUrls projectUrlDetails = projectUrlsRepository.findOne(projectUrlId);
            Credls credentials = new Credls(projectUrlDetails.getUserName(), projectUrlDetails.getPassword(), projectUrlDetails.getUrl());
            JiraRestClient restClient = loginService.getRestClient(credentials);
            JiraClient jiraClient = advancedLoginService.getJiraClient(credentials);
            GreenHopperClient gh = advancedLoginService.getGreenHopperClient(jiraClient);
            BasicReportRequestParams basicReportRequestParams = new BasicReportRequestParams();
            basicReportRequestParams.setRapidViewName(rapidViewName);
            basicReportRequestParams.setSubProjectName(subProjectName);
            return buildResponse(aggregateProjectReportService.getAggregateProjectReport(basicReportRequestParams, restClient, jiraClient, gh));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return buildErrorResponse(e);
        }
    }



    @RequestMapping(value = "/{projectUrlId}/boards/{rapidViewName}/sprint/{sprintName}/subProject/{subProjectName}/reports/generic", method = RequestMethod.GET)
    public ResponseEntity getMinimalSprintReport(@PathVariable("projectUrlId") Integer projectUrlId, @PathVariable("rapidViewName") String rapidViewName, @PathVariable("sprintName") String sprintName, @PathVariable("subProjectName") String subProjectName ) {
        try {
            ProjectUrls projectUrlDetails = projectUrlsRepository.findOne(projectUrlId);
            Credls credentials = new Credls(projectUrlDetails.getUserName(), projectUrlDetails.getPassword(), projectUrlDetails.getUrl());
            JiraRestClient restClient = loginService.getRestClient(credentials);
            JiraClient jiraClient = advancedLoginService.getJiraClient(credentials);
            GreenHopperClient gh = advancedLoginService.getGreenHopperClient(jiraClient);
            BasicReportRequestParams basicReportRequestParams = new BasicReportRequestParams();
            basicReportRequestParams.setRapidViewName(rapidViewName);
            basicReportRequestParams.setSubProjectName(subProjectName);
            basicReportRequestParams.setSprintName(sprintName);
            return buildResponse(sprintReportMinimalService.getMininmalSprintReport(basicReportRequestParams, restClient, jiraClient));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return buildErrorResponse(e);
        }
    }

    @RequestMapping(value = "/{projectUrlId}/boards/{rapidViewName}/sprints/{sprintName}/subProject/{subProjectName}/reports/sprint-retrospection", method = RequestMethod.GET)
    public ResponseEntity getSprintRetrospectionReport(@PathVariable("projectUrlId") Integer projectUrlId, @PathVariable("rapidViewName") String rapidViewName, @PathVariable("sprintName") String sprintName, @PathVariable("subProjectName") String subProjectName ) {
        try {
            ProjectUrls projectUrlDetails = projectUrlsRepository.findOne(projectUrlId);
            Credls credentials = new Credls(projectUrlDetails.getUserName(), projectUrlDetails.getPassword(), projectUrlDetails.getUrl());
            JiraRestClient restClient = loginService.getRestClient(credentials);
            JiraClient jiraClient = advancedLoginService.getJiraClient(credentials);
            BasicReportRequestParams basicReportRequestParams = new BasicReportRequestParams();
            basicReportRequestParams.setRapidViewName(rapidViewName);
            basicReportRequestParams.setSubProjectName(subProjectName);
            basicReportRequestParams.setSprintName(sprintName);
            return buildResponse(sprintRetrospectionReportService.getSprintRetrospectionReport(basicReportRequestParams, restClient, jiraClient));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return buildErrorResponse(e);
        }
    }
}