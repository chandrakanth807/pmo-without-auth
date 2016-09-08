package com.razorthink.pmo.bean.projecturls.jira;

import java.util.List;

public class SubProjectsResponse {
    private List<SubProjectValue> values;

    public List<SubProjectValue> getValues() {
        return values;
    }

    public void setValues(List<SubProjectValue> values) {
        this.values = values;
    }
}
