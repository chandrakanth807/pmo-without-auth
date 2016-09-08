package com.razorthink.pmo.bean.reports.jira.greenhopper;

import com.razorthink.pmo.bean.reports.jira.greenhopper.timetracking.EstimateStat;

public class IssueGreenHopperPOJO {

    private String key;
    private String typeName;
    private String summary;
    private String statusName;
    private String assigneeName;
    private EstimateStat estimateStatistic;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getAssigneeName() {
        return assigneeName;
    }

    public void setAssigneeName(String assigneeName) {
        this.assigneeName = assigneeName;
    }

    public EstimateStat getEstimateStatistic() {
        return estimateStatistic;
    }

    public void setEstimateStatistic(EstimateStat estimateStatistic) {
        this.estimateStatistic = estimateStatistic;
    }
}
