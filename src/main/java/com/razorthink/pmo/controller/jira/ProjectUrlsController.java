package com.razorthink.pmo.controller.jira;

import com.razorthink.pmo.bean.project_urls.AddProjectResponse;
import com.razorthink.pmo.commons.exceptions.WebappException;
import com.razorthink.pmo.controller.AbstractWebappController;
import com.razorthink.pmo.controller.test.TestController;
import com.razorthink.pmo.repositories.ProjectUrlsRepository;
import com.razorthink.pmo.tables.ProjectUrls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping(value = "/rest/jira")
public class ProjectUrlsController extends AbstractWebappController {
    private final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    ProjectUrlsRepository projectUrlsRepository;

    @RequestMapping(value = "/projects", method = RequestMethod.POST)
    public ResponseEntity insertRecord( @RequestBody ProjectUrls projectUrlDetails) {
        try {
            projectUrlDetails = projectUrlsRepository.save(projectUrlDetails);
            AddProjectResponse response = new AddProjectResponse();
            if (projectUrlDetails == null)
                return buildErrorResponse(new WebappException("Failed to add Project URL"));
            response.setSuccess(true);
            response.setMessage("Project Url details added successfully");
            return buildResponse(response);
        } catch(Exception ex)
        {
            return buildErrorResponse(ex);
        }
    }

    @RequestMapping(value = "/projects", method = RequestMethod.PUT)
    public ResponseEntity updateRecord(@RequestBody ProjectUrls projectUrls) {
        try {
            ProjectUrls projectUrls1 = projectUrlsRepository.save(projectUrls);
            if (projectUrls1 == null) {
                return buildErrorResponse(new WebappException("Failed to update Project URL"));
            }
            AddProjectResponse response = new AddProjectResponse();
            response.setSuccess(true);
            response.setMessage("Project Url update successful");
            return buildResponse(response);
        } catch (Exception ex)
        {
            return buildErrorResponse(ex);
        }
    }

    @RequestMapping(value = "/projects", method = RequestMethod.GET)
    public ResponseEntity listProjects() {
        List<ProjectUrls> list = projectUrlsRepository.findAll();
        for(ProjectUrls projectUrl : list)
            projectUrl.setPassword("dummy");
        return buildResponse(list);
    }

    @RequestMapping(value = "/projects/{id}", method = RequestMethod.DELETE)
    public ResponseEntity deleteRecord( @NotNull @PathVariable Integer id) {
        try{
            projectUrlsRepository.delete(id);
            AddProjectResponse response = new AddProjectResponse();
            response.setSuccess(true);
            response.setMessage("Project Url deletion successful");
            return buildResponse(response);
        } catch(Exception ex)
        {
         return buildErrorResponse(ex);
        }
    }
}
