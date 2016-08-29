package com.razorthink.pmo.bean.projecturls;

import java.util.List;


public class RapidView {

    private Integer rapidViewId;
    private String rapidViewName;
    private List<Sprint> sprintList;
    private List<SubProject> subProjectList;

    public List<Sprint> getSprintList() {
        return sprintList;
    }

    public void setSprintList(List<Sprint> sprintList) {
        this.sprintList = sprintList;
    }

    public List<SubProject> getSubProjectList() {
        return subProjectList;
    }

    public void setSubProjectList(List<SubProject> subProjectList) {
        this.subProjectList = subProjectList;
    }

    public Integer getRapidViewId() {
            return rapidViewId;
        }

        public void setRapidViewId(Integer rapidViewId) {
            this.rapidViewId = rapidViewId;
        }

        public String getRapidViewName() {
            return rapidViewName;
        }

        public void setRapidViewName(String rapidViewName) {
            this.rapidViewName = rapidViewName;
        }
}
