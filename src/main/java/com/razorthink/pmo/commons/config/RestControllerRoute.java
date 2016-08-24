package com.razorthink.pmo.commons.config;


public final class RestControllerRoute {
    public static final String REST_BASE_ROUTE = "/rest/";

    public class TestController {

        private TestController() {
        }

        public static final String ROUTE = REST_BASE_ROUTE + "/test";

        public class Subroute {

            public static final String TEST_ONE_SERVICE = "/one";

            private Subroute() {
            }
            /*public static final String GET_TRANSFORMATION_HISTORY = "/transformation/{" + URLParam.FILE_ID + "}";*/

            public class URLParam {

                private URLParam() {
                }

                /*public static final String FILE_ID = "fileId";
                public static final String HISTORY_ID = "historyId";*/
            }
        }
    }

}
