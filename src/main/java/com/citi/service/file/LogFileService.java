package com.citi.service.file;

import com.citi.model.PendingLog;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by VALLA on 2017/12/28.
 */
public interface LogFileService {
    void snapShotTargetFile(File folder, File targetFolder, Pattern pattern) throws IOException;

    List<PendingLog> scaningLog() throws IOException;

    void copyIssueLog(String issueFolderName) throws IOException;
}
