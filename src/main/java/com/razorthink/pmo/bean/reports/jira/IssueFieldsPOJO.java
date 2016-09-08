package com.razorthink.pmo.bean.reports.jira;

import java.util.List;



public class IssueFieldsPOJO {

    private IssueTypePOJO issuetype;
    private Double timespent;
    private ProjectPOJO project;
    private PriorityPOJO priority;
    private Double aggregatetimeoriginalestimate;
    private List<Object> issuelinks;
    private PersonPOJO assignee;
    private StatusPOJO status;
    private Double timeoriginalestimate;
    private String description;
    private Double aggregatetimeestimate;
    private String summary;
    private PersonPOJO creator;
    private List<Object> subtasks;
    private PersonPOJO reporter;
    private List<String> customfield_10003;


    public IssueTypePOJO getIssuetype() {
        return issuetype;
    }

    public void setIssuetype(IssueTypePOJO issuetype) {
        this.issuetype = issuetype;
    }

    public Double getTimespent() {
        return timespent;
    }

    public void setTimespent(Double timespent) {
        this.timespent = timespent;
    }

    public ProjectPOJO getProject() {
        return project;
    }

    public void setProject(ProjectPOJO project) {
        this.project = project;
    }

    public PriorityPOJO getPriority() {
        return priority;
    }

    public void setPriority(PriorityPOJO priority) {
        this.priority = priority;
    }

    public Double getAggregatetimeoriginalestimate() {
        return aggregatetimeoriginalestimate;
    }

    public void setAggregatetimeoriginalestimate(Double aggregatetimeoriginalestimate) {
        this.aggregatetimeoriginalestimate = aggregatetimeoriginalestimate;
    }

    public List<Object> getIssuelinks() {
        return issuelinks;
    }

    public void setIssuelinks(List<Object> issuelinks) {
        this.issuelinks = issuelinks;
    }

    public PersonPOJO getAssignee() {
        return assignee;
    }

    public void setAssignee(PersonPOJO assignee) {
        this.assignee = assignee;
    }

    public StatusPOJO getStatus() {
        return status;
    }

    public void setStatus(StatusPOJO status) {
        this.status = status;
    }

    public Double getTimeoriginalestimate() {
        return timeoriginalestimate;
    }

    public void setTimeoriginalestimate(Double timeoriginalestimate) {
        this.timeoriginalestimate = timeoriginalestimate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getAggregatetimeestimate() {
        return aggregatetimeestimate;
    }

    public void setAggregatetimeestimate(Double aggregatetimeestimate) {
        this.aggregatetimeestimate = aggregatetimeestimate;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public PersonPOJO getCreator() {
        return creator;
    }

    public void setCreator(PersonPOJO creator) {
        this.creator = creator;
    }

    public List<Object> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<Object> subtasks) {
        this.subtasks = subtasks;
    }

    public PersonPOJO getReporter() {
        return reporter;
    }

    public void setReporter(PersonPOJO reporter) {
        this.reporter = reporter;
    }

    public List<String> getCustomfield_10003() {
        return customfield_10003;
    }

    public void setCustomfield_10003(List<String> customfield_10003) {
        this.customfield_10003 = customfield_10003;
    }
}
