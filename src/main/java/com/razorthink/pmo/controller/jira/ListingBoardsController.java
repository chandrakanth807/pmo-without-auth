package com.razorthink.pmo.controller.jira;

import com.razorthink.pmo.bean.project_urls.RapidView;
import com.razorthink.pmo.commons.exceptions.WebappException;
import com.razorthink.pmo.controller.AbstractWebappController;
import com.razorthink.pmo.service.ListingBoardsService;
import com.razorthink.pmo.tables.ProjectUrls;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/rest/jira/")
public class ListingBoardsController extends AbstractWebappController{

    @Autowired
    ListingBoardsService listingBoardsService;

    @RequestMapping(value = "/projects/{projectUrlId}/boards", method = RequestMethod.GET)
    public ResponseEntity listProjects( @PathVariable Integer projectUrlId ) {
        try {
            List<RapidView> rapidViewList = listingBoardsService.getBoards(projectUrlId);
            return buildResponse(rapidViewList);
        } catch (WebappException e) {
            return buildErrorResponse(e);
        }
    }
}
