package com.razorthink.pmo.service;

import com.razorthink.pmo.bean.reports.Credls;
import com.razorthink.pmo.commons.exceptions.DataException;
import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.JiraClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;


@Service
public class AdvancedLoginService {

    private static final Logger logger = LoggerFactory.getLogger(AdvancedLoginService.class);

    /**
     * AdvancedLogin Authorize is used to authorize JiraClient and GreenHopperClient
     * using the credentials provided.They can be used to perform operations in Jira
     * by means of Rest calls.
     *
     * @param credentials Contains username,password and url to authorize the user to Jira
     */
    private JiraClient authorize(Credls credentials) {
        logger.debug("advance authorizing");
        JiraClient jira;
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        String url = credentials.getProjectUrl();
        BasicCredentials creds = new BasicCredentials(username, password);
        jira = new JiraClient(url, creds);
        return jira;
    }

    /**
     * Returns a JiraClient Object which is used to fetch details from Jira.
     *
     * @return JiraClient object
     * @throws DataException if user is not logged in
     */
    public JiraClient getJiraClient(Credls credentials) {
        return authorize(credentials);
    }

    /**
     * Returns a GreenHopperClient Object which is used to fetch details from Jira.
     *
     * @return GreenHopperClient object
     * @throws DataException if user is not logged in
     */
    public GreenHopperClient getGreenHopperClient(JiraClient jiraClient) {
        return new GreenHopperClient(jiraClient);

    }
}
