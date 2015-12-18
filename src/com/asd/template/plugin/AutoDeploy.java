package com.asd.template.plugin;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.Scanner;

/**
 * 自动部署工具
 * 
 */
public class AutoDeploy {

    // 旧文件的备份名称
    private String backUpPrefix_old = "old-";
    // 新文件的备份名称
    private String backUpPrefix_new = "new-";
    private String sourceWarFile;
    private String catalinaHome;
    private String loggerFile;
    private Shell  shell;

    public AutoDeploy(String configFileName) throws Exception {
        Properties argsMap = getProperties(configFileName);
        this.sourceWarFile = argsMap.getProperty("sourceWarFile");
        this.catalinaHome = argsMap.getProperty("catalinaHome");
        String host = argsMap.getProperty("host");
        String user = argsMap.getProperty("user");
        String password = argsMap.getProperty("password");
        this.loggerFile = argsMap.getProperty("loggerFile");
        String log = "args:\nhost:%s\nuser:%s\npassword:%s\nsourceWarFile:%s\ncatalinaHome:%s\nloggerFile:%s\n";
        System.out.println(String.format(log, host, user, password, sourceWarFile, catalinaHome, loggerFile));
        shell = new Shell(host, user, password);
    }

    // 文件传输；启动控制台输出
    public void redeploy() throws Exception {
        // 先放到home目录
        shell.sftp(sourceWarFile, ".");

        String appName = getAppName(sourceWarFile);
        String cmd = new StringBuilder(
                getCopyCmd(String.format("%s/webapps/%s", catalinaHome, appName), String.format("~/%s%s", backUpPrefix_old, appName)))//
                        .append(getCopyCmd(String.format("~/%s", appName), String.format("%s/webapps/", catalinaHome)))//
                        .append(getMoveCmd(String.format("~/%s", appName), String.format("~/%s%s", backUpPrefix_new, appName)))//
                        .append(getLoggerCmd(loggerFile))//
                        .toString();
        shell.shell(new ByteArrayInputStream(cmd.getBytes()), System.out);
    }

    public void manual(String selection) throws Exception {
        String appName = getAppName(sourceWarFile);
        String sourceFile = getSourceFile(selection, appName);
        String cmd = new StringBuilder(getCopyCmd(String.format("~/%s", sourceFile), String.format("%s/webapps/%s", catalinaHome, appName)))//
                .append(getLoggerCmd(loggerFile))//
                .toString();
        shell.shell(new ByteArrayInputStream(cmd.getBytes()), System.out);
    }

    public void shell() {
        shell.shell(null, null);
    }

    /**
     * 
     */
    public void log() {
        shell.shell(new ByteArrayInputStream(getLoggerCmd(loggerFile).getBytes()), System.out);
    }

    /**
     * 切割字符串的到war文件名称
     * 
     * @param sourceWarFile
     * @return
     */
    private String getAppName(String sourceWarFile) {
        int index = sourceWarFile.lastIndexOf("/") + 1;
        return sourceWarFile.substring(index);
    }

    public String getMoveCmd(String from, String to) {
        return String.format("mv %s %s\n", from, to);
    }

    public String getCopyCmd(String from, String to) {
        return String.format("cp %s %s\n", from, to);
    }

    /**
     * 打印日志
     * 
     * @param loggerFile2
     * @return
     */
    private String getLoggerCmd(String loggerFile) {
        return String.format("tail -f %s\n", loggerFile);
    }

    /**
     * @param selection
     * @return
     */
    private String getSourceFile(String selection, String appName) {
        Scanner scanner = new Scanner(System.in);
        try {
            while (true) {
                if ("old".equals(selection)) {
                    return "old-" + appName;
                }
                if ("new".equals(selection)) {
                    return "new-" + appName;
                }
                System.out.println("please input your selection to deploy:\t new ? old");
                selection = scanner.nextLine();
            }
        } finally {
            scanner.close();
        }
    }

    /**
     * @return
     */
    private static Properties getProperties(String fileName) throws Exception {
        Properties properties = new Properties();
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println(String.format("file %s not exists", fileName));
        }
        properties.load(new FileInputStream(file));
        return properties;
    }

}
