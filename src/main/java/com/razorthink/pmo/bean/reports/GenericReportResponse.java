package com.razorthink.pmo.bean.reports;


public class GenericReportResponse {
    private String downloadLink;
    private String reportAsJson;

    public String getDownloadLink() {
        return downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public String getReportAsJson() {
        return reportAsJson;
    }

    public void setReportAsJson(String reportAsJson) {
        this.reportAsJson = reportAsJson;
    }
}
