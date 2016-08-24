package com.razorthink.pmo.utils;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.JiraException;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;
import net.rcarz.jiraclient.greenhopper.RapidView;
import net.rcarz.jiraclient.greenhopper.Sprint;
import org.apache.commons.collections.IteratorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class JiraService {

    /**
     * Generate instance for logger
     */
    private static final Logger logger = LoggerFactory.getLogger(JiraService.class);

    protected static final String access_token = "1EXzZED9FhXGT5Ag7uvx6e6pYO00ZZbW";
    private JiraRestClient restClient = null;
    private IssueRestClient issueClient = null;
    private String url = null;
    private String userName = null;
    private String password = null;
    private List<BasicProject> projects = new ArrayList<BasicProject>();

    private List<Sprint> allSprints = null;

    private List<Issue> allIssues = null;

    private List<Issue> linkedIssues = null;

    private List<RapidView> allRapidView = null;

    public JiraRestClient authorize(String url, String userName, String password) throws JiraException {
        logger.debug("Authenticating user " + userName);
        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        URI jiraServerUri = null;
        this.url = url;
        this.userName = userName;
        this.password = password;
        try {
            jiraServerUri = new URI(url);
        } catch (URISyntaxException e) {
            logger.error("Could not load URI \nMessage: " + e.getMessage() + " \nCause: " + e.getCause());
            throw new JiraException("Could not load URI " + e.getCause());
        }

        try {
            restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, userName, password);
            restClient.getProjectClient().getAllProjects().claim();
            issueClient = restClient.getIssueClient();
        } catch (Exception e) {
            logger.error("Authentication failed " + "\nMessage: " + e.getMessage() + " \nCause: " + e.getCause());
            throw new JiraException("Authentication failed");
        }
        return restClient;

    }

    @SuppressWarnings("unchecked")
    public List<BasicProject> getProjects() throws JiraException {
        logger.debug("Listing projects");
        try {
            Iterator<BasicProject> proj = restClient.getProjectClient().getAllProjects().claim().iterator();
            projects = IteratorUtils.toList(proj);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Listing of projects failed" + "\nMessage: " + e.getMessage() + " \nCause: " + e.getCause());
            throw new JiraException("Listing of projects failed " + e.getCause());

        }
        return projects;
    }

    @SuppressWarnings("unchecked")
    public List<Sprint> getAllSprints() throws JiraException {
        logger.debug("Listing sprints");
        try {

            BasicCredentials creds = new BasicCredentials(userName, password);
            JiraClient jira = new JiraClient(url, creds);

            GreenHopperClient gh = new GreenHopperClient(jira);

            allRapidView = gh.getRapidViews();
            allSprints = IteratorUtils.toList(allRapidView.iterator());
        } catch (Exception e) {
            logger.error("Listing of issues sprints" + "\nMessage: " + e.getMessage() + " \nCause: " + e.getCause());
            throw new JiraException("Listing of issues sprints " + e.getCause());

        }
        return allSprints;
    }

    @SuppressWarnings("unchecked")
    public List<Issue> getAllIssues(String projectName) throws JiraException {
        logger.debug("Listing issues");
        try {
            allIssues = IteratorUtils.toList(restClient.getSearchClient()
                    .searchJql("project = '" + projectName + "'", 1000, 0, null).claim().getIssues().iterator());
        } catch (Exception e) {
            logger.error("Listing of issues failed " + "\nMessage: " + e.getMessage() + " \nCause: " + e.getCause());
            throw new JiraException("Listing of issues failed " + e.getCause());
        }
        return allIssues;

    }

    public List<Issue> filterIssues(String projectName, String startDate, String endDate, String sprintType,
                                    String sprintName, String backlog) throws JiraException {
        logger.debug("Filtering issues");
        List<Issue> issues = new ArrayList<Issue>();
        try {
            if (projectName != null) {
                // Listing issues between given duration
                if (startDate != null && endDate != null) {
                    issues = (List<Issue>) restClient.getSearchClient().searchJql("project = '" + projectName
                            + "' AND created >= '" + startDate + "' AND created < '" + endDate + "'").claim()
                            .getIssues();
                }
                // Listing issues based on open/closed sprints
                if (sprintType != null) {
                    if ((sprintType).equals("open")) {
                        issues = (List<Issue>) restClient.getSearchClient()
                                .searchJql("project = '" + projectName + "' AND sprint in openSprints()").claim()
                                .getIssues();
                    } else if ((sprintType).equals("closed")) {
                        issues = (List<Issue>) restClient.getSearchClient()
                                .searchJql("project = '" + projectName + "' AND sprint in closedSprints()").claim()
                                .getIssues();
                    }
                }
                // Listing issues based on sprint name
                if (sprintName != null) {
                    issues = (List<Issue>) restClient.getSearchClient()
                            .searchJql("project = '" + projectName + "' AND sprint = '" + sprintName + "'").claim()
                            .getIssues();
                }
                // Listing backlogged issues
                if (backlog != null) {
                    issues = (List<Issue>) restClient.getSearchClient()
                            .searchJql("project = '" + projectName + "' AND status = 'QA BACKLOG'").claim().getIssues();
                }
            }
        } catch (Exception e) {
            logger.error("Filtering of issues failed " + "\nMessage: " + e.getMessage() + " \nCause: " + e.getCause());
            throw new JiraException("Filtering of issues failed " + e.getCause());
        }
        return issues;
    }

    public Issue sycnIssue(String projectName, String issuekey, String type, String updated) throws JiraException {
        Issue issue;
        try {
            // Listing issues to be synced
            List<Issue> issues = (List<Issue>) restClient.getSearchClient().searchJql("project = '" + projectName
                    + "' AND issuekey ='" + issuekey + "' AND type = '" + type + "' AND updated >='-" + updated + "'")
                    .claim().getIssues();
            issue = null;
            if (issues.size() > 0) {
                issue = issueClient.getIssue(issuekey, Arrays.asList(IssueRestClient.Expandos.CHANGELOG)).claim();
            }
        } catch (Exception e) {
            logger.error("Syncing of issues failed " + "\nMessage: " + e.getMessage() + " \nCause: " + e.getCause());
            throw new JiraException("Syncing of issues failed " + e.getCause());
        }
        return issue;
    }

    @SuppressWarnings("unchecked")
    public List<Issue> getLinkedIssues(JiraRestClient client, String projectName, String issueKey)
            throws JiraException {
        logger.debug("Listing issues");
        try {
            linkedIssues = IteratorUtils.toList(client.getSearchClient()
                    .searchJql("project='" + projectName + "' && issue in linkedIssues('" + issueKey + "')", 1000, 0,
                            null)
                    .claim().getIssues().iterator());
        } catch (Exception e) {
            logger.error("Listing of issues failed " + "\nMessage: " + e.getMessage() + " \nCause: " + e.getCause());
            throw new JiraException("Listing of issues failed " + e.getCause());
        }
        return linkedIssues;

    }
}
