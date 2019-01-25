package com.zishanfu.vistrips.tools;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

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
	
}
