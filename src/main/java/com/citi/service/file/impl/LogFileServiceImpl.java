package com.citi.service.file.impl;

import com.citi.Constants;
import com.citi.model.PendingLog;
import com.citi.service.file.LogFileService;
import com.citi.service.log.ParserTrace;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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

        if(!folder.exists())
            throw new FileNotFoundException();

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
    public List<PendingLog> scaningLog(){
        //TODOed 檢查資料夾
        //TODOed 將檔名符合的檔案copy到temp
        String fileSelectRegStr = this.getFilePattern();
        Pattern pattern = Pattern.compile(fileSelectRegStr);
        try {
            this.snapShotTargetFile(new File(this.folderPath), new File(this.tempFolderPath), pattern);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODOed 掃描temp -> pending log
        logger.debug("scanning log file...");
        Map<String, Map<String, String>> scanresult = parserTrace.dumpWebContainer(tempFolderPath);
        List<PendingLog> pendings = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_hhmmss");
        String timeStamp = format.format(new Date());
        //TODOed 組裝POJO
        for(String key : scanresult.keySet()){
            Map<String, String> map = scanresult.get(key);
            PendingLog pendingLog = new PendingLog();
            pendingLog.setPendingClass(map.get(ParserTrace.CLASS_NAME));
            pendingLog.setStartTime(map.get(ParserTrace.START_TIME));
            pendingLog.setInuseSec(map.get(ParserTrace.PENDING_TIME));
            pendingLog.setIssueLogFolderName(timeStamp);
            pendings.add(pendingLog);
        }
        logger.debug("pending files count: " + pendings.size());

        return pendings;
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

    private String getFilePattern(){
        //ex: trace_17.10.11_11.29.06.log
        String result = "";
        String baseStr = "trace_";
        Calendar calendar = Calendar.getInstance();
        String yearStr = String.valueOf(calendar.get(Calendar.YEAR));
        yearStr = yearStr.substring(2);

        result = result.concat("^").concat(baseStr).concat(yearStr).concat(".*").concat(".log$");
        return result;
    }

}
