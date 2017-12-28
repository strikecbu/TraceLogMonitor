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
    }
}
