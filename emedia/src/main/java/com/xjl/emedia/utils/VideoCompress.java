package com.xjl.emedia.utils;

/**
 * Created by x33664 on 2019/2/15.
 * 内核使用FFmpeg 压缩时间有待优化
 */

public class VideoCompress {

    private String currentInputVideoPath = "";

    private String currentOutputVideoPath = "";

    private String cmd = "-y -i " + currentInputVideoPath + " -strict -2 -vcodec libx264 -preset ultrafast " +
            "-crf 24 -acodec aac -ar 44100 -ac 2 -b:a 96k -s 640x480 -aspect 16:9 " + currentOutputVideoPath;

    /**
     * 此方法是覆盖原视频的方法
     */
    public VideoCompress(String currentInputVideoPath) {
        this.currentInputVideoPath = currentInputVideoPath;
    }

    /**
     * 此方法是制定输出路径的方法 不覆盖原本视频
     */
    public VideoCompress(String currentInputVideoPath, String currentOutputVideoPath) {
        this(currentInputVideoPath);
        this.currentOutputVideoPath = currentOutputVideoPath;
    }


}

