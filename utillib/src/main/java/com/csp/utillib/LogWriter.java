package com.csp.utillib;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Description: 日志文件打印
 * <p>Create Date: 2017/7/14
 * <p>Modify Date: 2017/09/18
 *
 * @author csp
 * @version 1.0.1
 * @since AndroidUtils 1.0.0
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class LogWriter {
    private boolean isWrite = BuildConfig.DEBUG; // true: 允许写日志
    private int level; // 日志优先级
    private File file;

    private final static int EOF = -1;
    private final static int BUFFER_LENGTH = 8192; // 8 KB

    public LogWriter(File file) {
        this(file, Log.ERROR);
    }

    public LogWriter(File file, int level) {
        this.file = file;
        this.level = level;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void run() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    file.getParentFile().mkdirs();
                    if (!file.exists())
                        file.createNewFile();

                    write(file);
                } catch (IOException e) {
                    LogCat.printStackTrace(e);
                }
            }
        }).start();
    }

    /**
     * 日志记录是否有效
     */
    public boolean isValid() {
        return file.exists() && isWrite;
    }

    /**
     * 停止日志编写
     */
    public void quit() {
        isWrite = false;
    }

    /**
     * 日志文件写入(二进制方式)
     *
     * @param dest 目标文件
     * @throws IOException if an I/O error occurs.
     */
    private void write(File dest) throws IOException {
        Process process = null;
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        try {
            String cmd = getLogcatCmd();
            process = Runtime.getRuntime().exec(cmd);
            bis = new BufferedInputStream(process.getInputStream());

            int len;
            byte[] bArr = new byte[BUFFER_LENGTH];
            bos = new BufferedOutputStream(new FileOutputStream(dest, true));
            while (isValid() && (len = bis.read(bArr)) != EOF) {
                bos.write(bArr, 0, len);
                bos.flush();
            }
        } finally {
            if (bis != null)
                bis.close();

            if (bos != null)
                bos.close();

            if (process != null)
                process.destroy();
        }
    }

    /**
     * 获取[log]的命令行
     */
    private String getLogcatCmd() {
        String cmd = "logcat -v time *:";
        switch (level) {
            case Log.ERROR:
                return cmd + 'E';
            case Log.WARN:
                return cmd + 'W';
            case Log.INFO:
                return cmd + 'I';
            case Log.DEBUG:
                return cmd + 'D';
            default:
                return cmd + 'V';
        }
    }
}
