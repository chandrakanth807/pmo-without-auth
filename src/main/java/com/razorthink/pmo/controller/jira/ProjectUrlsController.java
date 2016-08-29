package com.razorthink.pmo.controller.jira;

import com.razorthink.pmo.bean.project_urls.AddProjectResponse;
import com.razorthink.pmo.commons.config.Constants;
import com.razorthink.pmo.commons.config.RestControllerRoute;
import com.razorthink.pmo.commons.exceptions.WebappException;
import com.razorthink.pmo.controller.AbstractWebappController;
import com.razorthink.pmo.repositories.ProjectUrlsRepository;
import com.razorthink.pmo.tables.ProjectUrls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping(value = RestControllerRoute.Jira.JIRA_BASE_ROUTE )
public class ProjectUrlsController extends AbstractWebappController {

    private final Logger logger = LoggerFactory.getLogger(ProjectUrlsController.class);

    @Autowired
    ProjectUrlsRepository projectUrlsRepository;

    @RequestMapping(value = RestControllerRoute.Jira.ProjectUrlsController.Subroute.PROJECT_URLS , method = RequestMethod.POST)
    public ResponseEntity insertRecord( @RequestBody ProjectUrls projectUrlDetails) {
        try {
            projectUrlDetails = projectUrlsRepository.save(projectUrlDetails);
            AddProjectResponse response = new AddProjectResponse();
            if (projectUrlDetails == null)
                return buildErrorResponse(new WebappException(Constants.Jira.ProjectUrlsController.FAILED_ADD_PROJECT_URL));
            response.setSuccess(true);
            response.setMessage(Constants.Jira.ProjectUrlsController.ADD_PROJECT_URL_SUCCESSFUL);
            return buildResponse(response);
        } catch(Exception ex)
        {
            return buildErrorResponse(ex);
        }
    }

    @RequestMapping(value = RestControllerRoute.Jira.ProjectUrlsController.Subroute.PROJECT_URLS, method = RequestMethod.PUT)
    public ResponseEntity updateRecord(@RequestBody ProjectUrls projectUrls) {
        try {
            ProjectUrls projectUrls1 = projectUrlsRepository.save(projectUrls);
            if (projectUrls1 == null) {
                return buildErrorResponse(new WebappException(Constants.Jira.ProjectUrlsController.FAILED_UPDATE_PROJECT_URL));
            }
            AddProjectResponse response = new AddProjectResponse();
            response.setSuccess(true);
            response.setMessage(Constants.Jira.ProjectUrlsController.UPDATE_PROJECT_URL_SUCCESSFUL);
            return buildResponse(response);
        } catch (Exception ex)
        {
            return buildErrorResponse(ex);
        }
    }

    @RequestMapping(value = RestControllerRoute.Jira.ProjectUrlsController.Subroute.PROJECT_URLS , method = RequestMethod.GET)
    public ResponseEntity listProjects() {
        List<ProjectUrls> list = projectUrlsRepository.findAll();
        for(ProjectUrls projectUrl : list)
            projectUrl.setPassword(Constants.Jira.ProjectUrlsController.DUMMY_PASSWORD);
        return buildResponse(list);
    }

    @RequestMapping(value = RestControllerRoute.Jira.ProjectUrlsController.Subroute.DELETE_PROJECT_URL , method = RequestMethod.DELETE)
    public ResponseEntity deleteRecord( @NotNull @PathVariable Integer id) {
        try{
            projectUrlsRepository.delete(id);
            AddProjectResponse response = new AddProjectResponse();
            response.setSuccess(true);
            response.setMessage(Constants.Jira.ProjectUrlsController.DELETE_PROJECT_URL_SUCCESSFUL);
            return buildResponse(response);
        } catch(Exception ex)
        {
         return buildErrorResponse(ex);
        }
    }
}
