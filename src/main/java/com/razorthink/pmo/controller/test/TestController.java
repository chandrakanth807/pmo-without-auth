package com.razorthink.pmo.controller.test;

import com.razorthink.pmo.bean.MyBean;
import com.razorthink.pmo.commons.config.RestControllerRoute;
import com.razorthink.pmo.controller.AbstractWebappController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;


/*@ApiIgnore
@Api( value = "Test controller", description = "APIs for Test operations. Services that xxxxxx." )*/
@RestController
@RequestMapping(value = RestControllerRoute.TestController.ROUTE)
public class TestController extends AbstractWebappController {
    private final Logger logger = LoggerFactory.getLogger(TestController.class);

    /*@ApiOperation( value = "testing one service", notes = "just returns some string.", response = MyBean.class, code = 200 )*/
    @RequestMapping(value = RestControllerRoute.TestController.Subroute.TEST_ONE_SERVICE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity testOne() {
        try {
            MyBean myBean = new MyBean();
            myBean.setName("chandra");
            return buildResponse(myBean);
        } catch (Exception e) {
            logger.error("Exception occurred in TestController.testOne ", e);
            return buildErrorResponse(e);
        }
    }

    /*@ApiOperation( value = "Refine Operation...", notes = "fetch a script, needs authorization token as header.", response = MyBean.class, code = 200 )*/
    @RequestMapping(value = "/two", method = RequestMethod.GET)
    public ResponseEntity getScriptContent() {
        try {
            MyBean myBean = new MyBean();
            myBean.setName("chandra");
            return buildResponse(myBean);
        } catch (Exception e) {
            logger.error("Exception occurred in TestController.testOne ", e);
            return buildErrorResponse(e);
        }
    }

    @RequestMapping("/user")
    public Principal user(Principal principal) {
        return principal;
    }
}
