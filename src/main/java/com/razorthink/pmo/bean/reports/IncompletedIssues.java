package com.razorthink.pmo.bean.reports;

import net.rcarz.jiraclient.RestClient;
import net.rcarz.jiraclient.greenhopper.GreenHopperField;
import net.rcarz.jiraclient.greenhopper.SprintIssue;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

public class IncompletedIssues {

    private RestClient restclient = null;
    private List<SprintIssue> incompleteIssues = null;

    public IncompletedIssues(RestClient restclient, JSONObject json) {
        this.restclient = restclient;
        if (json != null) {
            deserialise(json);
        }
    }

    @SuppressWarnings("rawtypes")
    private void deserialise(JSONObject json) {
        Map map = json;
        incompleteIssues = GreenHopperField.getResourceArray(SprintIssue.class,
                map.get("issuesNotCompletedInCurrentSprint"), restclient);
    }

    public List<SprintIssue> getIncompleteIssues() {
        return incompleteIssues;
    }
}
