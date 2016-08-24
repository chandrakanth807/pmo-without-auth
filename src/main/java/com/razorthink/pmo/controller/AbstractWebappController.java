package com.razorthink.pmo.controller;

import com.razorthink.pmo.commons.controller.AbstractController;

public class AbstractWebappController extends AbstractController {

    /*protected String getCurrentUser() throws WebappException {
        try {
            Principal principal = (Principal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal == null || principal.getName() == null || principal.getName().trim().isEmpty()) {
                throw new WebappException(Constants.Webapp.ERROR_FETCHING_CURRENT_USER, HttpStatus.FORBIDDEN);
            }
            return principal.getName();
        } catch (WebappException we) {
            throw we;
        } catch (Exception e) {
            throw new WebappException(e);
        }

    }*/

}
