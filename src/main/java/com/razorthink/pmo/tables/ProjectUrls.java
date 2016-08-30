package com.razorthink.pmo.tables;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Entity
@Table(name = "projecturls", catalog = "pmo")
public class ProjectUrls implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "project_id", nullable = false)
    private Integer id;

    @Column(name = "username", nullable = false, length = 90)
    @NotNull
    private String userName;

    @Column(name = "password", nullable = false, length = 45)
    @NotNull
    private String password;


    @Column(name = "project_url", nullable = false, length = 450)
    @NotNull
    private String url;

    @Column(name = "project_name", nullable = false, length = 45)
    @NotNull
    private String projectName;

    @Column(name = "created_by", nullable = false, length = 90)
    @NotNull
    private String owner;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
