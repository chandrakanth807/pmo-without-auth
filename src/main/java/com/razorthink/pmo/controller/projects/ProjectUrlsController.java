package com.razorthink.pmo.controller.projects;

import com.razorthink.pmo.controller.AbstractWebappController;
import com.razorthink.pmo.controller.test.TestController;
import com.razorthink.pmo.repositories.ProjectUrlsRepository;
import com.razorthink.pmo.tables.ProjectUrls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping(value = "/rest/jira")
public class ProjectUrlsController extends AbstractWebappController {
    private final Logger logger = LoggerFactory.getLogger(TestController.class);

    @Autowired
    ProjectUrlsRepository projectUrlsRepository;

    @RequestMapping(value = "/insert", method = RequestMethod.GET)
    public ResponseEntity insertRecord() {
        ProjectUrls tempObj = createTempObject();
        projectUrlsRepository.save(tempObj);
        return buildResponse("success");
    }

    @RequestMapping(value = "/update", method = RequestMethod.GET)
    public ResponseEntity updateRecord() {
        ProjectUrls tempObj = createTempObject();
        tempObj.setUserName("updatedUser");
        tempObj.setId(1);
        projectUrlsRepository.save(tempObj);
        return buildResponse("success");
    }

    @RequestMapping(value = "/select", method = RequestMethod.GET)
    public ResponseEntity findAllRecord() {
        List<ProjectUrls> list = projectUrlsRepository.findAll();
        return buildResponse(list);
    }

    @RequestMapping(value = "/delete/{id}", method = RequestMethod.GET)
    public ResponseEntity deleteRecord(@PathVariable Integer id) {
        projectUrlsRepository.delete(id);
        return buildResponse("success");
    }

    private ProjectUrls createTempObject() {
        ProjectUrls obj = new ProjectUrls();
        obj.setUserName("user" + new Random().nextInt());
        obj.setPassword("password");
        obj.setOwner("chandra");
        obj.setProjectName("PMO");
        obj.setUrl("https://pmo.atlassian.net");
        return obj;
    }
}
