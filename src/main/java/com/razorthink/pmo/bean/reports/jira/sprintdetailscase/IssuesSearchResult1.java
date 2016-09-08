package com.razorthink.pmo.bean.reports.jira.sprintdetailscase;

import com.razorthink.pmo.bean.reports.jira.IssuePOJO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IssuesSearchResult1 {

    private Integer maxResults;
    private Integer startAt;
    private Integer total;
    private List<IssuePOJO> issues = new ArrayList<>();
    private Map<String,String> names = new HashMap<>();

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Integer getStartAt() {
        return startAt;
    }

    public void setStartAt(Integer startAt) {
        this.startAt = startAt;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public List<IssuePOJO> getIssues() {
        return issues;
    }

    public void setIssues(List<IssuePOJO> issues) {
        this.issues = issues;
    }

    public Map<String, String> getNames() {
        return names;
    }

    public void setNames(Map<String, String> names) {
        this.names = names;
    }
}
