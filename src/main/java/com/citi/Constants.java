package com.citi;

/**
 * Created by VALLA on 2017/12/28.
 */
public interface Constants {
//  Email config
    String LOCAL_TEST = "LOCAL_TEST";
    String EDM_FROM_ADDR = "EDM_FROM_ADDR";
    String EDM_FROM_PERSON = "EDM_FROM_PERSON";
    String EDM_HOST = "EDM_HOST";
    String EDM_USR = "EDM_USR";
    String EDM_PWD = "EDM_PWD";

//  # Log scan
    String INTERVAL_TIME = "intervalTime";
    String POLITE_MODE = "politeMode";
//    scan time
    //    log path
    String LOG_FOLDER_PATH = "logFolderPath";
    String TEMP_FOLDER_PATH = "tempFolderPath";
    String ISSUE_LOG_FOLDER_PATH = "issueLogFolderPath";
//    pendingLimit
    String ALLOW_PENDING_LIMIT_NUMBER = "allowPendingLimit";
    String ALLOW_PENDING_LIMIT_TIME = "allowPendingTimeLimit";
    String EMAIL_SEND_TARGETS = "emailSendTargets";
    String SMS_SEND_TARGETS = "smsSendTargets";

    //special settings
    String OTHER_PERFIX = "otherScan";
    String OTHER_PATTERN = "pattern";
    String OTHER_ALLOWCOUNT = "allowCount";

    enum AlertType {
        Pending("COLA server over pending warning! please see more info from email.",
                "COLA server over pending warning",
                "COLA server over pending back to normal",
                "Pending situation is back to normal now."),
        SpecialSearch("COLA server found special search keywords over limit! please see more info from email.",
                "COLA server over found keyword warning",
                "COLA server over keyword warning back to normal",
                "Keywords finding is not over limit now.");

        private String alertSmsMsg;

        private String alertEmailTitle;

        private String backToNormalEmailTitle;

        private String backToNormalEmailMsg;

        AlertType(String alertSmsMsg, String alertEmailTitle, String backToNormalEmailTitle, String backToNormalEmailMsg) {
            this.alertSmsMsg = alertSmsMsg;
            this.alertEmailTitle = alertEmailTitle;
            this.backToNormalEmailTitle = backToNormalEmailTitle;
            this.backToNormalEmailMsg = backToNormalEmailMsg;
        }

        public String getAlertSmsMsg() {
            return this.alertSmsMsg;
        }

        public String getAlertEmailTitle() {
            return this.alertEmailTitle;
        }

        public String getBackToNormalEmailTitle() {
            return this.backToNormalEmailTitle;
        }

        public String getBackToNormalEmailMsg() {
            return this.backToNormalEmailMsg;
        }


    }

}
