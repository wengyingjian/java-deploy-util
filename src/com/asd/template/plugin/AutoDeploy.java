package com.asd.template.plugin;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自动部署工具
 * 
 */
public class AutoDeploy {

    private String sourceWarFile;
    private String catalinaHome;
    private String loggerFile;
    private String host;
    private String user;
    private String password;

    public AutoDeploy(String host, String user, String password, String sourceWarFile, String catalinaHome, String loggerFile) {
        this.sourceWarFile = sourceWarFile;
        this.catalinaHome = catalinaHome;
        this.host = host;
        this.user = user;
        this.password = password;
        this.loggerFile = loggerFile;
    }

    /**
     * loggerFile 默认为 catalinaHome/logs/catalina.out
     * 
     * @see Shell
     * @param sourceWarFile 本地war文件的绝对路径
     * @param catalinaHome 目标主机的tomcat目录
     * 
     */
    public AutoDeploy(String host, String user, String password, String sourceWarFile, String catalinaHome) {
        this(host, user, password, sourceWarFile, catalinaHome, catalinaHome + "/logs/catalina.out");
    }

    /**
     * 部署
     */
    @Deprecated
    public void deploy() throws IOException {

        Shell shell = new Shell(host, user, password);

        shell.sftp(sourceWarFile, catalinaHome + "/webapps/");

        // 生成一个输入流，用来作为shell的输入数据，
        // 得到input_wreiter，可以用来不断向输入流中写入
        PipedOutputStream input_pipe = new PipedOutputStream();
        InputStream input = new PipedInputStream(input_pipe);
        PrintWriter inputWriter = new PrintWriter(input_pipe);

        // 生成一个输出流，用户作为shell的输出数据
        // 得到一个output_pipe，用来不断从输出流中得到数据
        PipedInputStream output_pipe = new PipedInputStream();
        PipedOutputStream ouput = new PipedOutputStream(output_pipe);
        BufferedReader outputReader = new BufferedReader(new InputStreamReader(output_pipe));
        shell.shell(input, ouput);

        // 将原先有的reader中的数据清掉
        while (outputReader.ready()) {
            outputReader.readLine();
        }

        inputWriter.println("cd " + catalinaHome + "\nps -ef | grep tomcat");
        inputWriter.flush();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String pid = null;
        while (outputReader.ready()) {
            String line = outputReader.readLine();
            if (line.endsWith("org.apache.catalina.startup.Bootstrap start")) {
                pid = getPid(line);
                break;
            }
        }
        String cmd = String.format("bin/startup.sh\ntail -f %s", loggerFile);
        if (pid != null) {
            cmd = String.format("kill -9 %s\n%s", pid, cmd);
        }
        inputWriter.println(cmd);
        inputWriter.flush();
        String line = outputReader.readLine();
        while (line != null) {
            System.out.println(line);
            line = outputReader.readLine();
        }

    }

    private String getPid(String line) {
        Pattern pattern = Pattern.compile("\\w+[ ]+\\d+");
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            String userAndPid = matcher.group();
            for (int i = userAndPid.length() - 1; i >= 0; i--) {
                if (userAndPid.charAt(i) == ' ') {
                    return userAndPid.substring(i + 1);
                }
            }
        }
        return null;
    }

    // 文件传输；开启tomcat；启动控制台输出
    public void console() throws Exception {

        Shell shell = new Shell(host, user, password);
        shell.sftp(sourceWarFile, catalinaHome + "/webapps/");

        String cmd = String.format("%s/bin/startup.sh\ntail -f %s\n", catalinaHome, loggerFile);
        shell.shell(new ByteArrayInputStream(cmd.getBytes()), System.out);

    }

    // 文件传输；启动控制台输出
    public void redeploy() throws Exception {
        Shell shell = new Shell(host, user, password);
        shell.sftp(sourceWarFile, catalinaHome + "/webapps/");

        String cmd = String.format("tail -f %s\n", loggerFile);
        shell.shell(new ByteArrayInputStream(cmd.getBytes()), System.out);
    }

}
