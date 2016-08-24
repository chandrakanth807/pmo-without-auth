package com.razorthink.pmo.bean.reports;

/**
 * Created by root on 24/8/16.
 */
public class Credls {
    private String username;
    private String password;
    private String projectUrl;

    public Credls(String username, String password, String projectUrl) {
        this.username = username;
        this.password = password;
        this.projectUrl = projectUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }
}
