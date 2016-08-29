package com.razorthink.pmo.commons.config;

public final class Constants {
    private Constants() {

    }

    public final class Jira {
        public static final String ERROR_FETCHING_CURRENT_USER = "error.fetching.current.user";
        public static final String MISSING_REQUIRED_PARAMETERS = "missing.required.parameters";
        public static final String AGGREGATE_PROJECT_REPORT_EXTENSION = "_aggregate_report.csv";
        public static final String CSV_DOWNLOAD_DIRECTORY_PATH_PROPERTY = "csv.filename";
        public static final String DOWNLOAD_LINK_BASE_PATH_PROPERTY = "csv.aliaspath";

        public final class ProjectUrlsController {
            private ProjectUrlsController() {
            }
            public static final String FAILED_ADD_PROJECT_URL = "failed.to.add.project.url";
            public static final String ADD_PROJECT_URL_SUCCESSFUL = "project.url.added.successfully";
            public static final String FAILED_UPDATE_PROJECT_URL = "failed.to.update.project.url";
            public static final String UPDATE_PROJECT_URL_SUCCESSFUL = "project.url.updated.successfully";
            public static final String DUMMY_PASSWORD = "dummy";
            public static final String DELETE_PROJECT_URL_SUCCESSFUL = "project.url.deleted.successfully";

        }

    }
}

