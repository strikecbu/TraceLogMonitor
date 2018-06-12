package com.citi;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

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
                "COLA server over pending warning"),
        SpecialSearch("COLA server found special search over limit! please see more info from email.",
                "COLA server over found keyword warning");

        private String smsMsg;

        private String emailTitle;

        AlertType(String smsMsg, String emailTitle) {
            this.smsMsg = smsMsg;
            this.emailTitle = emailTitle;
        }

        public String getSmsMsg() {
            return this.smsMsg;
        }

        public String getEmailTitle() {
            return this.emailTitle;
        }
    }

}
