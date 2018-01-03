package com.citi;

import com.citi.model.PendingLog;
import com.citi.service.email.EmailService;
import com.citi.service.email.impl.EmailServiceImpl;
import com.citi.service.file.LogFileService;
import com.citi.service.file.impl.LogFileServiceImpl;
import com.citi.service.log.ParserTrace;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Properties;

/**
 * Created by VALLA on 2017/12/26.
 */
public class App implements Runnable{

    private static final Logger logger = Logger.getLogger(App.class);

    static Properties prop = new Properties();

    private LogFileService logFileService;

    private EmailService emailService;

    public static void main(String[] args) throws IOException {
        logger.debug("monitor start ...");
        App testObj = new App();
        Thread thread = new Thread(testObj);
        thread.start();
    }

    public App(){
        this.loadProperties();
        this.emailService = new EmailServiceImpl(prop);
        this.logFileService = new LogFileServiceImpl(prop);
    }

    @Override
    public void run() {
        String scanTime = prop.getProperty(Constants.INTERVAL_TIME);
        try {
            long mills = Long.parseLong(scanTime) * 60 * 1000;
            String mailTitle = "COLA server over pending warning";
            String message = "monitor is on! You are in notify group!";
            emailService.sendEmailNotify(mailTitle, message);
            while (true){
                logger.debug("now start a new scan...");
                this.scanProcess();
                logger.debug("all process done!");
                Thread.sleep(mills);
            }
        } catch (Exception e) {
            logger.error(e);
            e.printStackTrace();
        }

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
            emailService.sendEmailNotify(pendingLogs);
        }
    }

    private void loadProperties(){
        File jarUse = new File("./config.properties");
        if(jarUse.exists()){
            logger.debug("loading prod properties...");
            try {
                this.prop.load(new InputStreamReader(new FileInputStream(jarUse),"UTF-8"));
                logger.debug("loading prod success!");
                return;
            } catch (IOException e) {
                logger.error("can not read config.properties in Prod!", e);
                e.printStackTrace();
            }
        }
        URL resource = Thread.currentThread().getContextClassLoader().getResource("config.properties");
        try {
            this.prop.load(new InputStreamReader(resource.openStream(),"UTF-8"));
        } catch (IOException e) {
            logger.error("can not read config.properties!", e);
            e.printStackTrace();
        }
    }

}
