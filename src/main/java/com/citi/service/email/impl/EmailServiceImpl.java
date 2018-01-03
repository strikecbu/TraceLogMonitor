package com.citi.service.email.impl;

import com.citi.Constants;
import com.citi.model.PendingLog;
import com.citi.service.email.CCSimpleEmailService;
import com.citi.service.email.EmailService;
import com.citi.util.CapString;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Properties;

/**
 * Created by VALLA on 2018/1/3.
 */
public class EmailServiceImpl implements EmailService{

    private Properties prop;

    private CCSimpleEmailService emailService;

    protected static Logger logger = Logger.getLogger(EmailServiceImpl.class);

    public EmailServiceImpl(Properties prop){
        this.prop = prop;
        this.emailService = new CCSimpleEmailServiceImpl(prop);
    }

    @Override
    public String getMessageContent(List<PendingLog> pendingLogs){
        if(pendingLogs.size() <= 0)
            return "";
        final String NEXT_LINE = "\n";
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

    public void sendEmailNotify(List<PendingLog> pendingLogs){
        String message = this.getMessageContent(pendingLogs);
        String mailTitle = "COLA server over pending warning";
        this.sendEmailNotify(mailTitle, message);
    }

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

}