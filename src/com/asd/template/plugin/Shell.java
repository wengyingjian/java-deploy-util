package com.asd.template.plugin;

import java.io.InputStream;
import java.io.OutputStream;

import com.asd.template.plugin.monitor.MySftpProgressMonitor;
import com.jcraft.jsch.*;

/**
 * jsch实现的shell，包含shell，sftp两大功能
 */
public class Shell {

    private static final String CHANNEL_SHELL    = "shell";
    private static final String CHANNEL_SFTP     = "sftp";
    private static final int    CONNECT_TIME_OUT = 30000;
    private String              host;
    private String              user;
    private String              password;
    private int                 port             = 22;

    /**
     * @param host 目标服务器地址
     * @param user 目标服务器用户名
     * @param password 目标服务器用户名对应的密码
     * 
     */
    public Shell(String host, String user, String password) {
        this.host = host;
        this.user = user;
        this.password = password;
    }

    /**
     * 获取shell连接
     * 
     * @param input 连接的输入流，可以使用pipeInputStream,向输入流中写入数据
     * @param output 连接的输出流，可以使用System.out打印在控制台
     * @return <code>Channel</code> 可以调用channel.disconnect关闭连接
     */
    public Channel shell(InputStream input, OutputStream output) {
        if (input == null) {
            input = System.in;
        }
        if (output == null) {
            output = System.out;
        }
        try {
            Session session = getSession();
            Channel channel = session.openChannel(CHANNEL_SHELL);
            channel.setInputStream(input);
            channel.setOutputStream(output);
            channel.connect(CONNECT_TIME_OUT);
            return channel;
        } catch (JSchException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * sftp文件传输
     * 
     * @param sourceFile 源文件的（绝对）文件位置
     * @param destDir 目标文件的文件路径 默认在/root/(root用户),／home/${user}/(普通用户)下
     */
    public void sftp(String sourceFile, String destDir) {
        System.out.println(String.format("start sftp:\nsourceFile:\t%s\ndestDir:\t%s", sourceFile, destDir));
        if (destDir == null || "".equals(destDir)) {
            destDir = ".";
        }
        try {
            Session session = getSession();
            Channel channel = session.openChannel(CHANNEL_SFTP);
            channel.connect();
            ChannelSftp c = (ChannelSftp) channel;
            SftpProgressMonitor monitor = new MySftpProgressMonitor();
            int mode = ChannelSftp.OVERWRITE;
            c.put(sourceFile, destDir, monitor, mode);
            session.disconnect();
        } catch (JSchException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (SftpException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Session getSession() throws JSchException {
        JSch jsch = new JSch();
        Session session = jsch.getSession(user, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect(CONNECT_TIME_OUT);
        return session;
    }

}