package com.razorthink.pmo.bean.reports.jira.greenhopper;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Contents {

    List<IssueGreenHopperPOJO> issuesNotCompletedInCurrentSprint = new ArrayList<>();
    List<IssueGreenHopperPOJO> puntedIssues = new ArrayList<>();
    Map<String, Boolean> issueKeysAddedDuringSprint = new HashMap<>();

    public List<IssueGreenHopperPOJO> getIssuesNotCompletedInCurrentSprint() {
        return issuesNotCompletedInCurrentSprint;
    }

    public void setIssuesNotCompletedInCurrentSprint(List<IssueGreenHopperPOJO> issuesNotCompletedInCurrentSprint) {
        this.issuesNotCompletedInCurrentSprint = issuesNotCompletedInCurrentSprint;
    }

    public List<IssueGreenHopperPOJO> getPuntedIssues() {
        return puntedIssues;
    }

    public void setPuntedIssues(List<IssueGreenHopperPOJO> puntedIssues) {
        this.puntedIssues = puntedIssues;
    }

    public Map<String, Boolean> getIssueKeysAddedDuringSprint() {
        return issueKeysAddedDuringSprint;
    }

    public void setIssueKeysAddedDuringSprint(Map<String, Boolean> issueKeysAddedDuringSprint) {
        this.issueKeysAddedDuringSprint = issueKeysAddedDuringSprint;
    }
}
