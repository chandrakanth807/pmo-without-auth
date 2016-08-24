package com.razorthink.pmo.bean.reports;

import javax.validation.constraints.NotNull;

public class BasicReportRequestParams {
    private String sprintName;
    private String rapidViewName;
    private String subProjectName;
    private Integer sprintId;
    private Integer rapidViewId;
    private Integer subProjectId;
    @NotNull
    private Integer projectUrlId;

    public Integer getProjectUrlId() {
        return projectUrlId;
    }

    public void setProjectUrlId(Integer projectUrlId) {
        this.projectUrlId = projectUrlId;
    }

    public String getSprintName() {
        return sprintName;
    }

    public void setSprintName(String sprintName) {
        this.sprintName = sprintName;
    }

    public String getRapidViewName() {
        return rapidViewName;
    }

    public void setRapidViewName(String rapidViewName) {
        this.rapidViewName = rapidViewName;
    }

    public String getSubProjectName() {
        return subProjectName;
    }

    public void setSubProjectName(String subProjectName) {
        this.subProjectName = subProjectName;
    }

    public Integer getSprintId() {
        return sprintId;
    }

    public void setSprintId(Integer sprintId) {
        this.sprintId = sprintId;
    }

    public Integer getRapidViewId() {
        return rapidViewId;
    }

    public void setRapidViewId(Integer rapidViewId) {
        this.rapidViewId = rapidViewId;
    }

    public Integer getSubProjectId() {
        return subProjectId;
    }

    public void setSubProjectId(Integer subProjectId) {
        this.subProjectId = subProjectId;
    }
}
