package com.razorthink.pmo.service.jira;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.razorthink.pmo.bean.reports.Credls;
import com.razorthink.pmo.commons.exceptions.DataException;
import com.razorthink.pmo.utils.JiraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);


    /**
     * Authorize is used to authorize JiraRestClient using the credentials provided.
     * It can be used to perform operations in Jira by means of Rest calls.
     *
     * @param credentials credentials of project jira url
     * @return JiraRestClient object
     */
    private JiraRestClient authorize(Credls credentials) {
        logger.debug("authorizing");

        JiraService js = new JiraService();
        JiraRestClient restClient;
        String username = credentials.getUsername();
        String password = credentials.getPassword();
        String url = credentials.getProjectUrl();
        if (username == null || password == null || url == null) {
            throw new DataException(HttpStatus.BAD_REQUEST.name(), "Parameters cannot be null");
        }
        try {
            restClient = js.authorize(url, username, password);
            restClient.getProjectClient().getAllProjects().claim();
            return restClient;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new DataException(HttpStatus.BAD_REQUEST.name(), "Could not login");
        }
    }

    /**
     * Returns a JiraRestClient Object which is used to fetch details from Jira.
     *
     * @return JiraRestClient object
     * @throws DataException if user is not logged in
     */
    public JiraRestClient getRestClient(Credls credentials) {
        return authorize(credentials);

    }
}
