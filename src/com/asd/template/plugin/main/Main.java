/**
 * 
 */
package com.asd.template.plugin.main;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import com.asd.template.plugin.AutoDeploy;

/**
 * 
 * @author <a href="mailto:wengyingjian@foxmail.com">翁英健</a>
 * @version 1.1 2015年12月18日
 * @since 1.1
 */
public class Main {
    public static final String       MODE_DEPLOY = "deploy";
    public static final String       MODE_SHELL  = "shell";
    public static final String       MODE_LOG    = "log";
    public static final String       MODE_MANUAL = "manual";
    public static final List<String> MODE_LIST   = Arrays.asList(MODE_DEPLOY, MODE_SHELL, MODE_LOG, MODE_MANUAL);
    private static Scanner           scanner;

    public static void main(String[] args) throws Exception {
        scanner = new Scanner(System.in);

        File configFile = getConfigFile(args);
        AutoDeploy deploy = new AutoDeploy(configFile);

        String mode = getMode();
        if (MODE_DEPLOY.equals(mode)) {
            deploy.redeploy();
            return;
        }
        if (MODE_SHELL.equals(mode)) {
            deploy.shell();
            return;
        }
        if (MODE_MANUAL.equals(mode)) {
            deploy.manual(getManualConfig(args));
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
    private static String getManualConfig(String[] args) {
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
    private static File getConfigFile(String[] args) {
        File file = null;
        // 如果第一个参数有输入，则认为是配置文件
        if (args.length > 0) {
            file = new File(getFileName(args[0]));
            if (file.exists()) {
                return file;
            }
        }
        // 否则要求用户输入
        while (true) {
            System.out.println("please input config file:");
            if (scanner.hasNextLine()) {
                String fileName = scanner.nextLine();
                file = new File(getFileName(fileName));
                if (file.exists()) {
                    return file;
                } else {
                    System.out.println(String.format("file %s not exists!", getFileName(fileName)));
                }
            }
        }
    }

    /**
     * 获取绝对路径的文件名
     * 
     * @param string
     * @return
     */
    private static String getFileName(String fileName) {
        if (!fileName.startsWith("/")) {
            fileName = System.getProperty("user.dir") + "/" + fileName;
        }
        return fileName;
    }

    /**
     * @return
     */
    private static String getMode() {
        String mode = System.getProperty("mode");
        if (mode == null) {
            mode = MODE_DEPLOY;
        }
        if (!MODE_LIST.contains(mode)) {
            mode = MODE_DEPLOY;
        }
        System.out.println("mode:\t" + mode);
        return mode;
    }
}
