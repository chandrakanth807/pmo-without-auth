package com.razorthink.pmo.commons.config;


public final class RestControllerRoute {
    public static final String REST_BASE_ROUTE = "/rest";


    public class Jira {
        public static final String JIRA_BASE_ROUTE = REST_BASE_ROUTE + "/jira";

        public class ProjectUrlsController {

            private ProjectUrlsController() {
            }

            public class Subroute {

                private Subroute() {
                }

                public static final String PROJECT_URLS = "/projects";
                public static final String DELETE_PROJECT_URL = "/projects/{" + URLParam.PROJECT_URL_ID + "}";

                public class URLParam {

                    private URLParam() {
                    }

                    public static final String PROJECT_URL_ID = "id";
                }
            }
        }

        public class ListingBoardsController {

            private ListingBoardsController() {
            }

            public class Subroute {

                private Subroute() {
                }

                public static final String GET_BOARDS = "/projects/{" + URLParam.PROJECT_URL_ID + "}/boards";

                public class URLParam {

                    private URLParam() {
                    }

                    public static final String PROJECT_URL_ID = "projectUrlId";
                }
            }
        }

        public class ReportsController {

            private ReportsController() {
            }

            public class Subroute {

                private Subroute() {
                }

                public static final String GET_AGGREGATE_PROJ_REPORT = "/projects/{" + URLParam.PROJECT_URL_ID + "}/boards/{" + URLParam.RAPID_VIEW_NAME + "}/subProject/{" + URLParam.SUB_PROJECT_NAME + "}/reports/board-summary";
                public static final String GET_SPRINT_MINIMAL_REPORT = "/projects/{"+URLParam.PROJECT_URL_ID+"}/boards/{"+URLParam.RAPID_VIEW_NAME+"}/sprint/{"+URLParam.SPRINT_NAME+"}/subProject/{"+URLParam.SUB_PROJECT_NAME+"}/reports/generic";
                public static final String GET_SPRINT_RETROSPECTION_REPORT = "/projects/{"+URLParam.PROJECT_URL_ID+"}/boards/{"+URLParam.RAPID_VIEW_NAME+"}/sprints/{"+URLParam.SPRINT_NAME+"}/subProject/{"+URLParam.SUB_PROJECT_NAME+"}/reports/sprint-retrospection";
                public static final String GET_TIME_GAINED_SPRINT_REPORT = "/projects/{"+URLParam.PROJECT_URL_ID+"}/boards/{"+URLParam.RAPID_VIEW_NAME+"}/sprint/{"+URLParam.SPRINT_NAME+"}/subProject/{"+URLParam.SUB_PROJECT_NAME+"}/reports/time-gained-sprint-report";
                public static final String GET_TIME_EXCEEDED_SPRINT_REPORT = "/projects/{"+URLParam.PROJECT_URL_ID+"}/boards/{"+URLParam.RAPID_VIEW_NAME+"}/sprint/{"+URLParam.SPRINT_NAME+"}/subProject/{"+URLParam.SUB_PROJECT_NAME+"}/reports/time-exceeded-tickets-sprint-report";

                public class URLParam {

                    private URLParam() {
                    }

                    public static final String PROJECT_URL_ID = "projectUrlId";
                    public static final String RAPID_VIEW_NAME = "rapidViewName";
                    public static final String SUB_PROJECT_NAME = "subProjectName";
                    public static final String SPRINT_NAME = "sprintName";

                }
            }
        }

    }
}
