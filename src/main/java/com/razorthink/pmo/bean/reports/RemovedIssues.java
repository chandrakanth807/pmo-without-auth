package com.razorthink.pmo.bean.reports;

import net.rcarz.jiraclient.RestClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperField;
import net.rcarz.jiraclient.greenhopper.SprintIssue;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class RemovedIssues {
    private List<SprintIssue> puntedIssues = null;
    private Set<String> issuesAdded = null;
    private RestClient restclient = null;

    public RemovedIssues(RestClient restclient, JSONObject json) {
        this.restclient = restclient;
        if (json != null) {
            deserialise(json);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void deserialise(JSONObject json) {

        Map map = json;
        puntedIssues = GreenHopperField.getResourceArray(SprintIssue.class, map.get("puntedIssues"), restclient);
        Map m1 = (Map) map.get("issueKeysAddedDuringSprint");
        issuesAdded = m1.keySet();
    }

    public List<SprintIssue> getPuntedIssues() {
        return puntedIssues;
    }

    public Set<String> getIssuesAdded() {
        return issuesAdded;
    }
}
