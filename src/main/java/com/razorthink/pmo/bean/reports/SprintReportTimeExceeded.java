package com.razorthink.pmo.bean.reports;


public class SprintReportTimeExceeded {

    private String issueKey;
    private String issueType;
    private String issueSummary;
    private String assignee;
    private String timeExceeded;
    private String status;

    public String getIssueKey() {
        return issueKey;
    }

    public void setIssueKey(String issueKey) {
        this.issueKey = issueKey;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getIssueSummary() {
        return issueSummary;
    }

    public void setIssueSummary(String issueSummary) {
        this.issueSummary = issueSummary;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTimeExceeded() {
        return timeExceeded;
    }

    public void setTimeExceeded(String timeExceeded) {
        this.timeExceeded = timeExceeded;
    }
}
