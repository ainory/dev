package com.ainory.dev.utils.kafka;


import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by ainory on 2016. 9. 30..
 */
public class KafkaProducerUtil {
    private static final Logger logger = LoggerFactory.getLogger(KafkaProducerUtil.class);

    private KafkaProducer<String,String> producer;
    private String topic;

    private final String DEFAULT_ACKS = "1";
    private final String DEFAULT_BATCH_SIZE = "20000";
    private final String DEFAULT_LINGER_MS = "0";
    private final String DEFAULT_KEY_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    private final String DEFAULT_VALUE_SERIALIZER = "org.apache.kafka.common.serialization.StringSerializer";
    private final String DEFAULT_REQUEST_TIMEOUT_MS = "1000";
    private final String DEFAULT_TIMEOUT_MS = "1000";
    private final String DEFAULT_METADATA_FETCH_TIMEOUT_MS = "1000";

    public KafkaProducerUtil(String brokers, String topic) {

        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("acks", DEFAULT_ACKS);
        props.put("batch.size", DEFAULT_BATCH_SIZE);
        props.put("linger.ms", DEFAULT_LINGER_MS);
        props.put("key.serializer", DEFAULT_KEY_SERIALIZER);
        props.put("value.serializer", DEFAULT_VALUE_SERIALIZER);
        props.put("request.timeout.ms", DEFAULT_REQUEST_TIMEOUT_MS);
        props.put("timeout.ms", DEFAULT_TIMEOUT_MS);
        props.put("metadata.fetch.timeout.ms", DEFAULT_METADATA_FETCH_TIMEOUT_MS);

        producer = new KafkaProducer<>(props);
        this.topic = topic;

    }

    public KafkaProducerUtil(String brokers, String topic, int batchSize, int lingerMs) {

        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("acks", DEFAULT_ACKS);
        props.put("batch.size", String.valueOf(batchSize));
        props.put("linger.ms", String.valueOf(lingerMs));
        props.put("key.serializer", DEFAULT_KEY_SERIALIZER);
        props.put("value.serializer", DEFAULT_VALUE_SERIALIZER);
        props.put("request.timeout.ms", DEFAULT_REQUEST_TIMEOUT_MS);
        props.put("timeout.ms", DEFAULT_TIMEOUT_MS);
        props.put("metadata.fetch.timeout.ms", DEFAULT_METADATA_FETCH_TIMEOUT_MS);

        producer = new KafkaProducer<>(props);
        this.topic = topic;
    }

    public KafkaProducerUtil(String brokers, String topic, int batchSize, int lingerMs, String keySerializer, String valueSerializer) {

        Properties props = new Properties();
        props.put("bootstrap.servers", brokers);
        props.put("acks", DEFAULT_ACKS);
        props.put("batch.size", String.valueOf(batchSize));
        props.put("linger.ms", String.valueOf(lingerMs));
        props.put("key.serializer", keySerializer);
        props.put("value.serializer", valueSerializer);
        props.put("request.timeout.ms", DEFAULT_REQUEST_TIMEOUT_MS);
        props.put("timeout.ms", DEFAULT_TIMEOUT_MS);
        props.put("metadata.fetch.timeout.ms", DEFAULT_METADATA_FETCH_TIMEOUT_MS);

        producer = new KafkaProducer<>(props);
        this.topic = topic;
    }

    /**
     * Message Send
     * @param message
     * @param flushFlag
     * @return
     */
    public boolean sendMessage(String message, boolean flushFlag){

        try{
            if(producer != null){
                producer.send(new ProducerRecord<String, String>(topic, message));

                if(flushFlag){
                    producer.flush();
                }

                return true;
            }else {
                return false;
            }
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return false;
        }
    }

    /**
     * Message Send
     * @param message
     * @return
     */
    public boolean sendMessage(String message){
        return sendMessage(message, true);
    }

    /**
     * Message List Send
     * @param msgList
     * @return
     * @throws Exception
     */
    public boolean sendMessageList(ArrayList<String> msgList) throws Exception{

        long start = System.nanoTime();
        int successCount = 0;

        for(String msg : msgList){
            if(sendMessage(msg, false) == true){
                successCount++;
            }
        }
        producer.flush();

        long end = System.nanoTime();
        logger.debug("Message Send Complete(Total: "+ msgList.size() +" /Success: "+successCount+") - "+(end-start)/1000000.0+"ms");

        return true;
    }

    /**
     * Close Producer
     */
    public void closeProducer(){

        try{
            if(producer != null){
                producer.flush();
                producer.close();
            }
        }catch (Exception e){
            logger.error(ExceptionUtils.getMessage(e));
        }
    }

    public static void main(String[] args) {
        try{

//            KafkaProducerUtil  kafkaProducerUtil = new KafkaProducerUtil("localhost:9092","TEST");
//            KafkaProducerUtil  kafkaProducerUtil = new KafkaProducerUtil("localhost:9092","TEST");
            KafkaProducerUtil  kafkaProducerUtil = new KafkaProducerUtil("localhost:9092","TEST");
//            KafkaProducerUtil  kafkaProducerUtil = new KafkaProducerUtil("localhost:9092","TEST");

            /*ArrayList<String> messageList = new ArrayList<>();

            MetricDataListInfo metricDataListInfo = new MetricDataListInfo();
            MetricDataInfo metricDataInfo = new MetricDataInfo();

            metricDataInfo.setProcess_seq(1001);
            metricDataInfo.setSystem_seq(111);
            metricDataInfo.setTable_name("table_a");
            metricDataInfo.setMetric_name("Metric A");
            metricDataInfo.setMetric_value("value");
            metricDataInfo.setTimestamp(System.currentTimeMillis());

            metricDataListInfo.addDataList(metricDataInfo);

            metricDataInfo = new MetricDataInfo();

            metricDataInfo.setProcess_seq(1001);
            metricDataInfo.setSystem_seq(111);
            metricDataInfo.setTable_name("table_a");
            metricDataInfo.setMetric_name("Metric A");
            metricDataInfo.setMetric_value("value");
            metricDataInfo.setTimestamp(System.currentTimeMillis());

            metricDataListInfo.addDataList(metricDataInfo);

            metricDataInfo = new MetricDataInfo();

            metricDataInfo.setProcess_seq(1001);
            metricDataInfo.setSystem_seq(111);
            metricDataInfo.setTable_name("table_a");
            metricDataInfo.setMetric_name("Metric A");
            metricDataInfo.setMetric_value("value");
            metricDataInfo.setTimestamp(System.currentTimeMillis());

            metricDataListInfo.addDataList(metricDataInfo);

            metricDataInfo = new MetricDataInfo();

            metricDataInfo.setProcess_seq(1001);
            metricDataInfo.setSystem_seq(111);
            metricDataInfo.setTable_name("table_a");
            metricDataInfo.setMetric_name("Metric A");
            metricDataInfo.setMetric_value("value");
            metricDataInfo.setTimestamp(System.currentTimeMillis());

            metricDataListInfo.addDataList(metricDataInfo);

            kafkaProducerUtil.sendMessage(metricDataListInfo.toJsonString());

            kafkaProducerUtil.sendMessage("");

            messageList.add(metricDataListInfo.toJsonString());
            messageList.add(metricDataListInfo.toJsonString());
            messageList.add(metricDataListInfo.toJsonString());
            messageList.add(metricDataListInfo.toJsonString());
            messageList.add(metricDataListInfo.toJsonString());
            messageList.add(metricDataListInfo.toJsonString());
            messageList.add(metricDataListInfo.toJsonString());
            kafkaProducerUtil.sendMessageList(messageList);

            kafkaProducerUtil.closeProducer();
*/
        }catch (Exception e){
            e.printStackTrace();
            System.out.println(ExceptionUtils.getStackTrace(e));
        }
//        kafkaProducerUtil.closeProducer();
    }
}
