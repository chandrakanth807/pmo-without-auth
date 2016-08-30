package com.razorthink.pmo.bean.reports;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;

public class JiraClientsPOJO {

    private JiraRestClient jqlClient;
    private JiraClient jiraClient;
    private GreenHopperClient gh;

    public JiraRestClient getJqlClient() {
        return jqlClient;
    }

    public void setJqlClient(JiraRestClient jqlClient) {
        this.jqlClient = jqlClient;
    }

    public JiraClient getJiraClient() {
        return jiraClient;
    }

    public void setJiraClient(JiraClient jiraClient) {
        this.jiraClient = jiraClient;
    }

    public GreenHopperClient getGh() {
        return gh;
    }

    public void setGh(GreenHopperClient gh) {
        this.gh = gh;
    }
}
