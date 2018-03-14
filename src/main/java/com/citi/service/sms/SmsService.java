package com.citi.service.sms;

import com.citi.model.PendingLog;

import java.util.List;

/**
 * Created by VALLA on 2018/1/8.
 */
public interface SmsService {
    void sendSms(String phoneNumber, String message) throws Exception;

    void sendSms(List<PendingLog> pendingLogs);
}
