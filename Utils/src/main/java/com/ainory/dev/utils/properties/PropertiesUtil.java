package com.ainory.dev.utils.properties;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * Created by ainory on 2016. 6. 13..
 */
public class PropertiesUtil {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static PropertiesUtil propertiesUtil;

    private static PropertiesConfiguration propertiesConfiguration;

    private static final String PROPERTIES_DEFAULT = "common.properties";
    private static String PROPERTIES_FILE_PATH;

    static{
        try{
            if(StringUtils.isEmpty(PROPERTIES_FILE_PATH)){
                PROPERTIES_FILE_PATH = PROPERTIES_DEFAULT;
            }
            propertiesUtil = new PropertiesUtil();
        }catch (Exception e){

        }
    }

    public PropertiesUtil() throws ConfigurationException{
        load();
    }

    public static void setPropertiesFile(String propertiesFile) throws Exception {
        PROPERTIES_FILE_PATH = propertiesFile;
        load();
    }

    private static void load() throws ConfigurationException{
        propertiesConfiguration = new PropertiesConfiguration();
        propertiesConfiguration.load(PROPERTIES_FILE_PATH);
    }

    public static String getString(String key){
        return propertiesConfiguration.getString(key);
    }
    public static int getInt(String key){
        return propertiesConfiguration.getInt(key);
    }

    public static void printProperties(){
        logger.info("=================================");
        for(Iterator<String> iter = propertiesConfiguration.getKeys(); iter.hasNext();){

            String key = iter.next();
            logger.info(key +" : " +getString(key));

        }
        logger.info("=================================");

    }

    public static void main(String[] args) {
        try{
//            PropertiesUtil.setPropertiesFile("yarncollector.properties");
            PropertiesUtil.printProperties();

            logger.debug(propertiesConfiguration.getString("runner.jobhistoryserver.state"));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
