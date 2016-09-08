package com.razorthink.pmo.bean.projecturls.jira;


import java.util.List;

public class BoardsResponsePOJO {

    private List<BoardValue> values;

    public List<BoardValue> getValues() {
        return values;
    }

    public void setValues(List<BoardValue> values) {
        this.values = values;
    }
}

