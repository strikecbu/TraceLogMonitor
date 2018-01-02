package com.citi.service.file.impl;

import com.citi.service.file.LogFileService;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by VALLA on 2017/12/28.
 */
public class LogFileServiceImpl implements LogFileService {

    private final static Logger logger = Logger.getLogger(LogFileServiceImpl.class);

    @Override
    public void snapShotTargetFile(File folder, File targetFolder, Pattern pattern) throws IOException {
        if(folder == null || targetFolder == null || pattern == null)
            throw new IllegalArgumentException();

        if(!folder.exists())
            throw new FileNotFoundException();

        if(!targetFolder.exists()){
            targetFolder.mkdirs();
        }

        logger.debug("[snapShotTargetFile] file checking...");
        for(File file : folder.listFiles()){
            String fileName = file.getName();
            Matcher matcher = pattern.matcher(fileName);
            if(matcher.find()){
                logger.debug("[snapShotTargetFile] select file: " + fileName);
                try {
                    FileUtils.copyFileToDirectory(file, targetFolder);
                } catch (IOException e){
                    logger.debug("[snapShotTargetFile] select file is not available copy! pass it.");
                }
            }
        }
    @Override
    public List<PendingLog> scaningLog(){
        //TODO 檢查資料夾
        //TODO 將檔名符合的檔案copy到temp
        String folderPath = prop.getProperty(Constants.LOG_FOLDER_PATH);
        String tempFolderPath = prop.getProperty(Constants.TEMP_FOLDER_PATH);

        String fileSelectRegStr = this.getFilePattern();
        Pattern pattern = Pattern.compile(fileSelectRegStr);
        try {
            this.snapShotTargetFile(new File(folderPath), new File(tempFolderPath), pattern);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //TODO 掃描temp -> pending log
        logger.debug("scanning log file...");
        Map<String, Map<String, String>> scanresult = parserTrace.dumpWebContainer(tempFolderPath);
        List<PendingLog> pendings = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_hhmmss");
        String timeStamp = format.format(new Date());
        //TODO 組裝POJO
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
}
