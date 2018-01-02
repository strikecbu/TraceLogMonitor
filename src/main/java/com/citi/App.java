package com.citi;

import com.citi.model.PendingLog;
import com.citi.service.email.CCSimpleEmailService;
import com.citi.service.email.impl.CCSimpleEmailServiceImpl;
import com.citi.service.file.LogFileService;
import com.citi.service.file.impl.LogFileServiceImpl;
import com.citi.service.log.ParserTrace;
import com.citi.util.CapString;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * Created by VALLA on 2017/12/26.
 */
public class App {

    private static final Logger logger = Logger.getLogger(App.class);

    static Properties prop = new Properties();

    private LogFileService logFileService;

    private CCSimpleEmailService emailService;

    public static void main(String[] args) throws IOException {
        logger.debug("monitor start ...");
        //testing
        App testObj = new App();
        testObj.scanProcess();
    }

    public App(){
        URL resource = Thread.currentThread().getContextClassLoader().getResource("config.properties");
        try {
            prop.load(new InputStreamReader(resource.openStream(),"UTF-8"));
        } catch (IOException e) {
            logger.error("can not read config.properties!", e);
            e.printStackTrace();
        }
        this.emailService = new CCSimpleEmailServiceImpl(prop);
        this.logFileService = new LogFileServiceImpl(prop, new ParserTrace());
    }

    private void scanProcess() throws IOException {
        List<PendingLog> pendingLogs = logFileService.scaningLog();
        int count = pendingLogs.size();
        int allowLimit = Integer.parseInt(prop.getProperty(Constants.ALLOW_PENDING_LIMIT));
        boolean isOverAllow = count > allowLimit;

        if(isOverAllow){
            //TODO 保存該次檔案
            String issueLogFolderName = pendingLogs.get(0).getIssueLogFolderName();
            logFileService.copyIssueLog(issueLogFolderName);
            //TODO send notify
            this.sendEmailNotify(pendingLogs);
        }
    }

    private void sendEmailNotify(List<PendingLog> pendingLogs){
        String message = emailService.getMessageContent(pendingLogs);
        String emailTargets = prop.getProperty(Constants.EMAIL_SEND_TARGETS);
        String mailTitle = "COLA server over pending warning";
        logger.debug("sending notify mail...");
        for(String target : emailTargets.split(",")){
            if(CapString.isEmpty(target))
                continue;
            logger.debug("sending mail to : " + target);
            try{
                emailService.processEMail(target, mailTitle, message);
            } catch (Exception e){
                logger.error("sending mail to : " + target + " fail!");
                logger.error("reason : " + e);
            }
        }
        logger.debug("sending notify mail complete!");
    }
}
