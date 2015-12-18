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
    // 打包后带发布的文件
    private String sourceWarFile;
    // tomcat－home
    private String catalinaHome;
    // 需要监控的日志文件
    private String loggerFile;
    private Shell  shell;

    public AutoDeploy(File configFile) throws Exception {
        Properties argsMap = getProperties(configFile);
        this.sourceWarFile = argsMap.getProperty("sourceWarFile");
        this.catalinaHome = argsMap.getProperty("catalinaHome");
        this.loggerFile = argsMap.getProperty("loggerFile");

        String host = argsMap.getProperty("host");
        String user = argsMap.getProperty("user");
        String password = argsMap.getProperty("password");

        String log = "args:\nhost:%s\nuser:%s\npassword:%s\nsourceWarFile:%s\ncatalinaHome:%s\nloggerFile:%s\n";
        System.out.println(String.format(log, host, user, password, sourceWarFile, catalinaHome, loggerFile));

        shell = new Shell(host, user, password);
    }

    /**
     * 1.文件传输<br/>
     * 2.文件备份<br/>
     * 3.启动控制台输出<br/>
     * 
     * @throws Exception
     */
    public void redeploy() throws Exception {
        // 先放到home目录
        shell.sftp(sourceWarFile, ".");

        String appName = getAppName(sourceWarFile);
        // 给旧的war包备份，加上前缀old-,放在~目录:
        // \cp oldApp ~/prefix-oldApp
        // 将新的war包移到webapps目录下:
        // \cp ~/newApp ....../webapps/
        // 将~目录下新的war包重命名，加上前缀new-:
        // mv ~/newAPp ~/prefix-newApp
        // 打印日志:
        // tail -f logFile
        String cmd = new StringBuilder(
                getCopyCmd(String.format("%s/webapps/%s", catalinaHome, appName), String.format("~/%s%s", backUpPrefix_old, appName)))//
                        .append(getCopyCmd(String.format("~/%s", appName), String.format("%s/webapps/", catalinaHome)))//
                        .append(getMoveCmd(String.format("~/%s", appName), String.format("~/%s%s", backUpPrefix_new, appName)))//
                        .append(getLoggerCmd(loggerFile))//
                        .toString();
        shell.shell(new ByteArrayInputStream(cmd.getBytes()), System.out);
    }

    /**
     * 将redeploy时备份在~目录的war包发布到webapp下
     * 
     * @param selection new:新的包,old:老的包
     * @throws Exception
     */
    public void manual(String selection) throws Exception {
        String appName = getAppName(sourceWarFile);
        String sourceFile = getSourceFile(selection, appName);
        String cmd = new StringBuilder(getCopyCmd(String.format("~/%s", sourceFile), String.format("%s/webapps/%s", catalinaHome, appName)))//
                .append(getLoggerCmd(loggerFile))//
                .toString();
        shell.shell(new ByteArrayInputStream(cmd.getBytes()), System.out);
    }

    /**
     * shell连接上服务器，手动输入命令
     */
    public void shell() {
        shell.shell(null, null);
    }

    /**
     * 只打印日志
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
        return String.format("mv -f %s %s\n", from, to);
    }

    public String getCopyCmd(String from, String to) {
        return String.format("\\cp %s %s\n", from, to);
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
     * 将输入的文件名转化为properties对象
     * 
     * @return properties对象
     * @throws 如果文件不存在，则抛出RuntimeException
     */
    private static Properties getProperties(File file) throws Exception {
        Properties properties = new Properties();
        if (file.exists()) {
            properties.load(new FileInputStream(file));
            return properties;
        }
        throw new RuntimeException(String.format("file %s not exists!", file.getName()));
    }

}
