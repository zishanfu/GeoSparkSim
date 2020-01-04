package com.zishanfu.geosparksim.tools;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class FileOps {
    private static final Logger LOG = Logger.getLogger(FileOps.class);

    public void createDirectory(String directory) {
        try {
            File f = new File(directory);
            if (f.isDirectory()) {
                FileUtils.cleanDirectory(f);
                FileUtils.forceDelete(f);
            }
            FileUtils.forceMkdir(f);
        } catch (IOException e) {
            LOG.warn("Error happens when creating geosparksim output folder.", e);
        }
    }

    public boolean deleteDirectory(String directory) {
        File f = new File(directory);
        if (f.isDirectory()) {
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
