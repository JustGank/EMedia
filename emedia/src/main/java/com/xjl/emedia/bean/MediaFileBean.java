package com.xjl.emedia.bean;

import java.io.File;

public class MediaFileBean {

    public String folderPath;

    public String folderName;

    public int num;

    public String coverFilePath;

    public MediaFileBean(String folderPath) {
        this.folderPath = folderPath;
        this.folderName = this.folderPath.substring(this.folderPath.lastIndexOf(File.separator)+1);
    }

}
