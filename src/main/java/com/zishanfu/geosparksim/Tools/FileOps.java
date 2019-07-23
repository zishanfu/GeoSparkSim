package com.zishanfu.geosparksim.Tools;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class FileOps {

    public void createDirectory(String directory) {
        try {
            File f = new File(directory);
            if(f.isDirectory()) {
                FileUtils.cleanDirectory(f);
                FileUtils.forceDelete(f);
            }
            FileUtils.forceMkdir(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteDirectory(String directory){
        File f = new File(directory);
        if(f.isDirectory()) {
            try {
                FileUtils.cleanDirectory(f);
                FileUtils.forceDelete(f);
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

}

