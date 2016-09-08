package com.razorthink.pmo.bean.reports.jira;

import java.util.ArrayList;
import java.util.List;

public class IssuesSearchResult {

    private Integer maxResults;
    private Integer startAt;
    private Integer total;
    private List<IssuePOJO> issues = new ArrayList<>();

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
}
