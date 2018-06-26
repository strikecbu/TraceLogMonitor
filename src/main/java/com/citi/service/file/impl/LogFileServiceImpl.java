package com.citi.service.file.impl;

import com.citi.Constants;
import com.citi.model.PendingLog;
import com.citi.model.SpecialSearch;
import com.citi.service.file.LogFileService;
import com.citi.service.log.ParserTrace;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by VALLA on 2017/12/28.
 */
public class LogFileServiceImpl implements LogFileService {


    private final static Logger logger = Logger.getLogger(LogFileServiceImpl.class);

    private ParserTrace parserTrace;

    private String folderPath;

    private String tempFolderPath;

    private String issueLogFolderPath;

    public LogFileServiceImpl(Properties prop){
        this.parserTrace = new ParserTrace();
        this.folderPath = prop.getProperty(Constants.LOG_FOLDER_PATH);
        this.tempFolderPath = prop.getProperty(Constants.TEMP_FOLDER_PATH);
        this.issueLogFolderPath = prop.getProperty(Constants.ISSUE_LOG_FOLDER_PATH);
    }

    @Override
    public void snapShotTargetFile(File folder, File targetFolder, Pattern pattern) throws IOException {
        if(folder == null || targetFolder == null || pattern == null)
            throw new IllegalArgumentException();
       logger.debug("AbsolutePath : " + folder.getAbsolutePath());
        if(!folder.exists() && !folder.isDirectory())
            throw new FileNotFoundException();
        if(folder.listFiles().length == 0){
            logger.debug("no any files in folder...");
            throw new FileNotFoundException();
        }

        if(!targetFolder.exists()){
            targetFolder.mkdirs();
        } else {
            FileUtils.cleanDirectory(targetFolder);
        }

        logger.debug("file checking...");
        for(File file : folder.listFiles()){
            String fileName = file.getName();
            Matcher matcher = pattern.matcher(fileName);
            if(matcher.find()){
                logger.debug("select file: " + fileName);
                try {
                    FileUtils.copyFileToDirectory(file, targetFolder);
                } catch (IOException e){
                    logger.debug("select file is not available copy! pass it.");
                }
            }
        }
        logger.debug("[snapShotTargetFile] file checking complete!");
    }

    @Override
    public void snapShotTraceLog() throws IOException {
        //TODOed 檢查資料夾
        //TODOed 將檔名符合的檔案copy到temp
        String fileSelectRegStr = this.getFilePattern();
        Pattern pattern = Pattern.compile(fileSelectRegStr);
        logger.debug("folderPath: " + this.folderPath);
        logger.debug("tempFolderPath: " + this.tempFolderPath);
        this.snapShotTargetFile(new File(this.folderPath), new File(this.tempFolderPath), pattern);
    }

    @Override
    public List<PendingLog> scaningLog() throws IOException {
        //TODOed 掃描temp -> pending log
        logger.debug("scanning log file...");
        Map<String, Map<String, String>> scanresult = parserTrace.dumpWebContainer(tempFolderPath);
        List<PendingLog> pendings = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_hhmmss");
        String timeStamp = format.format(new Date());
        int maxPendingTime = 0;
        //TODOed 組裝POJO
        for(String key : scanresult.keySet()){
            Map<String, String> map = scanresult.get(key);
            PendingLog pendingLog = new PendingLog();
            pendingLog.setPendingClass(map.get(ParserTrace.CLASS_NAME));
            pendingLog.setStartTime(map.get(ParserTrace.START_TIME));
            pendingLog.setInuseSec(map.get(ParserTrace.PENDING_TIME));
            pendingLog.setIssueLogFolderName(timeStamp);
            int inuseSec;
            try {
                inuseSec = Integer.parseInt(map.get(ParserTrace.PENDING_TIME));
                if(inuseSec > maxPendingTime) {
                    maxPendingTime = inuseSec;
                }
            } catch (NumberFormatException e) {
                // do nothing...
            }
            pendings.add(pendingLog);
        }
        if(maxPendingTime > 0)
            logger.debug("max thread pending time: " + maxPendingTime + " sec");
        logger.debug("pending threads count: " + pendings.size());

        return pendings;
    }

    @Override
    public Map<SpecialSearch, List<String>> scaningLogBySpecialSearch(List<SpecialSearch> searchList) throws IOException {
        Map<SpecialSearch, List<String>> result = new HashMap<>();
        File tempFolder = new File(tempFolderPath);
        if(!tempFolder.exists() || !tempFolder.isDirectory() || tempFolder.listFiles() == null) {
            return result;
        }

        for (SpecialSearch specialSearch : searchList) {
            result.put(specialSearch, new ArrayList<String>());
        }

        for (File file : Objects.requireNonNull(tempFolder.listFiles())) {
            try (
                    FileInputStream fis = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader bur = new BufferedReader(isr)
            ) {
                String line;
                while ((line = bur.readLine()) != null ) {
                    for (SpecialSearch specialSearch : searchList) {
                        Pattern pattern = specialSearch.getPattern();
                        Matcher matcher = pattern.matcher(line);
                        if(matcher.find()) {
                            List<String> logs = result.get(specialSearch);
                            logs.add(line);
                        }
                    }
                }
            }

        }
        for (Map.Entry<SpecialSearch, List<String>> entry : result.entrySet()) {
            String key = entry.getKey().getPattern().toString();
            long size = entry.getValue().size();
            logger.debug("[scaningLogBySpecialSearch] pattern: " + key + ", total found: " + size);
        }
        return result;
    }

    @Override
    public void copyIssueLog(String issueFolderName) throws IOException {
        File tempFolder = new File(this.tempFolderPath);
        String issueLogFolderPath = this.issueLogFolderPath;
        issueLogFolderPath = issueLogFolderPath.concat(issueFolderName).concat(File.separator);
        File issueLogFolder = new File(issueLogFolderPath);
        logger.debug("now copy temp dir files...");
        if(!issueLogFolder.exists()){
            logger.debug("create new folder...");
            FileUtils.forceMkdir(issueLogFolder);
        }

        FileUtils.copyDirectory(tempFolder, issueLogFolder);
        logger.debug("copy temp dir files complete !");
    }

    @Override
    public void cleanIssueLogFolder() throws IOException {
        String issueLogFolderPath = this.issueLogFolderPath;
        File issueFolder = new File(issueLogFolderPath);
        if(issueFolder.exists() && issueFolder.isDirectory())
            FileUtils.cleanDirectory(issueFolder);
    }

    private String getFilePattern(){
        //ex: trace.log
        //ex: trace_17.10.11_11.29.06.log
        String result = "";
        String baseStr = "trace_";
        Calendar calendar = Calendar.getInstance();
        String yearStr = String.valueOf(calendar.get(Calendar.YEAR));
        yearStr = yearStr.substring(2);

        result = result.concat("^").concat(baseStr).concat(yearStr).concat(".*").concat("\\.log$");
        result = result.concat("|^trace\\.log$");
        return result;
    }

}
