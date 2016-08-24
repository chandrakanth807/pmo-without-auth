package com.razorthink.pmo.bean.reports;


public class GenericReportResponse {
    private String downloadLink;
    private Object reportAsJson;

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }


    public Object getReportAsJson() {
        return reportAsJson;
    }

    public void setReportAsJson(Object reportAsJson) {
        this.reportAsJson = reportAsJson;
    }
}
