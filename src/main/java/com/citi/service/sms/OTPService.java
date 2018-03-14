package com.citi.service.sms;

public interface OTPService {
    /**
     * 傳送 SMS。
     * 
     * @param phoneNumber
     *            需要去 0 加 +886
     * @param message
     *            要傳送的訊息
     * @return SMS Server 回傳的結果，記在 AP log 即可，無論成功失敗都不影響交易。
     * @throws Exception
     */
    String sendOTPbySMS(String phoneNumber, String message) throws Exception;
}
