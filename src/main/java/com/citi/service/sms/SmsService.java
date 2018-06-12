package com.citi.service.sms;

import com.citi.Constants;

/**
 * Created by VALLA on 2018/1/8.
 */
public interface SmsService {
    void sendSms(String phoneNumber, String message) throws Exception;

    void sendSms(Constants.AlertType alertType);
}
