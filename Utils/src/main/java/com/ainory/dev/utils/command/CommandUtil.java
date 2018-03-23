package com.ainory.dev.utils.command;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by ainory on 2016. 12. 21..
 */
public class CommandUtil {
    private static final Logger logger = LoggerFactory.getLogger(CommandUtil.class);

    /**
     * Execute Command
     *
     * @param commandLine
     * @return
     */
    public static String executionCommand(String commandLine){

        StringBuffer executionMsg = new StringBuffer();
        try{
            Process process = Runtime.getRuntime().exec(commandLine);

            process.waitFor();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line = "";

            while ((line=bufferedReader.readLine()) != null){
                executionMsg.append(line).append(StringUtils.LF);
                logger.debug(line);
            }

            return executionMsg.toString();
        }catch (Exception e){
            return ExceptionUtils.getRootCauseMessage(e);
        }
    }

    /**
     * Make Command Line
     *
     * @param path
     * @param shellFile
     * @param process
     * @param arg
     * @param action
     * @return
     */
    public static String makeCommandLine(String path, String shellFile, String process, String arg, String action){

        StringBuffer commandLine = new StringBuffer();

        commandLine.append(path).append("/").append(shellFile).append(StringUtils.SPACE).append(process).append(StringUtils.SPACE).append(action).append(StringUtils.SPACE).append(StringUtils.isEmpty(arg)?"":arg);

        logger.debug(commandLine.toString());
        return commandLine.toString();
    }

    public static void main(String[] args) {
//        logger.debug(executionCommand("/Users/ainory/Desktop/Dev/TestPackage/zookeeper-3.4.6/bin/zkServer.sh stop"));
    }
}
