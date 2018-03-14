package com.citi.service.sms.impl;

import com.citi.service.sms.OTPService;
import com.citi.service.sms.SmsService;
import com.citi.util.CapString;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by VALLA on 2018/1/8.
 */
public class SmsServiceImpl implements SmsService {

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
    public void SendSms(String phoneNumber, String message) throws Exception {
        phoneNumber = tansPhoneNumber(phoneNumber);
        if(CapString.isEmpty(phoneNumber))
            throw new IllegalArgumentException("phoneNumber is illegal");

        otpService.sendOTPbySMS(phoneNumber, message);
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

