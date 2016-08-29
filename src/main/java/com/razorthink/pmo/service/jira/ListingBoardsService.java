package com.razorthink.pmo.service.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.pmo.bean.projecturls.RapidView;
import com.razorthink.pmo.bean.projecturls.Sprint;
import com.razorthink.pmo.bean.projecturls.SubProject;
import com.razorthink.pmo.bean.projecturls.jira.JiraResponseObject;
import com.razorthink.pmo.bean.projecturls.jira.Value;
import com.razorthink.pmo.bean.reports.Credls;
import com.razorthink.pmo.commons.exceptions.WebappException;
import com.razorthink.pmo.repositories.ProjectUrlsRepository;
import com.razorthink.pmo.tables.ProjectUrls;
import com.razorthink.pmo.utils.JSONUtils;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.RestClient;
import net.rcarz.jiraclient.RestException;
import net.sf.json.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ListingBoardsService {

    @Autowired
    private LoginService loginService;

    @Autowired
    private AdvancedLoginService advancedLoginService;

    @Autowired
    private ProjectUrlsRepository projectUrlsRepository;

    public List<RapidView> getBoards( Integer projectUrlId) throws WebappException {

            if ( projectUrlId != null) {
                ProjectUrls projectUrlDetails = projectUrlsRepository.findOne(projectUrlId);
                Credls credentials = new Credls(projectUrlDetails.getUserName(), projectUrlDetails.getPassword(), projectUrlDetails.getUrl());
                JiraRestClient restClient = loginService.getRestClient(credentials);
                JiraClient jiraClient = advancedLoginService.getJiraClient(credentials);
                List<RapidView> rapidViewList = getBoards(jiraClient);
                return rapidViewList;
            } else {
                throw new WebappException("Please provide ProjectUrlId");
            }
    }

    public List<RapidView> getBoards(JiraClient advancedClient) throws WebappException {
        RestClient restClient = advancedClient.getRestClient();
        try {
            URI uri = restClient.buildURI("/rest/agile/1.0/board");
            JSON jsonResult = restClient.get(uri);

            JiraResponseObject jiraResponseObject = JSONUtils.parse(jsonResult.toString(), JiraResponseObject.class);

            List<RapidView> rapidViewList = new ArrayList<>();

            for(Value value : jiraResponseObject.getValues())
            {
                RapidView rapidView = new RapidView();
                rapidView.setRapidViewId(value.getId());
                rapidView.setRapidViewName(value.getName());
                rapidViewList.add(rapidView);
            }
            populateSprintsForEachRapidView(rapidViewList, restClient);
            populateSubProjectsForEachRapidView(rapidViewList, restClient);
            return rapidViewList;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new WebappException(e);
        } catch (RestException e) {
            throw new WebappException(e);
        } catch (IOException e) {
            throw new WebappException(e);
        }
    }

    private void populateSprintsForEachRapidView(List<RapidView> rapidViewList, RestClient restClient) throws WebappException {
        for(RapidView rapidView : rapidViewList) {
            try {
                URI uri = restClient.buildURI("/rest/agile/1.0/board/"+rapidView.getRapidViewId()+"/sprint");
                JSON jsonResult = restClient.get(uri);

                JiraResponseObject jiraResponseObject = JSONUtils.parse(jsonResult.toString(), JiraResponseObject.class);

                List<Sprint> sprintList = new ArrayList<>();
                for(Value value : jiraResponseObject.getValues())
                {
                    Sprint sprint = new Sprint();
                    sprint.setSprintId(value.getId());
                    sprint.setSprintName(value.getName());
                    sprint.setSprintState(value.getState());
                    sprintList.add(sprint);
                }
                rapidView.setSprintList(sprintList);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                throw new WebappException(e);
            } catch (RestException e) {
                throw new WebappException(e);
            } catch (IOException e) {
                throw new WebappException(e);
            }
        }

    }

    private void populateSubProjectsForEachRapidView(List<RapidView> rapidViewList, RestClient restClient) throws WebappException {
        for(RapidView rapidView : rapidViewList) {
            try {
                URI uri = restClient.buildURI("/rest/agile/1.0/board/"+rapidView.getRapidViewId()+"/project");
                JSON jsonResult = restClient.get(uri);
                JiraResponseObject jiraResponseObject = JSONUtils.parse(jsonResult.toString(), JiraResponseObject.class);

                List<SubProject> subProjectList = new ArrayList<>();
                for(Value value : jiraResponseObject.getValues())
                {
                    SubProject subProject = new SubProject();
                    subProject.setSubProjectId(value.getId());
                    subProject.setSubProjectName(value.getName());
                    subProjectList.add(subProject);
                }
                rapidView.setSubProjectList(subProjectList);
            } catch (URISyntaxException e) {
                e.printStackTrace();
                throw new WebappException(e);
            } catch (RestException e) {
                throw new WebappException(e);
            } catch (IOException e) {
                throw new WebappException(e);
            }
        }
    }
}
