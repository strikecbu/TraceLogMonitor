package com.citi.util;

import com.citi.model.PendingLog;
import com.citi.model.SpecialSearch;

import java.util.*;

/**
 * @author AndyChen
 * @version <ul>
 * <li>2018/6/27 AndyChen,new
 * </ul>
 * @since 2018/6/27
 */
public final class LogAlertBuffer {

    public static String PENDING_COUNT = "PENDING_COUNT";
    public static String PENDING_OVERTIME = "PENDING_OVERTIME";

    private static Properties prop;

    private static Map<String, SendRecord> records = new HashMap<>();

    private LogAlertBuffer() {

    }

    public static void setProp(Properties prop) {
        LogAlertBuffer.prop = prop;
    }

    public static boolean hasLastAlert(String type) {
        return records.get(type) != null;
    }

    public static Map<String, SendRecord> getAllRecords() {
        return records;
    }

    public static void removeRecord(String type) {
        records.remove(type);
    }

    public static boolean checkPendingCount(List<PendingLog> pendingLogs) {
        if(prop == null)
            throw new IllegalStateException("No prop found, please set prop at least once");

        int count = pendingLogs.size();
        return checkPending(PENDING_COUNT, count, 10);
    }

    public static boolean checkPendingOvertime(List<PendingLog> overPendingTimeLogs) {
        if(prop == null)
            throw new IllegalStateException("No prop found, please set prop at least once");
        int count = overPendingTimeLogs.size();
        return checkPending(PENDING_OVERTIME, count, 5);
    }

    public static boolean checkSpecialSearch(Map<SpecialSearch, List<String>> logsMap) {
        if(prop == null)
            throw new IllegalStateException("No prop found, please set prop at least once");
        Set<SpecialSearch> specialSearches = logsMap.keySet();
        Iterator<SpecialSearch> iterator = specialSearches.iterator();
        boolean result = false;
        while (iterator.hasNext()) {
            SpecialSearch specialSearch = iterator.next();
            String key = specialSearch.getPattern().toString();
            List<String> logs = logsMap.get(specialSearch);
            boolean isOverAllow = logs.size() > specialSearch.getAllowPendingCount();
            if(isOverAllow) {
                boolean isNeedSend = checkPending(key, logs.size(), 10);
                if(!isNeedSend) {
                    logsMap.remove(specialSearch);
                    continue;
                }
                result = true;
            }
        }
        return result;
    }

    /**
     * 檢查所要送的條件與前次發送的alert做比較
     * 如果最後傳送為前日，或是未傳送過，亦或是本次掃瞄出的數量超過前次百分之多少(@targetRate)，就會判定本次應送alert
     * @param type alert type
     * @param nowCount 目前所找出的數量
     * @param targetRate 與前次比較超過多少%要送
     * @return is need to send alert
     */
    private static boolean checkPending(String type, int nowCount, int targetRate) {
        SendRecord sendRecord = records.get(type);
        if(sendRecord == null) {
            sendRecord = new SendRecord();
            setRecord(sendRecord, nowCount);
            records.put(type, sendRecord);
            return true;
        } else {
            Calendar lastDate = Calendar.getInstance();
            lastDate.setTime(new Date(sendRecord.getSendTime()));
            Calendar today = Calendar.getInstance();
            if((today.get(Calendar.YEAR) - lastDate.get(Calendar.YEAR)) > 0 ||
                    (today.get(Calendar.DAY_OF_YEAR) - lastDate.get(Calendar.DAY_OF_YEAR) > 0)) {
                setRecord(sendRecord, nowCount);
                return true;
            }
            double diffCount = nowCount - sendRecord.getLastCount();
            boolean isNeedToSend = (diffCount / sendRecord.getLastCount()) * 100 > targetRate;
            if(isNeedToSend) {
                setRecord(sendRecord, nowCount);
                return true;
            }
        }
        return false;
    }

    private static void setRecord(SendRecord sendRecord, int count) {
        sendRecord.setSendTime(new Date().getTime());
        sendRecord.setLastCount(count);
    }

    public static class SendRecord {
        private long sendTime;
        private int lastCount;

        private long getSendTime() {
            return sendTime;
        }

        private void setSendTime(long sendTime) {
            this.sendTime = sendTime;
        }

        private int getLastCount() {
            return lastCount;
        }

        private void setLastCount(int lastCount) {
            this.lastCount = lastCount;
        }
    }
}
