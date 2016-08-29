package com.razorthink.pmo.controller.jira;

import com.razorthink.pmo.bean.reports.BasicReportRequestParams;
import com.razorthink.pmo.commons.config.RestControllerRoute;
import com.razorthink.pmo.controller.AbstractWebappController;
import com.razorthink.pmo.service.jira.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping( value = RestControllerRoute.Jira.JIRA_BASE_ROUTE )
public class ReportsController extends AbstractWebappController {

    private final Logger logger = LoggerFactory.getLogger(ReportsController.class);

    @Autowired
    private AggregateProjectReportService aggregateProjectReportService;

    @Autowired
    private SprintReportMinimalService sprintReportMinimalService;

    @Autowired
    private SprintRetrospectionReportService sprintRetrospectionReportService;

    @Autowired
    private SprintReportTimeGainedService sprintReportTimeGainedService;

    @Autowired
    private SprintReportTimeExceededService sprintReportTimeExceededService;


    @RequestMapping(value = RestControllerRoute.Jira.ReportsController.Subroute.GET_AGGREGATE_PROJ_REPORT, method = RequestMethod.GET)
    public ResponseEntity getAggregateProjectReport(@PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.PROJECT_URL_ID) Integer projectUrlId, @PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.RAPID_VIEW_NAME) String rapidViewName, @PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.SUB_PROJECT_NAME) String subProjectName) {
        try {
            BasicReportRequestParams basicReportRequestParams = new BasicReportRequestParams();
            basicReportRequestParams.setProjectUrlId(projectUrlId);
            basicReportRequestParams.setRapidViewName(rapidViewName);
            basicReportRequestParams.setSubProjectName(subProjectName);
            return buildResponse( aggregateProjectReportService.getAggregateProjectReport( basicReportRequestParams ));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return buildErrorResponse(e);
        }
    }



    @RequestMapping(value = RestControllerRoute.Jira.ReportsController.Subroute.GET_SPRINT_MINIMAL_REPORT, method = RequestMethod.GET)
    public ResponseEntity getMinimalSprintReport(@PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.PROJECT_URL_ID) Integer projectUrlId, @PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.RAPID_VIEW_NAME) String rapidViewName, @PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.SPRINT_NAME) String sprintName, @PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.SUB_PROJECT_NAME) String subProjectName ) {
        try {
            BasicReportRequestParams basicReportRequestParams = new BasicReportRequestParams();
            basicReportRequestParams.setProjectUrlId(projectUrlId);
            basicReportRequestParams.setRapidViewName(rapidViewName);
            basicReportRequestParams.setSubProjectName(subProjectName);
            basicReportRequestParams.setSprintName(sprintName);
            return buildResponse(sprintReportMinimalService.getMininmalSprintReport(basicReportRequestParams));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return buildErrorResponse(e);
        }
    }

    @RequestMapping(value = RestControllerRoute.Jira.ReportsController.Subroute.GET_SPRINT_RETROSPECTION_REPORT, method = RequestMethod.GET)
    public ResponseEntity getSprintRetrospectionReport(@PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.PROJECT_URL_ID) Integer projectUrlId, @PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.RAPID_VIEW_NAME) String rapidViewName, @PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.SPRINT_NAME) String sprintName, @PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.SUB_PROJECT_NAME) String subProjectName ) {
        try {
            BasicReportRequestParams basicReportRequestParams = new BasicReportRequestParams();
            basicReportRequestParams.setProjectUrlId(projectUrlId);
            basicReportRequestParams.setRapidViewName(rapidViewName);
            basicReportRequestParams.setSubProjectName(subProjectName);
            basicReportRequestParams.setSprintName(sprintName);
            return buildResponse(sprintRetrospectionReportService.getSprintRetrospectionReport(basicReportRequestParams));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return buildErrorResponse(e);
        }
    }

    @RequestMapping(value = RestControllerRoute.Jira.ReportsController.Subroute.GET_TIME_GAINED_SPRINT_REPORT, method = RequestMethod.GET)
    public ResponseEntity getMinimalSprintReportTimeGained(@PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.PROJECT_URL_ID) Integer projectUrlId, @PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.RAPID_VIEW_NAME) String rapidViewName, @PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.SPRINT_NAME) String sprintName, @PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.SUB_PROJECT_NAME) String subProjectName ) {
        try {
            BasicReportRequestParams basicReportRequestParams = new BasicReportRequestParams();
            basicReportRequestParams.setProjectUrlId(projectUrlId);
            basicReportRequestParams.setRapidViewName(rapidViewName);
            basicReportRequestParams.setSubProjectName(subProjectName);
            basicReportRequestParams.setSprintName(sprintName);
            return buildResponse(sprintReportTimeGainedService.getMininmalSprintReportTimeGained(basicReportRequestParams));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return buildErrorResponse(e);
        }
    }

    @RequestMapping(value = RestControllerRoute.Jira.ReportsController.Subroute.GET_TIME_EXCEEDED_SPRINT_REPORT, method = RequestMethod.GET)
    public ResponseEntity getMinimalSprintReportTimeExceeded(@PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.PROJECT_URL_ID) Integer projectUrlId, @PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.RAPID_VIEW_NAME) String rapidViewName, @PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.SPRINT_NAME) String sprintName, @PathVariable(RestControllerRoute.Jira.ReportsController.Subroute.URLParam.SUB_PROJECT_NAME) String subProjectName ) {
        try {
            BasicReportRequestParams basicReportRequestParams = new BasicReportRequestParams();
            basicReportRequestParams.setProjectUrlId(projectUrlId);
            basicReportRequestParams.setRapidViewName(rapidViewName);
            basicReportRequestParams.setSubProjectName(subProjectName);
            basicReportRequestParams.setSprintName(sprintName);
            return buildResponse(sprintReportTimeExceededService.getMininmalSprintReportTimeExceeded(basicReportRequestParams));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return buildErrorResponse(e);
        }
    }
}