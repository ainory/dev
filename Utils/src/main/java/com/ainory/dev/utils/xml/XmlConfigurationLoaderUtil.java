package com.ainory.dev.utils.xml;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by ainory on 2016. 7. 25..
 */
public class XmlConfigurationLoaderUtil {
    private static final Logger logger = LoggerFactory.getLogger(XmlConfigurationLoaderUtil.class);

    /**
     *
     * @param confPath
     * @return XMLConfiguration
     */
    private static XMLConfiguration initConfiguration(String confPath){

        try{
            XMLConfiguration xmlConfiguration = new XMLConfiguration();
            xmlConfiguration.setDelimiterParsingDisabled(true);
            xmlConfiguration.load(confPath);

            return xmlConfiguration;
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new XMLConfiguration();
        }
    }

    /**
     *
     * @param confPath
     * @return Configuration name:value Map
     */
    public static HashMap<String, String> xmlConfigurationLoad(String confPath){

        try{
            HashMap<String, String> resultConfMap = new HashMap<String, String>();

            Object root = initConfiguration(confPath).getRootNode();
            if(root instanceof HierarchicalConfiguration.Node){
                HierarchicalConfiguration.Node node = (HierarchicalConfiguration.Node) root;

                for(ConfigurationNode childNode : node.getChildren()){
                    StringBuffer name = new StringBuffer();
                    StringBuffer value = new StringBuffer();

                    for(ConfigurationNode childDataNode : childNode.getChildren()){

                        if(StringUtils.equalsIgnoreCase(childDataNode.getName(),"name")){
                            name.append(childDataNode.getValue());
                        }
                        if(StringUtils.equalsIgnoreCase(childDataNode.getName(),"value")){
                            value.append(childDataNode.getValue());
                        }
                    }
                    resultConfMap.put(name.toString(),value.toString());
                }
            }
            return resultConfMap;

        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new HashMap<>();
        }
    }

    /**
     *
     * @param confPath
     * @param confBackupPath
     * @return
     */
    public static boolean xmlConfigurationBackup(String confPath, String confBackupPath){

        try{
            initConfiguration(confPath).save(confBackupPath);
            return true;
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    /**
     *
     * @param confMap
     * @return NameNode Alias Map (ex: {SUN-002-01:nn2},{SUN-001-01:nn1}...)
     */
    public static HashMap<String,String> getNameNodeAliasMap(HashMap<String,String> confMap){

        final String NAMESERVICE_KEY = "dfs.nameservices";
        final String HA_NAMENODES_PREFIX = "dfs.ha.namenodes.";
        final String NAME_HTTP_PREFIX = "dfs.namenode.http-address.";

        HashMap<String, String> hostMappingMap = new HashMap<>();

        try{
            if(!confMap.containsKey(NAMESERVICE_KEY)){
                return new HashMap<>();
            }

            String nameService = confMap.get(NAMESERVICE_KEY);

            if(!confMap.containsKey(HA_NAMENODES_PREFIX+nameService)){
                return new HashMap<>();
            }

            String[] innerNames = StringUtils.split(confMap.get(HA_NAMENODES_PREFIX+nameService), ",");

            for(String innerName : innerNames){

                String key = NAME_HTTP_PREFIX+nameService+"."+innerName;
                if(!confMap.containsKey(key)){
                    return new HashMap<>();
                }
                hostMappingMap.put(StringUtils.upperCase(StringUtils.split(confMap.get(key),":")[0]), innerName);
            }

            return hostMappingMap;

        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new HashMap<>();
        }
    }

    /**
     *
     * @param confMap
     * @param searchHost
     * @return NameNodeAlias (ex: nn1)
     */
    public static String getNameNodeAlias(HashMap<String,String> confMap, String searchHost){

        HashMap<String,String> hostMappingMap = getNameNodeAliasMap(confMap);

        if(!hostMappingMap.containsKey(StringUtils.upperCase(searchHost))){
            return new String();
        }

        return hostMappingMap.get(StringUtils.upperCase(searchHost));
    }

    /**
     *
     * @param confPath
     * @param searchHost
     * @return NameNodeAlias (ex: nn1)
     */
    public static String getNameNodeAlias(String confPath, String searchHost){

        HashMap<String,String> hostMappingMap = getNameNodeAliasMap(xmlConfigurationLoad(confPath));

        if(!hostMappingMap.containsKey(StringUtils.upperCase(searchHost))){
            return searchHost;
        }

        return hostMappingMap.get(StringUtils.upperCase(searchHost));
    }

    public static HashMap<String,String> getResourceManagerAliasMap(HashMap<String,String> confMap){

        final String RM_HA_ENABLED_KEY = "yarn.resourcemanager.ha.enabled";
        final String RM_HA_IDS_KEY = "yarn.resourcemanager.ha.rm-ids";
        final String RM_WEBAPP_ADDRESS_PREFIX = "yarn.resourcemanager.webapp.address.";

        HashMap<String, String> hostMappingMap = new HashMap<>();

        try{

            if(!confMap.containsKey(RM_HA_ENABLED_KEY)){
                return new HashMap<>();
            }

            if(StringUtils.equalsIgnoreCase(confMap.get(RM_HA_ENABLED_KEY),"false")){
                return new HashMap<>();
            }

            if(!confMap.containsKey(RM_HA_IDS_KEY)){
                return new HashMap<>();
            }

            String[] innerNames = StringUtils.split(confMap.get(RM_HA_IDS_KEY), ",");

            for(String innerName : innerNames){

                String key = RM_WEBAPP_ADDRESS_PREFIX + innerName;
                if(!confMap.containsKey(key)){
                    return new HashMap<>();
                }
                hostMappingMap.put(StringUtils.upperCase(StringUtils.split(confMap.get(key),":")[0]), innerName);
            }

            return hostMappingMap;

        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new HashMap<>();
        }
    }

    public static String getResourceManagerAlias(HashMap<String,String> confMap, String searchHost){

        HashMap<String,String> hostMappingMap = getResourceManagerAliasMap(confMap);

        if(!hostMappingMap.containsKey(StringUtils.upperCase(searchHost))){
            return new String();
        }

        return hostMappingMap.get(StringUtils.upperCase(searchHost));
    }

    public static String getResourceManagerAlias(String confPath, String searchHost){

        HashMap<String,String> hostMappingMap = getResourceManagerAliasMap(xmlConfigurationLoad(confPath));

        if(!hostMappingMap.containsKey(StringUtils.upperCase(searchHost))){
            return searchHost;
        }

        return hostMappingMap.get(StringUtils.upperCase(searchHost));
    }


    public static void main(String[] args) {

        /*HashMap<String, String> resultMap = xmlConfigurationLoad("/Users/ainory/Desktop/Dev/SourceFactory_MetaTron/TestTicat/src/main/resources/hdfs-site.xml");

        for(Iterator<String> iter = resultMap.keySet().iterator(); iter.hasNext();){
            String key = iter.next();

//            logger.debug(key + " : "+resultMap.get(key));
            System.out.println(key + " : "+resultMap.get(key));
        }*/

//        System.out.println(getNameNodeAlias("/Users/ainory/Desktop/Dev/SourceFactory_MetaTron/TestTicat/src/main/resources/hdfs-site.xml","sun-002-01"));

        System.out.println(getResourceManagerAlias("/Users/ainory/Desktop/Dev/SourceFactory_MetaTron/METATRON-applications/JmxCollector/src/main/resources/yarn-site.xml", "SUN-001-02"));

//        System.out.println(getNameNodeAlias(resultMap, "sun-001-01"));
    }
}
