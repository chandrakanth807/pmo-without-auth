package com.razorthink.pmo.bean.reports;

public class SprintRetrospection {

    private String assignee;
    private double estimatedHours;
    private double availableHours;
    private double surplus;
    private String buffer;
    private String efficiency;
    private double timeTaken;
    private int totalTasks;
    private int incompletedIssues;

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

    public double getEstimatedHours() {
        return estimatedHours;
    }

    public void setEstimatedHours(double estimatedHours) {
        this.estimatedHours = estimatedHours;
    }

    public double getAvailableHours() {
        return availableHours;
    }

    public void setAvailableHours(double availableHours) {
        this.availableHours = availableHours;
    }

    public double getSurplus() {
        return surplus;
    }

    public void setSurplus(double surplus) {
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

    public double getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(double timeTaken) {
        this.timeTaken = timeTaken;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getIncompletedIssues() {
        return incompletedIssues;
    }

    public void setIncompletedIssues(int incompletedIssues) {
        this.incompletedIssues = incompletedIssues;
    }
}
