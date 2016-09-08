package com.razorthink.pmo.bean.projecturls;

import org.joda.time.DateTime;

public class Sprint {
    private Integer sprintId;
    private String sprintName;
    private String sprintState;
    private DateTime startDate;
    private DateTime endDate;
    private DateTime completeDate;

    public Integer getSprintId() {
        return sprintId;
    }

    public void setSprintId(Integer sprintId) {
        this.sprintId = sprintId;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String sprintName) {
        this.sprintName = sprintName;
    }

    public String getSprintState() {
        return sprintState;
    }

    public void setSprintState(String sprintState) {
        this.sprintState = sprintState;
    }

    public DateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(DateTime startDate) {
        this.startDate = startDate;
    }

    public DateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(DateTime endDate) {
        this.endDate = endDate;
    }

    public DateTime getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(DateTime completeDate) {
        this.completeDate = completeDate;
    }
}
