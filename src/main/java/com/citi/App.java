package com.citi;

import com.citi.model.PendingLog;
import com.citi.model.SpecialSearch;
import com.citi.service.email.EmailService;
import com.citi.service.email.impl.EmailServiceImpl;
import com.citi.service.file.LogFileService;
import com.citi.service.file.impl.LogFileServiceImpl;
import com.citi.service.sms.SmsService;
import com.citi.service.sms.impl.SmsServiceImpl;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;


/**
 * Created by VALLA on 2017/12/26.
 */
public class App implements Runnable{

    private static final Logger logger = Logger.getLogger(App.class);

    static Properties prop = new Properties();

    private LogFileService logFileService;

    private EmailService emailService;

    private SmsService smsService;

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
        this.smsService = new SmsServiceImpl(prop);
    }

    @Override
    public void run() {
        String scanTime = prop.getProperty(Constants.INTERVAL_TIME);
        try {
            long mills = Long.parseLong(scanTime) * 60 * 1000;
            String mailTitle = "COLA server warning system on!";
            String message = "monitor is on! You are in notify group!";
            emailService.sendEmailNotify(mailTitle, message);
            while (true){
                logger.debug("now start a new scan...");
                this.scanProcess();
                logger.debug("all process done! waiting next...");
                Thread.sleep(mills);
            }
        } catch (Exception e) {
            this.errorProcess(e);
        }
    }

    private List<SpecialSearch> getSpecialSearches() {
        String prop_patten = Constants.OTHER_PERFIX + "." + Constants.OTHER_PATTERN;
        String prop_allowCount = Constants.OTHER_PERFIX + "." + Constants.OTHER_ALLOWCOUNT;
        List<SpecialSearch> result = new ArrayList<>();
        int maxCount = 100;
        for (int i = 1; i <= maxCount; i++) {
            String patternStr = prop.getProperty(prop_patten + i);
            String allowCountStr = prop.getProperty(prop_allowCount + i);
            if (patternStr != null && !"".equals(patternStr) && allowCountStr != null && !"".equals(allowCountStr)) {
                int allowCount = Integer.parseInt(allowCountStr);
                SpecialSearch specialSearch = new SpecialSearch();
                Pattern pattern = Pattern.compile(patternStr);
                specialSearch.setPattern(pattern);
                specialSearch.setAllowPendingCount(allowCount);
                result.add(specialSearch);
                logger.debug("special search set. keyword: " + patternStr + ", allowCount: " + allowCount);
            }
        }
        return result;
    }

    private void scanProcess() throws IOException {
        logFileService.snapShotTraceLog();
        List<PendingLog> pendingLogs = logFileService.scaningLog();
        int count = pendingLogs.size();
        int allowLimitCount = Integer.parseInt(prop.getProperty(Constants.ALLOW_PENDING_LIMIT_NUMBER));
        int allowLimitTime = Integer.parseInt(prop.getProperty(Constants.ALLOW_PENDING_LIMIT_TIME));
        boolean isOverAllowCount = count > allowLimitCount;

        //TODO special search
        // 取得other keyword scan settings
        List<SpecialSearch> specialSearchList = this.getSpecialSearches();
        Map<SpecialSearch, List<String>> logsMap = logFileService.scaningLogBySpecialSearch(specialSearchList);
        if(logsMap.size() > 0)
            this.processSpecialSearchAlert(logsMap);

        if(isOverAllowCount) {
            this.processPendingAlert(pendingLogs);
            return;
        }
        //判斷是否有thread pending time超過
        List<PendingLog> overPendingTimeLogs = getOverPendingTimeLogs(pendingLogs, allowLimitTime);
        if(overPendingTimeLogs.size() > 0) {
            this.processPendingAlert(overPendingTimeLogs);
        }
    }

    private void processSpecialSearchAlert(Map<SpecialSearch, List<String>> logsMap) throws IOException {
        //TODOed 保存該次檔案
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_hhmmss");
        String issueLogFolderName = format.format(new Date());
        //clean old log file
        logFileService.cleanIssueLogFolder();
        logFileService.copyIssueLog(issueLogFolderName);
        //TODOed send notify
        emailService.sendEmailNotify(logsMap);
        // send sms notffy
        smsService.sendSms(Constants.AlertType.SpecialSearch);
    }

    private void processPendingAlert(List<PendingLog> pendingLogs) throws IOException {
        //TODOed 保存該次檔案
        String issueLogFolderName = pendingLogs.get(0).getIssueLogFolderName();
        //clean old log file
        logFileService.cleanIssueLogFolder();
        logFileService.copyIssueLog(issueLogFolderName);
        //TODOed send notify
        emailService.sendEmailNotify(pendingLogs);
        // send sms notffy
        smsService.sendSms(Constants.AlertType.Pending);
    }

    private List<PendingLog> getOverPendingTimeLogs(List<PendingLog> originalLogs, int allowSec) {
        List<PendingLog> result = new ArrayList<>();
        for (PendingLog pendingLog : originalLogs) {
            try {
                int logPendingTime = Integer.parseInt(pendingLog.getInuseSec());
                if(logPendingTime > allowSec)
                    result.add(pendingLog);
            } catch (NumberFormatException e) {
                //just skip this record...
            }
        }
        return result;
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

    private void errorProcess(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String errorMsg = sw.toString();
        String message = "monitor was down! See what happened: \n\n";
        String mailTitle = "COLA server warning system down!";
        emailService.sendEmailNotify(mailTitle, message.concat(errorMsg));
        logger.error(errorMsg);
        try {
            pw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            sw.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
