package com.citi.service.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by VALLA on 2017/12/28.
 */
public interface LogFileService {
    void snapShotTargetFile(File folder, File targetFolder, Pattern pattern) throws IOException;
}
