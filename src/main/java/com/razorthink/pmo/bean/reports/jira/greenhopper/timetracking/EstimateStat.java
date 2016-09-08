package com.razorthink.pmo.bean.reports.jira.greenhopper.timetracking;


public class EstimateStat {
    private String statFieldId;
    private StatFieldValue statFieldValue;

    public String getStatFieldId() {
        return statFieldId;
    }

    public void setStatFieldId(String statFieldId) {
        this.statFieldId = statFieldId;
    }

    public StatFieldValue getStatFieldValue() {
        return statFieldValue;
    }

    public void setStatFieldValue(StatFieldValue statFieldValue) {
        this.statFieldValue = statFieldValue;
    }
}
