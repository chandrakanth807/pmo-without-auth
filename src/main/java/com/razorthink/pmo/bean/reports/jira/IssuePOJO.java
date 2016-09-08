package com.razorthink.pmo.bean.reports.jira;


public class IssuePOJO {

    private Integer id;
    private String key;
    private IssueFieldsPOJO fields;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public IssueFieldsPOJO getFields() {
        return fields;
    }

    public void setFields(IssueFieldsPOJO fields) {
        this.fields = fields;
    }
}
