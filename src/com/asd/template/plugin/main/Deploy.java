/**
 * 
 */
package com.asd.template.plugin.main;

import java.util.Properties;

import com.asd.template.plugin.AutoDeploy;

/**
 * 
 * @author <a href="mailto:wengyingjian@foxmail.com">翁英健</a>
 * @version 1.1 2015年12月17日
 * @since 1.1
 */
public class Deploy {

    public static void main(String[] args) throws Exception {
        Properties argsMap = getArgsMap(getFile(args[0]));
        String host = argsMap.getProperty("host");
        String user = argsMap.getProperty("user");
        String password = argsMap.getProperty("password");
        String sourceWarFile = argsMap.getProperty("sourceWarFile");
        String catalinaHome = argsMap.getProperty("catalinaHome");
        String loggerFile = argsMap.getProperty("loggerFile");
        String log = "args:\nhost:%s\nuser:%s\npassword:%s\nsourceWarFile:%s\ncatalinaHome:%s\nloggerFile:%s\n";
        System.out.println(String.format(log, host, user, password, sourceWarFile, catalinaHome, loggerFile));
        AutoDeploy autoDeploy = new AutoDeploy(host, user, password, sourceWarFile, catalinaHome, loggerFile);
        autoDeploy.redeploy();
    }

    /**
     * @param prefix
     * @return
     */
    private static String getFile(String prefix) {
        String fileName = prefix + "-deploy.properties";
        return fileName;
    }

    /**
     * @return
     */
    private static Properties getArgsMap(String file) throws Exception {
        Properties properties = new Properties();
        properties.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(file));
        return properties;
    }
}
