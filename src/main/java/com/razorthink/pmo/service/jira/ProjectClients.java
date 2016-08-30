package com.razorthink.pmo.service.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.pmo.bean.reports.Credls;
import com.razorthink.pmo.bean.reports.JiraClientsPOJO;
import com.razorthink.pmo.repositories.ProjectUrlsRepository;
import com.razorthink.pmo.tables.ProjectUrls;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class ProjectClients {

    private static final Logger logger = LoggerFactory.getLogger(ProjectClients.class);

    @Autowired
    private LoginService loginService;

    @Autowired
    private AdvancedLoginService advancedLoginService;

    @Autowired
    private ProjectUrlsRepository projectUrlsRepository;


    private Map<Integer,JiraClientsPOJO> projectUrlToClientMap = new HashMap<>();

    public JiraClientsPOJO getJiraClientForProjectUrlId(Integer projectUrlID)
    {
        synchronized (ProjectClients.class) {
            JiraClientsPOJO jiraClientsPOJO = projectUrlToClientMap.get(projectUrlID);

            if (jiraClientsPOJO == null) {
                ProjectUrls projectUrlDetails = projectUrlsRepository.findOne(projectUrlID);
                Credls credentials = new Credls(projectUrlDetails.getUserName(), projectUrlDetails.getPassword(), projectUrlDetails.getUrl());
                JiraRestClient jqlClient = loginService.getRestClient(credentials);
                JiraClient jiraClient = advancedLoginService.getJiraClient(credentials);
                GreenHopperClient gh = advancedLoginService.getGreenHopperClient(jiraClient);
                jiraClientsPOJO = new JiraClientsPOJO();
                jiraClientsPOJO.setJqlClient(jqlClient);
                jiraClientsPOJO.setGh(gh);
                jiraClientsPOJO.setJiraClient(jiraClient);
                projectUrlToClientMap.put(projectUrlID,jiraClientsPOJO);
                logger.info("created jira clients for project url id: "+projectUrlID);
            }
            return jiraClientsPOJO;
        }
    }
}
