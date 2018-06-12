package com.citi.service.sms.impl;

import com.citi.Constants;
import com.citi.service.sms.OTPService;
import com.citi.service.sms.SmsService;
import com.citi.util.CapString;
import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by VALLA on 2018/1/8.
 */
public class SmsServiceImpl implements SmsService {

    private static Logger logger = Logger.getLogger(SmsServiceImpl.class);

    OTPService otpService;

    Properties prop;

    public SmsServiceImpl(Properties prop){
        this.prop = prop;
        otpService = new OTPServiceImpl(prop);
    }

    /**
     * @param phoneNumber must +8869xxxxxxxx or 09xxxxxxxx
     * @param message
     */
    @Override
    public void sendSms(String phoneNumber, String message) throws Exception {
        phoneNumber = tansPhoneNumber(phoneNumber);
        if(CapString.isEmpty(phoneNumber)){
            logger.error("phoneNumber : " + phoneNumber + " is illegal");
            return;
        }
        otpService.sendOTPbySMS(phoneNumber, message);
    }

    @Override
    public void sendSms(Constants.AlertType alertType) {
        String smsTargets = prop.getProperty(Constants.SMS_SEND_TARGETS);
        String message = alertType.getSmsMsg();
        for(String target : smsTargets.split(",")){
            if(CapString.isEmpty(target))
                continue;
            logger.debug("sending sms to : " + target);
            try{
                this.sendSms(target, message);
            } catch (Exception e){
                logger.error("sending sms to : " + target + " fail!");
                logger.error("reason : " + e);
            }
        }

    }

    private String tansPhoneNumber(String phoneNumber) {
        String phonePattern = "^\\+886[0-9]{9}$|^(09)[0-9]{8}$";
        Pattern pattern = Pattern.compile(phonePattern);
        Matcher matcher = pattern.matcher(phoneNumber);
        if (matcher.find()) {
            if (!CapString.isEmpty(matcher.group(1))) {
                phoneNumber = "+886".concat(phoneNumber.substring(1, phoneNumber.length()));
            }
            return phoneNumber;
        }
        return "";
    }
}

