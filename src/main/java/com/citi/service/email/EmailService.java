package com.citi.service.email;

import com.citi.model.PendingLog;

import java.util.List;

/**
 * Created by VALLA on 2018/1/3.
 */
public interface EmailService {

    String getMessageContent(List<PendingLog> pendingLogs);

    void sendEmailNotify(List<PendingLog> pendingLogs);

    void sendEmailNotify(String mailTitle, String message);

}
