package com.citi.service.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class ParserTrace {

    public static final String CLASS_NAME = "nearest";
    public static final String START_TIME = "time";
    public static final String PENDING_TIME = "duration";

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        ParserTrace p = new ParserTrace();
        p.dumpWebContainer();
    }

    public Map<String, Map<String, String>> dumpWebContainer(String folderPath) {
        Map<String, Map<String, String>> error = new TreeMap<String, Map<String, String>>();
//        String path = "/Volumes/RamDisk/server1/INC0046565396(5)";
        String path = folderPath;
        File logPath = new File(path);
        File[] logFiles = logPath.listFiles();
        FileInputStream fis = null;
        BufferedReader br = null;
        for (File logFile : logFiles) {
            try {
                fis = new FileInputStream(logFile);
                br = new BufferedReader(new InputStreamReader(fis));
                String line = null;
                boolean recordNearest = true;
                boolean recordTime = true;
                String data = null;
                while ((line = br.readLine()) != null) {
                    int errorIdx = line.indexOf("MCWrapper id");
                    if (errorIdx == 2) {
                        data = line.substring(errorIdx + 13, line.indexOf("Managed connection")).trim();
                        Map<String, String> info = new HashMap<String, String>();
                        info.put("state", line.substring(line.indexOf("State:") + 6, line.indexOf("Thread Id: ")).trim());
                        if(line.indexOf("Thread Name: ") ==-1 || line.indexOf("Connections being")==-1)
                            continue;
                        info.put("name", line.substring(line.indexOf("Thread Name: ") + 13, line.indexOf("Connections being")).trim());
                        error.put(data, info);
                        recordNearest = false;
                        recordTime = false;
                    }
                    if ((!recordNearest || !recordTime) && data != null) {
                        Map<String, String> info = error.get(data);
                        if (line.indexOf("com.citibank.") >= 0) {
                            info.put("nearest", line.trim());
                            error.put(data, info);
                            recordNearest = true;
                        } else if (line.indexOf("Start time") >= 0) {
                            int start = line.indexOf("time inuse");
                            int sep = line.lastIndexOf("Time inuse");
                            info.put("time", line.substring(start + 14, sep).trim());
                            info.put("duration", line.substring(sep + 10, line.indexOf("(seconds)")).trim());
                            recordTime = true;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                    }
                }
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        System.out.println(error.size());
        for (Entry<String, Map<String, String>> e : error.entrySet()) {
            Map<String, String> info = e.getValue();
            System.out.println(e.getKey() + "," + info.get("name") + "," + info.get("state") + "," + info.get("nearest") + "," + info.get("time") + "," + info.get("duration"));
        }
        return error;
    }
}
