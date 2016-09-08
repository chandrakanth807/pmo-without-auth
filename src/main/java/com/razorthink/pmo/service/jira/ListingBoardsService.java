package com.razorthink.pmo.service.jira;

import com.razorthink.pmo.bean.projecturls.RapidView;
import com.razorthink.pmo.commons.config.Constants;
import com.razorthink.pmo.commons.exceptions.WebappException;
import com.razorthink.pmo.repositories.ProjectUrlsRepository;
import com.razorthink.pmo.tables.ProjectUrls;
import com.razorthink.pmo.utils.JiraRestUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ListingBoardsService {

    @Autowired
    private ProjectUrlsRepository projectUrlsRepository;

    public List<RapidView> getBoards( Integer projectUrlId) throws WebappException {
        if(projectUrlId == null)
            throw new WebappException(Constants.Jira.MISSING_PROJECT_URL_ID);
        ProjectUrls projectUrl = projectUrlsRepository.findOne(projectUrlId);
        return JiraRestUtil.getBoards(projectUrl);
    }
}
