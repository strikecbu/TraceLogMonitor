package com.citi.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Parser {

    private Map<Integer, Map<String, String>> containerThread = new TreeMap<Integer, Map<String, String>>();
    private Map<Integer, String> error = new TreeMap<Integer, String>();

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Parser p = new Parser();
        p.dumpWebContainer();
    }

    private void dumpWebContainer() {
        String path = "/Volumes/RamDisk/server1/INC0046565396(1)";
        File logPath = new File(path);
        File[] logFiles = logPath.listFiles();
        FileInputStream fis = null;
        BufferedReader br = null;
        for (File logFile : logFiles) {
            try {
                fis = new FileInputStream(logFile);
                br = new BufferedReader(new InputStreamReader(fis));
                String line = null;
                boolean record = true;
                int sn = -1;
                while ((line = br.readLine()) != null) {
                    int idx = line.indexOf("[WebContainer :");
                    if (idx >= 0) {
                        int name = Integer.parseInt(line.substring(idx + 15, line.indexOf("]", idx)).trim());
                        Map<String, String> time = containerThread.get(name);
                        if (time == null) {
                            time = new HashMap<String, String>();
                            time.put("start", line);
                        } else {
                            time.put("end", line);
                        }
                        containerThread.put(name, time);
                    }

                    int errorIdx = line.indexOf("WSVR0605W");
                    if (errorIdx >= 0) {
                        int i = line.indexOf("WebContainer", errorIdx);
                        String data = line.substring(i + 15, i + 18).trim();
                        data = data.replace("\"", "");
                        sn = Integer.parseInt(data);
                        error.put(sn, line.substring(1, 21));
                        record = false;
                    }
                    if (!record && sn != -1) {
                        if (line.indexOf("at com.citibank.") >= 0) {
                            error.put(sn, error.get(sn) + " " + line);
                            record = true;
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
        System.out.println(containerThread.size());
        for (Entry<Integer, Map<String, String>> e : containerThread.entrySet()) {
            Map<String, String> time = e.getValue();
            System.out.println(e.getKey() + ", " + time.get("start").substring(1, 21) + ", " + time.get("end").substring(1, 21));
        }
        System.out.println("--");
        for (Entry<Integer, String> e : error.entrySet()) {
            System.out.println(e.getKey() + ", " + e.getValue());
        }
    }
}
