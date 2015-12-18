/**
 * 
 */
package com.asd.template.plugin.main;

import java.util.Scanner;

import com.asd.template.plugin.AutoDeploy;

/**
 * 
 * @author <a href="mailto:wengyingjian@foxmail.com">翁英健</a>
 * @version 1.1 2015年12月18日
 * @since 1.1
 */
public class Main {
    public static final String MODE_DEPLOY = "deploy";
    public static final String MODE_SHELL  = "shell";
    public static final String MODE_LOG    = "log";
    public static final String MODE_MANUAL = "manual";

    private static Scanner     scanner;

    public static void main(String[] args) throws Exception {
        scanner = new Scanner(System.in);
        String configFile = getConfigFile(args);
        String mode = getMode();
        AutoDeploy deploy = new AutoDeploy(configFile);
        if (MODE_DEPLOY.equals(mode)) {
            deploy.redeploy();
            return;
        }
        if (MODE_SHELL.equals(mode)) {
            deploy.shell();
            return;
        }
        if (MODE_MANUAL.equals(mode)) {
            deploy.manual(getConfig(args));
            return;
        }
        if (MODE_LOG.equals(mode)) {
            deploy.log();
            return;
        }
    }

    /**
     * @param string
     * @return
     */
    private static String getConfig(String[] args) {
        if (args.length < 2) {
            System.out.println("no args found! \n example: deploy a.properties old");
            System.out.println("now choose : new / old");
            while (true) {
                if (scanner.hasNextLine()) {
                    String choice = scanner.nextLine();
                    System.out.println("getArgs:" + choice);
                    if (choice == null) {
                        continue;
                    }
                    if ("new".equals(choice.trim())) {
                        return "new";
                    }
                    if ("old".equals(choice.trim())) {
                        return "old";
                    }
                } else {
                    System.out.println("wait!");
                }
            }
        }
        return args[1];
    }

    /**
     * @param args
     * @return
     */
    private static String getConfigFile(String[] args) {
        String file = null;
        // 如果第一个参数有输入，则认为是配置文件
        if (args.length > 0) {
            file = args[0];
        } else {
            // 否则要求用户输入
            while (true) {
                if (file != null && !"".equals(file.trim())) {
                    break;
                }
                System.out.println("please input config file:");
                if (scanner.hasNextLine()) {
                    file = scanner.nextLine();
                }
            }
        }
        // 如果输入的不是绝对路径，则通过当前路径拼接成绝对路径
        if (!file.startsWith("/")) {
            file = System.getProperty("user.dir") + "/" + file;
        }
        System.out.println("config file : " + file);
        return file;
    }

    /**
     * @return
     */
    private static String getMode() {
        String mode = System.getProperty("mode");
        if (mode == null) {
            mode = MODE_DEPLOY;
        }
        if (!(MODE_MANUAL.equals(mode) || MODE_SHELL.equals(mode) || MODE_LOG.equals(mode))) {
            mode = MODE_DEPLOY;
        }
        System.out.println("mode:\t" + mode);
        return mode;
    }
}
