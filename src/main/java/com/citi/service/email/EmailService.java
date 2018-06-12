package com.citi.service.email;

import com.citi.model.PendingLog;
import com.citi.model.SpecialSearch;

import java.util.List;
import java.util.Map;

/**
 * Created by VALLA on 2018/1/3.
 */
public interface EmailService {

    String getMessageContent(List<PendingLog> pendingLogs);

    String getMessageContent(Map<SpecialSearch, List<String>> logsMap);

    void sendEmailNotify(Map<SpecialSearch, List<String>> logsMap);

    void sendEmailNotify(List<PendingLog> pendingLogs);

    void sendEmailNotify(String mailTitle, String message);

}
