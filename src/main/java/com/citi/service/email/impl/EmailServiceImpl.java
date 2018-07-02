package com.citi.service.email.impl;

import com.citi.Constants;
import com.citi.model.PendingLog;
import com.citi.model.SpecialSearch;
import com.citi.service.email.CCSimpleEmailService;
import com.citi.service.email.EmailService;
import com.citi.util.CapString;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by VALLA on 2018/1/3.
 */
public class EmailServiceImpl implements EmailService{

    private Properties prop;

    private CCSimpleEmailService emailService;

    private static Logger logger = Logger.getLogger(EmailServiceImpl.class);

    public EmailServiceImpl(Properties prop){
        this.prop = prop;
        this.emailService = new CCSimpleEmailServiceImpl(prop);
    }

    @Override
    public String getMessageContent(List<PendingLog> pendingLogs){
        if(pendingLogs.size() <= 0)
            return "";
        final String NEXT_LINE = "\n<br/>";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Warning!! Server pending is over allow!!");
        stringBuilder.append(NEXT_LINE).append(NEXT_LINE);
        String issueFolderName = pendingLogs.get(0).getIssueLogFolderName();
        stringBuilder.append("issue folder name: ").append(issueFolderName);
        stringBuilder.append(NEXT_LINE).append(NEXT_LINE);

        stringBuilder.append("<< Pending Classes >>").append(NEXT_LINE);
        for(PendingLog pendingLog : pendingLogs){
            stringBuilder
                    .append(pendingLog.getPendingClass())
                    .append(NEXT_LINE)
                    .append("pending time: ")
                    .append(pendingLog.getInuseSec())
                    .append(" seconds")
                    .append(NEXT_LINE);
        }
        return stringBuilder.toString();
    }
    @Override
    public String getMessageContent(Map<SpecialSearch, List<String>> logsMap){
        final String NEXT_LINE = "\n<br/>";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Warning!! SpecialSearch is over allow!!");
        stringBuilder.append(NEXT_LINE).append(NEXT_LINE);
        stringBuilder.append("go to issueLog folder and find out what happen!");
        stringBuilder.append(NEXT_LINE).append(NEXT_LINE);

        for (Map.Entry<SpecialSearch, List<String>> entry : logsMap.entrySet()) {
            SpecialSearch specialSearch = entry.getKey();
            List<String> logs = entry.getValue();
            if(logs.size() == 0) {
                continue;
            }
            stringBuilder.append("<h3 style=\"color:red;\">Pattern: " + specialSearch.getPattern() + "</h3>");
            stringBuilder.append("found records count: " + logs.size() + ", following below:" + NEXT_LINE);
            for (String log : logs) {
                stringBuilder.append(log + NEXT_LINE);
            }
            stringBuilder.append(NEXT_LINE);
        }
        return stringBuilder.toString();
    }

    @Override
    public void sendEmailNotify(Map<SpecialSearch, List<String>> logsMap){
        String message = this.getMessageContent(logsMap);
        String mailTitle = Constants.AlertType.SpecialSearch.getAlertEmailTitle();
        this.sendEmailNotify(mailTitle, message);
    }

    @Override
    public void sendEmailNotify(List<PendingLog> pendingLogs){
        String message = this.getMessageContent(pendingLogs);
        String mailTitle = Constants.AlertType.Pending.getAlertEmailTitle();
        this.sendEmailNotify(mailTitle, message);
    }

    @Override
    public void sendEmailNotify(String mailTitle, String message){
        String emailTargets = prop.getProperty(Constants.EMAIL_SEND_TARGETS);
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

    @Override
    public void sendBackNormalEmail(Constants.AlertType alertType) {
        this.sendEmailNotify(alertType.getBackToNormalEmailTitle(), alertType.getBackToNormalEmailMsg());
    }

}
