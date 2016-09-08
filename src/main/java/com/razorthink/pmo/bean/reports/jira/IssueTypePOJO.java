package com.razorthink.pmo.bean.reports.jira;

/**
 * Created by root on 31/8/16.
 */
public class IssueTypePOJO {
    private Integer id;
    private String description;
    private String name;
    private boolean subtask;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSubtask() {
        return subtask;
    }

    public void setSubtask(boolean subtask) {
        this.subtask = subtask;
    }
}
