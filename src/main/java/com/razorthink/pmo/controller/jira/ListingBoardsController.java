package com.razorthink.pmo.controller.jira;

import com.razorthink.pmo.bean.projecturls.RapidView;
import com.razorthink.pmo.commons.config.RestControllerRoute;
import com.razorthink.pmo.controller.AbstractWebappController;
import com.razorthink.pmo.service.jira.ListingBoardsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping(value = RestControllerRoute.Jira.JIRA_BASE_ROUTE )
public class ListingBoardsController extends AbstractWebappController{

    private final Logger logger = LoggerFactory.getLogger(ListingBoardsController.class);

    @Autowired
    ListingBoardsService listingBoardsService;

    @RequestMapping(value = RestControllerRoute.Jira.ListingBoardsController.Subroute.GET_BOARDS, method = RequestMethod.GET)
    public ResponseEntity listBoards( @PathVariable(RestControllerRoute.Jira.ListingBoardsController.Subroute.URLParam.PROJECT_URL_ID) Integer projectUrlId ) {
        try {
            List<RapidView> rapidViewList = listingBoardsService.getBoards(projectUrlId);
            return buildResponse(rapidViewList);
        } catch (Exception e) {
            return buildErrorResponse(e);
        }
    }
}
