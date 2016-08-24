package com.razorthink.pmo.controller.reports;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.pmo.bean.reports.BasicReportRequestParams;
import com.razorthink.pmo.bean.reports.Credls;
import com.razorthink.pmo.controller.AbstractWebappController;
import com.razorthink.pmo.repositories.ProjectUrlsRepository;
import com.razorthink.pmo.service.AdvancedLoginService;
import com.razorthink.pmo.service.LoginService;
import com.razorthink.pmo.service.SprintReportMinimalService;
import com.razorthink.pmo.tables.ProjectUrls;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sprintReport")
public class SprintReportController extends AbstractWebappController {

    private final Logger logger = LoggerFactory.getLogger(SprintReportController.class);

    @Autowired
    private SprintReportMinimalService sprintReportMinimalService;

    @Autowired
    private LoginService loginService;

    @Autowired
    private AdvancedLoginService advancedLoginService;

    @Autowired
    private ProjectUrlsRepository projectUrlsRepository;

    @RequestMapping(value = "/getMinimalSprintReport", method = RequestMethod.POST)
    public ResponseEntity getMinimalSprintReport(@RequestBody BasicReportRequestParams basicReportRequestParams) {
        try {
            ProjectUrls projectUrlDetails = projectUrlsRepository.findOne(basicReportRequestParams.getProjectUrlId());
            Credls credentials = new Credls(projectUrlDetails.getUserName(), projectUrlDetails.getPassword(), projectUrlDetails.getUrl());
            JiraRestClient restClient = loginService.getRestClient(credentials);
            JiraClient jiraClient = advancedLoginService.getJiraClient(credentials);
            GreenHopperClient gh = advancedLoginService.getGreenHopperClient(jiraClient);
            return buildResponse(sprintReportMinimalService.getMininmalSprintReport(basicReportRequestParams, restClient, jiraClient));
        } catch (Exception e) {
            logger.error(e.getMessage());
            return buildErrorResponse(e);
        }
    }
}
