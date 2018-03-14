package com.citi.service.sms;

/**
 * Created by VALLA on 2018/1/8.
 */
public interface SmsService {
    void SendSms(String phoneNumber, String message) throws Exception;
}
