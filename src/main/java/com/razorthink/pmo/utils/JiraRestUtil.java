package com.razorthink.pmo.utils;

import com.google.common.base.Joiner;
import com.razorthink.pmo.bean.projecturls.RapidView;
import com.razorthink.pmo.bean.projecturls.Sprint;
import com.razorthink.pmo.bean.projecturls.SprintAndRapidViewId;
import com.razorthink.pmo.bean.projecturls.SubProject;
import com.razorthink.pmo.bean.projecturls.jira.*;
import com.razorthink.pmo.bean.reports.jira.IssuePOJO;
import com.razorthink.pmo.bean.reports.jira.IssuesSearchResult;
import com.razorthink.pmo.bean.reports.jira.greenhopper.Contents;
import com.razorthink.pmo.bean.reports.jira.greenhopper.SearchResult;
import com.razorthink.pmo.commons.exceptions.WebappException;
import com.razorthink.pmo.tables.ProjectUrls;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.apache.commons.codec.binary.Base64;
import org.joda.time.DateTime;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class JiraRestUtil {

    public static String httpGetMethodWithBasicAuth(URI uri, String name, String password) {
        /*String name = "amith.koujalgi";
        String password = "amith123";*/
        String authString = name + ":" + password;

        String authStringEnc = new Base64().encodeToString(authString.getBytes());
        Client cclient = Client.create();
        WebResource webResource = cclient.resource(uri);
        WebResource.Builder builder = webResource.accept("application/json").header("Authorization", "Basic " + authStringEnc);
        ClientResponse resp = null;
        int count =0;
        while(count<5) {
            try {
                resp = builder.get(ClientResponse.class);
                count++;
                break;
            } catch (Exception ce) {
                continue;
            }
        }

        if (resp.getStatus() != 200) {
            System.err.println("Unable to connect to the server" + uri);
        }

        return resp.getEntity(String.class);
    }

    public static List<IssuePOJO> findIssuesWithJQLQuery(ProjectUrls projectUrl, String jqlQuery, int maxResults, int startAt) {

        List<String> expandos = new ArrayList<>();
        expandos.add("schema");
        expandos.add("names");
        /*String urlString = projectUrl.getUrl();
        urlString = urlString.trim();
        if(urlString.endsWith("/"));
        {
            urlString = urlString.substring(0,urlString.length()-1);
        }*/
        String restCall = "/rest/api/latest/search";
        String customUrlString = appendProjectURLAndRestCall(projectUrl.getUrl(),restCall);
        UriBuilder uriBuilder = UriBuilder.fromUri(customUrlString).queryParam("jql", new Object[]{jqlQuery}).queryParam("expand", new Object[]{Joiner.on(",").join(expandos)})
                .queryParam("maxResults", new Object[]{maxResults})
                .queryParam("startAt", new Object[]{startAt});
        URI uri = uriBuilder.build(new Object[0]);
        String jsonResult = httpGetMethodWithBasicAuth(uri, projectUrl.getUserName(), projectUrl.getPassword());
        IssuesSearchResult searchResult = JSONUtils.parse(jsonResult, IssuesSearchResult.class);
        return searchResult.getIssues();
    }

    public static Contents getRemovedAndIncompleteIssues(ProjectUrls projectUrl, Integer rapidViewId, Integer sprintId) {
        String restCall = "rest/greenhopper/1.0/rapid/charts/sprintreport";

        String customUrl = appendProjectURLAndRestCall(projectUrl.getUrl(), restCall);

        UriBuilder uriBuilder = UriBuilder.fromUri(customUrl)
                .queryParam("rapidViewId", new Object[]{rapidViewId})
                .queryParam("sprintId", new Object[]{sprintId});
        URI uri = uriBuilder.build(new Object[0]);
        String jsonResult = httpGetMethodWithBasicAuth(uri, projectUrl.getUserName(), projectUrl.getPassword());
        SearchResult greenhopperSearchResult = JSONUtils.parse(jsonResult, SearchResult.class);

        return greenhopperSearchResult.getContents();
    }

    public static List<RapidView> getBoards(ProjectUrls projectUrl) {

        String restCall = "rest/agile/1.0/board";
        String customUrl = appendProjectURLAndRestCall(projectUrl.getUrl(), restCall);

        UriBuilder uriBuilder = UriBuilder.fromUri(customUrl);
        URI uri = uriBuilder.build(new Object[0]);

        String jsonResult = httpGetMethodWithBasicAuth(uri, projectUrl.getUserName(), projectUrl.getPassword());
        //JSON jsonResult = restClient.get(uri);
        BoardsResponsePOJO boardsResponsePOJO = JSONUtils.parse(jsonResult, BoardsResponsePOJO.class);

        List<RapidView> rapidViewList = new ArrayList<>();

        for (BoardValue boardValue : boardsResponsePOJO.getValues()) {
            com.razorthink.pmo.bean.projecturls.RapidView rapidView = new com.razorthink.pmo.bean.projecturls.RapidView();
            rapidView.setRapidViewId(boardValue.getId());
            rapidView.setRapidViewName(boardValue.getName());
            rapidViewList.add(rapidView);
        }
        populateSprintsForEachRapidView(rapidViewList, projectUrl);
        populateSubProjectsForEachRapidView(rapidViewList, projectUrl);
        return rapidViewList;

    }

    private static void populateSprintsForEachRapidView(List<RapidView> rapidViewList, ProjectUrls projectUrl) {
        for (RapidView rapidView : rapidViewList) {
            String restCall = "rest/agile/1.0/board/" + rapidView.getRapidViewId() + "/sprint";
            String customUrl = appendProjectURLAndRestCall(projectUrl.getUrl(), restCall);

            UriBuilder uriBuilder = UriBuilder.fromUri(customUrl);
            URI uri = uriBuilder.build(new Object[0]);
            String output = httpGetMethodWithBasicAuth(uri, projectUrl.getUserName(), projectUrl.getPassword());
            SprintsResponsePOJO sprintsResponsePOJO = JSONUtils.parse(output, SprintsResponsePOJO.class);

            List<Sprint> sprintList = new ArrayList<>();
            for (SprintValue sprintValue : sprintsResponsePOJO.getValues()) {
                Sprint sprint = new Sprint();
                sprint.setSprintId(sprintValue.getId());
                sprint.setSprintName(sprintValue.getName());
                sprint.setSprintState(sprintValue.getState());
                sprint.setCompleteDate(new DateTime(sprintValue.getCompleteDate()));
                sprint.setEndDate(new DateTime(sprintValue.getEndDate()));
                sprint.setStartDate(new DateTime(sprintValue.getStartDate()));
                sprintList.add(sprint);
            }
            rapidView.setSprintList(sprintList);
        }
    }

    private static void populateSubProjectsForEachRapidView(List<RapidView> rapidViewList, ProjectUrls projectUrl) {
        for (RapidView rapidView : rapidViewList) {

            String restCall = "rest/agile/1.0/board/" + rapidView.getRapidViewId() + "/project";
            String customUrl = appendProjectURLAndRestCall(projectUrl.getUrl(), restCall);

            UriBuilder uriBuilder = UriBuilder.fromUri(customUrl);
            URI uri = uriBuilder.build(new Object[0]);
            String jsonResult = httpGetMethodWithBasicAuth(uri, projectUrl.getUserName(), projectUrl.getPassword());
            SubProjectsResponse subProjectsResponse = JSONUtils.parse(jsonResult, SubProjectsResponse.class);

            List<SubProject> subProjectList = new ArrayList<>();
            for (SubProjectValue subProjectValue : subProjectsResponse.getValues()) {
                SubProject subProject = new SubProject();
                subProject.setSubProjectId(subProjectValue.getId());
                subProject.setSubProjectName(subProjectValue.getName());
                subProjectList.add(subProject);
            }
            rapidView.setSubProjectList(subProjectList);
        }
    }

    private static String appendProjectURLAndRestCall(String projectUrl, String restCall) {
        //projectUrl = appendProjectURLAndRestCall(projectUrl, restCall);
        projectUrl = projectUrl.trim();
        if (projectUrl.endsWith("/"))
            projectUrl = projectUrl + restCall;
        else
            projectUrl = projectUrl + "/" + restCall;
        return projectUrl;
    }

    public static SprintAndRapidViewId getSprintDetails(String rapidViewName, String sprintName, List<RapidView> rapidViewsList) throws WebappException {
        for(RapidView rapidView : rapidViewsList)
        {
            if(rapidView.getRapidViewName().trim().equals(rapidViewName))
            {
                for(Sprint sprint : rapidView.getSprintList())
                {
                    if(sprint.getSprintName().trim().equals(sprintName))
                    {
                        SprintAndRapidViewId returnObj = new SprintAndRapidViewId();
                        returnObj.setSprint(sprint);
                        returnObj.setRapidViewId(rapidView.getRapidViewId());
                        return returnObj;
                    }
                }
            }
        }
        throw new WebappException("sprint details not found; sprint name: "+sprintName+" ; rapidViewName: "+rapidViewName);
    }
}
