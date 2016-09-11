package com.razorthink.pmo.bean.reports;

public class SprintRetrospection {

    private String assignee;
    private Double estimatedHours;
    private Double availableHours;
    private Double surplus;
    private String buffer;
    private String efficiency;
    private Double timeTaken;
    private Integer totalTasks;
    private Integer incompletedIssues;

    public SprintRetrospection()
    {
        this.estimatedHours =0d;
        this.availableHours = 0d;
        this.surplus = 0d;
        this.buffer = "";
        this.efficiency = "";
        this.timeTaken = 0d;
        this.totalTasks = 0;
        this.incompletedIssues = 0;
    }

    public String getAssignee() {
        return assignee;
    }

    public void setAssignee(String assignee) {
        this.assignee = assignee;
    }

    public Double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(Double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public Double getAvailableHours() {
        return availableHours;
    }

    public void setAvailableHours(Double availableHours) {
        this.availableHours = availableHours;
    }

    public Double getSurplus() {
        return surplus;
    }

    public void setSurplus(Double surplus) {
        this.surplus = surplus;
    }

    public String getBuffer() {
        return buffer;
    }

    public void setBuffer(String buffer) {
        this.buffer = buffer;
    }

    public String getEfficiency() {
        return efficiency;
    }

    public void setEfficiency(String efficiency) {
        this.efficiency = efficiency;
    }

    public Double getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(Double timeTaken) {
        this.timeTaken = timeTaken;
    }

    public Integer getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(Integer totalTasks) {
        this.totalTasks = totalTasks;
    }

    public Integer getIncompletedIssues() {
        return incompletedIssues;
    }

    public void setIncompletedIssues(Integer incompletedIssues) {
        this.incompletedIssues = incompletedIssues;
    }
}
