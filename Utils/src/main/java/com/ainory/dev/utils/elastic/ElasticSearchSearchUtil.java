package com.ainory.dev.utils.elastic;


import com.ainory.dev.utils.elastic.entity.ElasticSearchAggregationInfo;
import com.ainory.dev.utils.elastic.entity.ElasticSearchAggregationListInfo;
import com.ainory.dev.utils.elastic.entity.ElasticSearchDataInfo;
import com.ainory.dev.utils.elastic.entity.ElasticSearchDataListInfo;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram.Bucket;
import org.elasticsearch.search.aggregations.bucket.histogram.ParsedDateHistogram;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

/**
 * ElasticSearch Search Util ( log collect using filebeat )
 *
 * Created by ainory on 2017. 9. 4..
 */
public class ElasticSearchSearchUtil {

    private static final Logger logger = LoggerFactory.getLogger(ElasticSearchSearchUtil.class);

    private static final Header[] HEADERS = { new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"),  new BasicHeader("Role", "Read") };

    public static final String SORT_ASC = "ASC";
    public static final String SORT_DESC = "DESC";

    public static final String PERIOD_UNIT_SECOND = "s";
    public static final String PERIOD_UNIT_MINUTE = "m";
    public static final String PERIOD_UNIT_HOUR = "h";
    public static final String PERIOD_UNIT_DAY = "d";

    private static final String INDEX = "index-*";
    private static final int DEFAULT_FROM = 0;
    private static final int DEFAULT_SIZE = 10000;
    private static final String AGGREGATION_FIELD_HOST = "host.keyword";
    private static final String AGGREGATION_FIELD_FILE = "file.keyword";
    private static final String FIELD_TIMESTAMP = "@timestamp";
    private static final String FIELD_HOST = "host";
    private static final String FIELD_FILE = "file";
    private static final String FIELD_MESSAGE = "message";

    private enum  AGGREGATION_TYPE {
        HOST, FILE, HOST_FILE, TIME_COUNT, ALL
    }

    private static final String DATE_FORMAT_DEFAULT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * View log using time filter only
     *
     * ex) time -> 2017-09-24 00:05:00.000
     *     plusMinusMinute -> 5
     *
     *     Search Range : 2017-09-24 00:00:00.000 ~ 2017-09-24 00:10:00.000
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param time - Standard Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param plusMinusMinute - Standard Time Plus Minus Minute
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchAll(String esHost, int esPort, String time, int plusMinusMinute, String sort, int pagingSize, int pageNum){

        try{
            return search(esHost,esPort, getSearchStartTime(time, plusMinusMinute), getSearchEndTime(time, plusMinusMinute), new ArrayList<>(), new ArrayList<>(), null, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * View log using time filter only (start/end time use)
     * 
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param startTime - Search Start Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param endTime - Search End Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchAll(String esHost, int esPort, String startTime, String endTime, String sort, int pagingSize, int pageNum){

        try{
            return search(esHost,esPort, getMilliTime(startTime), getMilliTime(endTime), new ArrayList<>(), new ArrayList<>(), null, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * Host Filter Search ( Host )
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param time - Standard Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param plusMinusMinute - Standard Time Plus Minus Minute
     * @param hostFilterList - Host List
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchHostFilter(String esHost, int esPort, String time, int plusMinusMinute, ArrayList<String> hostFilterList, String sort, int pagingSize, int pageNum){
        try{
            return search(esHost,esPort, getSearchStartTime(time, plusMinusMinute), getSearchEndTime(time, plusMinusMinute), hostFilterList, new ArrayList<>(), null, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param startTime - Search Start Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param endTime - Search End Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param hostFilterList - Host List
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchHostFilter(String esHost, int esPort, String startTime, String endTime, ArrayList<String> hostFilterList, String sort, int pagingSize, int pageNum){
        try{
            return search(esHost,esPort, getMilliTime(startTime), getMilliTime(endTime), hostFilterList, new ArrayList<>(), null, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * Host Filter Search ( Host & Message )
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param time - Standard Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param plusMinusMinute - Standard Time Plus Minus Minute
     * @param hostFilterList - Host List
     * @param messageFilter - Message Filter
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchHostFilter(String esHost, int esPort, String time, int plusMinusMinute, ArrayList<String> hostFilterList, String messageFilter, String sort, int pagingSize, int pageNum){
        try{
            return search(esHost,esPort, getSearchStartTime(time, plusMinusMinute), getSearchEndTime(time, plusMinusMinute), hostFilterList, new ArrayList<>(), messageFilter, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * Host Filter Search ( Host & Message )
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param startTime - Search Start Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param endTime - Search End Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param hostFilterList - Host List
     * @param messageFilter - Message Filter
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchHostFilter(String esHost, int esPort, String startTime, String endTime, ArrayList<String> hostFilterList, String messageFilter, String sort, int pagingSize, int pageNum){
        try{
            return search(esHost,esPort, getMilliTime(startTime), getMilliTime(endTime), hostFilterList, new ArrayList<>(), messageFilter, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * File Filter Search ( File )
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param time - Standard Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param plusMinusMinute - Standard Time Plus Minus Minute
     * @param fileFilterList - File List
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchFileFilter(String esHost, int esPort, String time, int plusMinusMinute, ArrayList<String> fileFilterList, String sort, int pagingSize, int pageNum){
        try{
            return search(esHost,esPort, getSearchStartTime(time, plusMinusMinute), getSearchEndTime(time, plusMinusMinute), new ArrayList<>(), fileFilterList, null, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * File Filter Search ( File )
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param startTime - Search Start Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param endTime - Search End Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param fileFilterList - File List
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchFileFilter(String esHost, int esPort, String startTime, String endTime, ArrayList<String> fileFilterList, String sort, int pagingSize, int pageNum){
        try{
            return search(esHost,esPort, getMilliTime(startTime), getMilliTime(endTime), new ArrayList<>(), fileFilterList, null, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * File Filter Search ( File & Message )
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param time - Standard Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param plusMinusMinute - Standard Time Plus Minus Minute
     * @param fileFilterList - File List
     * @param messageFilter - Message Filter
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchFileFilter(String esHost, int esPort, String time, int plusMinusMinute, ArrayList<String> fileFilterList, String messageFilter, String sort, int pagingSize, int pageNum){
        try{
            return search(esHost,esPort, getSearchStartTime(time, plusMinusMinute), getSearchEndTime(time, plusMinusMinute), new ArrayList<>(), fileFilterList, null, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * File Filter Search ( File & Message )
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param startTime - Search Start Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param endTime - Search End Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param fileFilterList - File List
     * @param messageFilter - Message Filter
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchFileFilter(String esHost, int esPort, String startTime, String endTime, ArrayList<String> fileFilterList, String messageFilter, String sort, int pagingSize, int pageNum){
        try{
            return search(esHost,esPort, getMilliTime(startTime), getMilliTime(endTime), new ArrayList<>(), fileFilterList, null, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * Filter Search ( Message )
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param time - Standard Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param plusMinusMinute - Standard Time Plus Minus Minute
     * @param messageFilter - Message Filter
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchFilter(String esHost, int esPort, String time, int plusMinusMinute, String messageFilter, String sort, int pagingSize, int pageNum){
        try{
            return search(esHost,esPort, getSearchStartTime(time, plusMinusMinute), getSearchEndTime(time, plusMinusMinute), new ArrayList<>(), new ArrayList<>(), messageFilter, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * Filter Search ( Message )
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param startTime - Search Start Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param endTime - Search End Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param messageFilter - Message Filter
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchFilter(String esHost, int esPort, String startTime, String endTime, String messageFilter, String sort, int pagingSize, int pageNum){
        try{
            return search(esHost,esPort, getMilliTime(startTime), getMilliTime(endTime), new ArrayList<>(), new ArrayList<>(), messageFilter, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * Filter Search ( Host & File )
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param time - Standard Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param plusMinusMinute - Standard Time Plus Minus Minute
     * @param hostFilterList - Host List
     * @param fileFilterList - File List
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchFilter(String esHost, int esPort, String time, int plusMinusMinute, ArrayList<String> hostFilterList, ArrayList<String> fileFilterList, String sort, int pagingSize, int pageNum){
        try{
            return search(esHost,esPort, getSearchStartTime(time, plusMinusMinute), getSearchEndTime(time, plusMinusMinute), hostFilterList, fileFilterList, null, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     *
     * Filter Search ( Host & File )
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param startTime - Search Start Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param endTime - Search End Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param hostFilterList - Host List
     * @param fileFilterList - File List
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchFilter(String esHost, int esPort, String startTime, String endTime, ArrayList<String> hostFilterList, ArrayList<String> fileFilterList, String sort, int pagingSize, int pageNum){
        try{
            return search(esHost,esPort, getMilliTime(startTime), getMilliTime(endTime), hostFilterList, fileFilterList, null, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * Filter Search ( Host & File & Message)
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param time - Standard Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param plusMinusMinute - Standard Time Plus Minus Minute
     * @param hostFilterList - Host List
     * @param fileFilterList - File List
     * @param messageFilter - Message Filter
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchFilter(String esHost, int esPort, String time, int plusMinusMinute, ArrayList<String> hostFilterList, ArrayList<String> fileFilterList, String messageFilter, String sort, int pagingSize, int pageNum){
        try{
            return search(esHost,esPort, getSearchStartTime(time, plusMinusMinute), getSearchEndTime(time, plusMinusMinute), hostFilterList, fileFilterList, messageFilter, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     *
     * Filter Search ( Host & File & Message)
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param startTime - Search Start Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param endTime - Search End Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param hostFilterList - Host List
     * @param fileFilterList - File List
     * @param messageFilter - Message Filter
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    public static ElasticSearchDataListInfo searchFilter(String esHost, int esPort, String startTime, String endTime, ArrayList<String> hostFilterList, ArrayList<String> fileFilterList, String messageFilter, String sort, int pagingSize, int pageNum){
        try{
            return search(esHost,esPort, getMilliTime(startTime), getMilliTime(endTime), hostFilterList, fileFilterList, messageFilter, sort, pagingSize, pageNum);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * LogEventManager Use...
     *
     * ex) time -> 2017-09-24 00:05:00.000
     *     minusMinute -> 5
     *
     *     Search Range : 2017-09-24 00:00:00.000 ~ 2017-09-24 00:05:00.000
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param time - Standard Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param minusMinute - Standard Time Minus Minute
     * @param hostFilterList - Host List
     * @param fileFilterList - File List
     * @param messageFilter - Message Filter
     * @param sort - Result Sort Default ASC
     * @return
     */
    public static ElasticSearchDataListInfo searchLog(String esHost, int esPort, String time, int minusMinute, ArrayList<String> hostFilterList, ArrayList<String> fileFilterList, String messageFilter, String sort){
        try{
            return search(esHost,esPort, time, minusMinute, hostFilterList, fileFilterList, messageFilter, sort);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * Aggregation All
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param startTime - Aggregation Start Time
     * @param endTime - Aggregation End Time
     * @param period - Period
     * @param periodUnit - d:Day, h:Hour, m:Minute, s:Seconds (refer to PERIOD_UNIT_DAY, PERIOD_UNIT_HOUR, PERIOD_UNIT_MINUTE, PERIOD_UNIT_SECOND)
     * @return
     */
    public static ElasticSearchAggregationListInfo aggregationAll(String esHost, int esPort, String startTime, String endTime, int period, String periodUnit){

        try{
            return aggregation(esHost,esPort, AGGREGATION_TYPE.ALL, startTime, endTime, period, periodUnit);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchAggregationListInfo();
        }
    }

    /**
     * Aggregation Host
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param startTime - Aggregation Start Time
     * @param endTime - Aggregation End Time
     * @param period - Period
     * @param periodUnit - d:Day, h:Hour, m:Minute, s:Seconds (refer to PERIOD_UNIT_DAY, PERIOD_UNIT_HOUR, PERIOD_UNIT_MINUTE, PERIOD_UNIT_SECOND)
     * @return
     */
    public static ElasticSearchAggregationListInfo aggregationHost(String esHost, int esPort, String startTime, String endTime, int period, String periodUnit){

        try{
            return aggregation(esHost,esPort, AGGREGATION_TYPE.HOST, startTime, endTime, period, periodUnit);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchAggregationListInfo();
        }
    }

    /**
     * Aggregation File
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param startTime - Aggregation Start Time
     * @param endTime - Aggregation End Time
     * @param period - Period
     * @param periodUnit - d:Day, h:Hour, m:Minute, s:Seconds (refer to PERIOD_UNIT_DAY, PERIOD_UNIT_HOUR, PERIOD_UNIT_MINUTE, PERIOD_UNIT_SECOND)
     * @return
     */
    public static ElasticSearchAggregationListInfo aggregationFile(String esHost, int esPort, String startTime, String endTime, int period, String periodUnit){

        try{
            return aggregation(esHost,esPort, AGGREGATION_TYPE.FILE, startTime, endTime, period, periodUnit);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchAggregationListInfo();
        }
    }

    /**
     * Aggregation Host_File
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param startTime - Aggregation Start Time
     * @param endTime - Aggregation End Time
     * @param period - Period
     * @param periodUnit - d:Day, h:Hour, m:Minute, s:Seconds (refer to PERIOD_UNIT_DAY, PERIOD_UNIT_HOUR, PERIOD_UNIT_MINUTE, PERIOD_UNIT_SECOND)
     * @return
     */
    public static ElasticSearchAggregationListInfo aggregationHostFile(String esHost, int esPort, String startTime, String endTime, int period, String periodUnit){

        try{
            return aggregation(esHost,esPort, AGGREGATION_TYPE.HOST_FILE, startTime, endTime, period, periodUnit);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchAggregationListInfo();
        }
    }

    /**
     * Search
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param startTime - Search Start Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param endTime - Search End Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param hostFilterList - Host List
     * @param fileFilterList - File List
     * @param messageFilter - Message Filter
     * @param sort - Result Sort Default ASC
     * @param pagingSize - Page Size
     * @param pageNum - Select page num
     * @return
     */
    private static ElasticSearchDataListInfo search(String esHost, int esPort, String startTime, String endTime, ArrayList<String> hostFilterList, ArrayList<String> fileFilterList, String messageFilter, String sort, int pagingSize, int pageNum){

        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        RestClient restClient = null;
        try{

            long start = System.nanoTime();

            if(hostFilterList == null || ArrayUtils.isEmpty(hostFilterList.toArray())){
                hostFilterList = new ArrayList<>();
            }
            if(fileFilterList == null || ArrayUtils.isEmpty(fileFilterList.toArray())){
                fileFilterList = new ArrayList<>();
            }

            // Connect
            restClient = RestClient.builder(new HttpHost(esHost, esPort,HttpHost.DEFAULT_SCHEME_NAME)).setDefaultHeaders(HEADERS).build();
            RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClient);

            // Indices
            SearchRequest searchRequest = new SearchRequest(INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            // BoolQuery
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.filter(QueryBuilders.rangeQuery(FIELD_TIMESTAMP).from(startTime).to(endTime));

            // Host Filter
            String hostFilter = StringUtils.join(hostFilterList, " ").trim();
            if(StringUtils.isNotEmpty(hostFilter)){
                boolQueryBuilder.must(QueryBuilders.matchQuery(FIELD_HOST, hostFilter));
            }
            // File Filter
            for(String fileFilter : fileFilterList){
                boolQueryBuilder.should(QueryBuilders.matchPhraseQuery(FIELD_FILE, fileFilter)).minimumShouldMatch(1);
            }

            // Message Filter
            if(StringUtils.isNotEmpty(messageFilter)){
                boolQueryBuilder.must(QueryBuilders.matchPhraseQuery(FIELD_MESSAGE, messageFilter));
            }

            // Order
            if(StringUtils.equals(sort, SORT_ASC)){
                searchSourceBuilder.sort(SortBuilders.fieldSort(FIELD_TIMESTAMP).order(SortOrder.ASC));
            }else if(StringUtils.equals(sort, SORT_DESC)){
                searchSourceBuilder.sort(SortBuilders.fieldSort(FIELD_TIMESTAMP).order(SortOrder.DESC));
            }else {
                searchSourceBuilder.sort(SortBuilders.fieldSort(FIELD_TIMESTAMP).order(SortOrder.ASC));
            }
            searchSourceBuilder.query(boolQueryBuilder);

            // Size
            searchSourceBuilder.size(pagingSize);
            if(pagingSize == 0 || pagingSize > DEFAULT_SIZE){
                searchSourceBuilder.size(DEFAULT_SIZE);
            }

            searchRequest.source(searchSourceBuilder);

            // Scroll
            searchRequest.scroll(scroll);

            // Query Request & Response
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);

            String scrollId = searchResponse.getScrollId();

            SearchHit[] searchHits = searchResponse.getHits().getHits();

            // Query Result Set ElasticSearchDataListInfo
            ElasticSearchDataListInfo elasticSearchDataListInfo = setData(searchResponse.getHits().getHits(), searchResponse.getHits().getTotalHits());

            int currentPageNum = 1;

            while (searchHits != null && searchHits.length > 0 && pageNum != 1){

                SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId);

                searchScrollRequest.scroll(scroll);
                searchResponse = restHighLevelClient.searchScroll(searchScrollRequest);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();

                if(searchResponse.getHits().getHits().length > 0){
                    currentPageNum++;
                }

                if(currentPageNum == pageNum){
                    elasticSearchDataListInfo = setData(searchResponse.getHits().getHits(), searchResponse.getHits().getTotalHits());
                    break;
                }
            }

            int totalPageCount = (int) Math.ceil((double)elasticSearchDataListInfo.getTotalCount()/(double)pagingSize);
            elasticSearchDataListInfo.setTotalPageCount(totalPageCount);
            elasticSearchDataListInfo.setCurrentPageCount(pageNum);

            elasticSearchDataListInfo.setElapsedMsTime((System.nanoTime()-start)/ 1000000.0);

            if(elasticSearchDataListInfo.getCurrentPageCount() > elasticSearchDataListInfo.getTotalPageCount()){
                elasticSearchDataListInfo.setMessageList(new ArrayList<>());
                elasticSearchDataListInfo.setDataList(new ArrayList<>());
            }

            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest);

            if(!clearScrollResponse.isSucceeded()){
                logger.error("Clear Scroll Response : " + clearScrollResponse.isSucceeded());
            }

            return elasticSearchDataListInfo;

        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();

        }finally {
            try{
                if(restClient != null){
                    restClient.close();
                }
            }catch (Exception e){
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    /**
     * Search
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param time - Standard Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param minusMinute - Standard Time Minus Minute
     * @param hostFilterList - Host List
     * @param fileFilterList - File List
     * @param messageFilter - Message Filter
     * @param sort - Result Sort Default ASC
     * @return
     */
    private static ElasticSearchDataListInfo search(String esHost, int esPort, String time, int minusMinute, ArrayList<String> hostFilterList, ArrayList<String> fileFilterList, String messageFilter, String sort){

        final Scroll scroll = new Scroll(TimeValue.timeValueMinutes(1L));
        RestClient restClient = null;
        try{

            long startTime = System.nanoTime();

            if(hostFilterList == null || ArrayUtils.isEmpty(hostFilterList.toArray())){
                hostFilterList = new ArrayList<>();
            }
            if(fileFilterList == null || ArrayUtils.isEmpty(fileFilterList.toArray())){
                fileFilterList = new ArrayList<>();
            }

            // Connect
            restClient = RestClient.builder(new HttpHost(esHost, esPort,HttpHost.DEFAULT_SCHEME_NAME)).setDefaultHeaders(HEADERS).build();
            RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClient);

            // Indices
            SearchRequest searchRequest = new SearchRequest(INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            // BoolQuery
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.filter(QueryBuilders.rangeQuery(FIELD_TIMESTAMP).gte(getSearchStartTime(time, minusMinute)).lt(getMilliTime(time)));

            // Host Filter
            String hostFilter = StringUtils.join(hostFilterList, " ").trim();
            if(StringUtils.isNotEmpty(hostFilter)){
                boolQueryBuilder.must(QueryBuilders.matchQuery(FIELD_HOST, hostFilter));
            }

            // File Filter
            for(String fileFilter : fileFilterList){
                boolQueryBuilder.should(QueryBuilders.matchPhraseQuery(FIELD_FILE, fileFilter)).minimumShouldMatch(1);
            }
            // Message Filter
            if(StringUtils.isNotEmpty(messageFilter)){
                boolQueryBuilder.must(QueryBuilders.matchPhraseQuery(FIELD_MESSAGE, messageFilter));
            }

            // Order
            if(StringUtils.equals(sort, SORT_ASC)){
                searchSourceBuilder.sort(SortBuilders.fieldSort(FIELD_TIMESTAMP).order(SortOrder.ASC));
            }else if(StringUtils.equals(sort, SORT_DESC)){
                searchSourceBuilder.sort(SortBuilders.fieldSort(FIELD_TIMESTAMP).order(SortOrder.DESC));
            }else {
                searchSourceBuilder.sort(SortBuilders.fieldSort(FIELD_TIMESTAMP).order(SortOrder.ASC));
            }
            searchSourceBuilder.query(boolQueryBuilder);

            // Size
            searchSourceBuilder.size(DEFAULT_SIZE);

            searchRequest.source(searchSourceBuilder);

            // Scroll
            searchRequest.scroll(scroll);

            // Query Request & Response
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);

            String scrollId = searchResponse.getScrollId();
            SearchHit[] searchHits = searchResponse.getHits().getHits();

            // Query Result Set ElasticSearchDataListInfo
            ElasticSearchDataListInfo elasticSearchDataListInfo = setData(searchResponse.getHits().getHits(), searchResponse.getHits().getTotalHits());

            while (searchHits != null && searchHits.length > 0){

                SearchScrollRequest searchScrollRequest = new SearchScrollRequest(scrollId);
                searchScrollRequest.scroll(scroll);

                // Query Request & Response ( scroll )
                searchResponse = restHighLevelClient.searchScroll(searchScrollRequest);
                scrollId = searchResponse.getScrollId();
                searchHits = searchResponse.getHits().getHits();

                // MessageList & ElasticsearchDataInfoList Append
                elasticSearchDataListInfo.getMessageList().addAll(getMessageData(searchResponse.getHits().getHits(), searchResponse.getHits().getTotalHits()));
                elasticSearchDataListInfo.getDataList().addAll(getElasticsearchData(searchResponse.getHits().getHits(), searchResponse.getHits().getTotalHits()));
            }

            elasticSearchDataListInfo.setElapsedMsTime((System.nanoTime()-startTime)/ 1000000.0);

            // Clear Scroll
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = restHighLevelClient.clearScroll(clearScrollRequest);

            if(!clearScrollResponse.isSucceeded()){
                logger.error("Clear Scroll Response : " + clearScrollResponse.isSucceeded());
            }

            return elasticSearchDataListInfo;

        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();

        }finally {
            try{
                if(restClient != null){
                    restClient.close();
                }
            }catch (Exception e){
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    /**
     * Aggregation
     *
     * @param esHost - ElasticSearch HTTP Host
     * @param esPort - ElasticSearch HTTP Port
     * @param aggregation_type - refer to AGGREAGTION_TYPE
     * @param startTime - Aggregation Start Time
     * @param endTime - Aggregation End Time
     * @param period - Period
     * @param periodUnit - d:Day, h:Hour, m:Minute, s:Seconds (refer to PERIOD_UNIT_DAY, PERIOD_UNIT_HOUR, PERIOD_UNIT_MINUTE, PERIOD_UNIT_SECOND)
     * @return
     */
    private static ElasticSearchAggregationListInfo aggregation(String esHost, int esPort, AGGREGATION_TYPE aggregation_type, String startTime, String endTime, int period, String periodUnit){

        RestClient restClient = null;
        long start = System.nanoTime();

        try{

            ElasticSearchAggregationListInfo elasticSearchAggregationListInfo = new ElasticSearchAggregationListInfo();

            elasticSearchAggregationListInfo.setRequestStartTime(startTime);
            elasticSearchAggregationListInfo.setRequestEndTime(endTime);
            elasticSearchAggregationListInfo.setPeriod(period);
            elasticSearchAggregationListInfo.setPeriodUnit(periodUnit);

            restClient = RestClient.builder(new HttpHost(esHost, esPort,HttpHost.DEFAULT_SCHEME_NAME)).setDefaultHeaders(HEADERS).build();
            RestHighLevelClient restHighLevelClient = new RestHighLevelClient(restClient);

            // Indices
            SearchRequest searchRequest = new SearchRequest(INDEX);
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            DateHistogramAggregationBuilder dateHistogramAggregationBuilder = AggregationBuilders.dateHistogram(AGGREGATION_TYPE.TIME_COUNT.name()).field(FIELD_TIMESTAMP).dateHistogramInterval(dateUnitConvert(period,periodUnit)).minDocCount(0).timeZone(DateTimeZone.forID("Asia/Seoul"));

            if(StringUtils.equals(AGGREGATION_TYPE.ALL.name(), aggregation_type.name())){
                searchSourceBuilder.aggregation(dateHistogramAggregationBuilder);
            }else if(StringUtils.equals(AGGREGATION_TYPE.HOST.name(), aggregation_type.name())){
                searchSourceBuilder.aggregation(AggregationBuilders.terms(AGGREGATION_TYPE.HOST.name()).field(AGGREGATION_FIELD_HOST).subAggregation(dateHistogramAggregationBuilder));
            }else if(StringUtils.equals(AGGREGATION_TYPE.FILE.name(), aggregation_type.name())){
                searchSourceBuilder.aggregation(AggregationBuilders.terms(AGGREGATION_TYPE.FILE.name()).field(AGGREGATION_FIELD_FILE).subAggregation(dateHistogramAggregationBuilder));
            }else if(StringUtils.equals(AGGREGATION_TYPE.HOST_FILE.name(), aggregation_type.name())){
                searchSourceBuilder.aggregation(AggregationBuilders.terms(AGGREGATION_TYPE.HOST_FILE.name()).field(AGGREGATION_FIELD_HOST).subAggregation(AggregationBuilders.terms(AGGREGATION_TYPE.FILE.name()).field(AGGREGATION_FIELD_FILE).subAggregation(dateHistogramAggregationBuilder)));
            }else {
                elasticSearchAggregationListInfo.setElapsedMsTime((System.nanoTime()-start)/ 1000000.0);
                return elasticSearchAggregationListInfo;
            }

            // BoolQuery
            BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
            boolQueryBuilder.must(QueryBuilders.matchAllQuery());
            boolQueryBuilder.must(QueryBuilders.rangeQuery(FIELD_TIMESTAMP).gte(getMilliTime(startTime)).lte(getMilliTime(endTime)));

            searchSourceBuilder.query(boolQueryBuilder);

            // Hit Size/
            searchSourceBuilder.size(0);

            searchRequest.source(searchSourceBuilder);

            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);

            if(StringUtils.equals(AGGREGATION_TYPE.ALL.name(), aggregation_type.name())){
                ParsedDateHistogram parsedDateHistogram = (ParsedDateHistogram) searchResponse.getAggregations().asMap().get(AGGREGATION_TYPE.TIME_COUNT.name());

                elasticSearchAggregationListInfo = setAggregationData(elasticSearchAggregationListInfo, AGGREGATION_TYPE.ALL.name(), parsedDateHistogram);
                elasticSearchAggregationListInfo.setElapsedMsTime((System.nanoTime()-start)/ 1000000.0);

            }else if(StringUtils.equals(AGGREGATION_TYPE.HOST.name(), aggregation_type.name())){
                ParsedStringTerms parsedStringTerms = (ParsedStringTerms) searchResponse.getAggregations().asMap().get(AGGREGATION_TYPE.HOST.name());

                elasticSearchAggregationListInfo =  setAggregationData(elasticSearchAggregationListInfo, parsedStringTerms);
                elasticSearchAggregationListInfo.setElapsedMsTime((System.nanoTime()-start)/ 1000000.0);

            }else if(StringUtils.equals(AGGREGATION_TYPE.FILE.name(), aggregation_type.name())){
                ParsedStringTerms parsedStringTerms = (ParsedStringTerms) searchResponse.getAggregations().asMap().get(AGGREGATION_TYPE.FILE.name());

                elasticSearchAggregationListInfo = setAggregationData(elasticSearchAggregationListInfo, parsedStringTerms);
                elasticSearchAggregationListInfo.setElapsedMsTime((System.nanoTime()-start)/ 1000000.0);

            }else if(StringUtils.equals(AGGREGATION_TYPE.HOST_FILE.name(), aggregation_type.name())){
                ParsedStringTerms parsedStringTerms = (ParsedStringTerms) searchResponse.getAggregations().asMap().get(AGGREGATION_TYPE.HOST_FILE.name());

                elasticSearchAggregationListInfo = setAggregationData2(elasticSearchAggregationListInfo, parsedStringTerms);
                elasticSearchAggregationListInfo.setElapsedMsTime((System.nanoTime()-start)/ 1000000.0);

            }else {}

//            return elasticSearchAggregationListInfo;
            return checkAggregationData(elasticSearchAggregationListInfo, aggregation_type.name());


        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));

            ElasticSearchAggregationListInfo elasticSearchAggregationListInfo = new ElasticSearchAggregationListInfo();
            elasticSearchAggregationListInfo.setRequestStartTime(startTime);
            elasticSearchAggregationListInfo.setRequestEndTime(endTime);
            elasticSearchAggregationListInfo.setElapsedMsTime((System.nanoTime()-start)/ 1000000.0);

            return elasticSearchAggregationListInfo;
        }finally {
            try{
                if(restClient != null){
                    restClient.close();
                }
            }catch (Exception e){
                logger.error(ExceptionUtils.getStackTrace(e));
            }
        }
    }

    /**
     * Set Aggregation Data ( 2-Depth ParsedStringTerms Use )
     *
     * @param elasticSearchAggregationListInfo
     * @param parsedStringTerms
     * @return
     */
    private static ElasticSearchAggregationListInfo setAggregationData2(ElasticSearchAggregationListInfo elasticSearchAggregationListInfo, ParsedStringTerms parsedStringTerms){

        try{

            StringBuffer host;
            StringBuffer file;

            for(Iterator hostIter = parsedStringTerms.getBuckets().iterator(); hostIter.hasNext();){
                ParsedStringTerms.ParsedBucket bucket = (ParsedStringTerms.ParsedBucket) hostIter.next();

                ParsedStringTerms innerParsedStringTerms = (ParsedStringTerms) bucket.getAggregations().asMap().get(AGGREGATION_TYPE.FILE.name());

                host = new StringBuffer();
                host.append((String)bucket.getKey());

                for(Iterator fileIter = innerParsedStringTerms.getBuckets().iterator(); fileIter.hasNext();){
                    ParsedStringTerms.ParsedBucket innerBucket = (ParsedStringTerms.ParsedBucket) fileIter.next();

                    file = new StringBuffer(host);
                    file.append("_").append((String)innerBucket.getKey());

                    ParsedDateHistogram parsedDateHistogram = (ParsedDateHistogram) innerBucket.getAggregations().asMap().get(AGGREGATION_TYPE.TIME_COUNT.name());
                    elasticSearchAggregationListInfo = setAggregationData(elasticSearchAggregationListInfo, file.toString(), parsedDateHistogram);
                }
            }

            return elasticSearchAggregationListInfo;
//            return checkAggregationData(elasticSearchAggregationListInfo, null);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchAggregationListInfo();
        }
    }

    /**
     * Set Aggregation Data ( ParsedStringTerms Use )
     *
     * @param elasticSearchAggregationListInfo
     * @param parsedStringTerms
     * @return
     */
    private static ElasticSearchAggregationListInfo setAggregationData(ElasticSearchAggregationListInfo elasticSearchAggregationListInfo, ParsedStringTerms parsedStringTerms){

        try{
            for(Iterator hostIter = parsedStringTerms.getBuckets().iterator(); hostIter.hasNext();){
                ParsedStringTerms.ParsedBucket bucket = (ParsedStringTerms.ParsedBucket) hostIter.next();

                String key = (String)bucket.getKey();

                ParsedDateHistogram parsedDateHistogram = (ParsedDateHistogram) bucket.getAggregations().asMap().get(AGGREGATION_TYPE.TIME_COUNT.name());
                elasticSearchAggregationListInfo = setAggregationData(elasticSearchAggregationListInfo, key, parsedDateHistogram);
            }

            return elasticSearchAggregationListInfo;
//            return checkAggregationData(elasticSearchAggregationListInfo, null);
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchAggregationListInfo();
        }
    }

    /**
     * Set Aggregation Data ( ParsedDateHistogram Use )
     *
     * @param elasticSearchAggregationListInfo
     * @param key
     * @param parsedDateHistogram
     * @return
     */
    private static ElasticSearchAggregationListInfo setAggregationData(ElasticSearchAggregationListInfo elasticSearchAggregationListInfo, String key, ParsedDateHistogram parsedDateHistogram){

        try{
            HashMap<String, ArrayList<Long>> dataMap = new HashMap<>();

            ArrayList<ElasticSearchAggregationInfo> objectList = new ArrayList<>();
            ArrayList<Long> xList = new ArrayList<>();
            ArrayList<Long> yList = new ArrayList<>();

            for(Iterator iter = parsedDateHistogram.getBuckets().iterator(); iter.hasNext();){
                Bucket bucket = (Bucket) iter.next();

                ElasticSearchAggregationInfo elasticSearchAggregationInfo = new ElasticSearchAggregationInfo();

                elasticSearchAggregationInfo.setCount(bucket.getDocCount());
                elasticSearchAggregationInfo.setTime(((DateTime)bucket.getKey()).getMillis());
                elasticSearchAggregationInfo.setTimeStr(bucket.getKeyAsString());

                objectList.add(elasticSearchAggregationInfo);

                xList.add(((DateTime)bucket.getKey()).getMillis());
                yList.add(bucket.getDocCount());
            }

            if(parsedDateHistogram.getBuckets().size() > 0 ){
                dataMap.put("x", xList);
                dataMap.put("y", yList);

                elasticSearchAggregationListInfo.putAggregationMap(key, dataMap);
                elasticSearchAggregationListInfo.putAggregationObjectMap(key, objectList);

                return elasticSearchAggregationListInfo;
//                return checkAggregationData(elasticSearchAggregationListInfo, key);
            }

            return elasticSearchAggregationListInfo;

        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return elasticSearchAggregationListInfo;
        }
    }

    /**
     * Search Result Data Set ( All )
     *
     * @param arrHit
     * @param totalCount
     * @return
     */
    private static ElasticSearchDataListInfo setData(SearchHit[] arrHit, long totalCount){

        ElasticSearchDataListInfo elasticSearchDataListInfo = new ElasticSearchDataListInfo();
        try{
            elasticSearchDataListInfo.setTotalCount(totalCount);

            for(SearchHit hit : arrHit){

                ElasticSearchDataInfo elasticSearchDataInfo = new ElasticSearchDataInfo();

                elasticSearchDataInfo.setTimeStr(String.valueOf(hit.getSource().get("@timestamp")));
                elasticSearchDataInfo.setTime((Long)hit.getSortValues()[0]);
                elasticSearchDataInfo.setHost(String.valueOf(hit.getSource().get("host")));
                elasticSearchDataInfo.setFileName(String.valueOf(hit.getSource().get("file")));

                elasticSearchDataInfo.setMessage(String.valueOf(hit.getSource().get("message")));

                StringBuffer message = new StringBuffer();
                message.append("[").append(elasticSearchDataInfo.getHost()).append(" | ").append(elasticSearchDataInfo.getFileName()).append("]").append(StringUtils.SPACE).append(elasticSearchDataInfo.getMessage());

                elasticSearchDataListInfo.addDataList(elasticSearchDataInfo);
                elasticSearchDataListInfo.addMessageList(message.toString());
            }

            return elasticSearchDataListInfo;

        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ElasticSearchDataListInfo();
        }
    }

    /**
     * Search Result Data Get ( Message List Only )
     *
     * @param arrHit
     * @param totalCount
     * @return
     */
    private static ArrayList<String> getMessageData(SearchHit[] arrHit, long totalCount){

        ElasticSearchDataListInfo elasticSearchDataListInfo = new ElasticSearchDataListInfo();
        try{
            elasticSearchDataListInfo.setTotalCount(totalCount);

            for(SearchHit hit : arrHit){

                ElasticSearchDataInfo elasticSearchDataInfo = new ElasticSearchDataInfo();

                elasticSearchDataInfo.setHost(String.valueOf(hit.getSource().get("host")));
                elasticSearchDataInfo.setFileName(String.valueOf(hit.getSource().get("file")));

                elasticSearchDataInfo.setMessage(String.valueOf(hit.getSource().get("message")));

                StringBuffer message = new StringBuffer();
                message.append("[").append(elasticSearchDataInfo.getHost()).append(" | ").append(elasticSearchDataInfo.getFileName()).append("]").append(StringUtils.SPACE).append(elasticSearchDataInfo.getMessage());

                elasticSearchDataListInfo.addMessageList(message.toString());
            }

            return elasticSearchDataListInfo.getMessageList();

        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ArrayList<>();
        }
    }

    /**
     * Search Result Data Get ( ElasticSearchDataInfo List Only )
     *
     * @param arrHit
     * @param totalCount
     * @return
     */
    private static ArrayList<ElasticSearchDataInfo> getElasticsearchData(SearchHit[] arrHit, long totalCount){

        ElasticSearchDataListInfo elasticSearchDataListInfo = new ElasticSearchDataListInfo();
        try{
            elasticSearchDataListInfo.setTotalCount(totalCount);

            for(SearchHit hit : arrHit){

                ElasticSearchDataInfo elasticSearchDataInfo = new ElasticSearchDataInfo();

                elasticSearchDataInfo.setTimeStr(String.valueOf(hit.getSource().get("@timestamp")));
                elasticSearchDataInfo.setTime((Long)hit.getSortValues()[0]);
                elasticSearchDataInfo.setHost(String.valueOf(hit.getSource().get("host")));
                elasticSearchDataInfo.setFileName(String.valueOf(hit.getSource().get("file")));

                elasticSearchDataInfo.setMessage(String.valueOf(hit.getSource().get("message")));

                elasticSearchDataListInfo.addDataList(elasticSearchDataInfo);
            }

            return elasticSearchDataListInfo.getDataList();

        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return new ArrayList<>();
        }
    }

    /**
     * Print Message ( for debug )
     *
     * @param elasticSearchDataListInfo
     */
    public static void printMessage(ElasticSearchDataListInfo elasticSearchDataListInfo){

        try{

            for(String message : elasticSearchDataListInfo.getMessageList()){
                logger.debug(message);
            }
            logger.debug("Search Count   : " + elasticSearchDataListInfo.getTotalCount());
            logger.debug("Page   Num     : " + elasticSearchDataListInfo.getCurrentPageCount());
            logger.debug("Paging Total   : " + elasticSearchDataListInfo.getTotalPageCount());
            logger.debug("Elapsed Time(s): " + elasticSearchDataListInfo.getElapsedMsTime()/1000);

        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Print Message ( for debug )
     *
     * @param elasticSearchAggregationListInfo
     */
    public static void printAggregationData(ElasticSearchAggregationListInfo elasticSearchAggregationListInfo){
        try{

            for(Iterator<String> iter = elasticSearchAggregationListInfo.getAggregationMap().keySet().iterator(); iter.hasNext();){
                String key = iter.next();

                logger.debug("-------- " + key + " -------- ");
                for(int i=0; i < elasticSearchAggregationListInfo.getAggregationMap().get(key).get("x").size(); i++){
                    logger.debug(DateFormatUtils.format(elasticSearchAggregationListInfo.getAggregationMap().get(key).get("x").get(i), DATE_FORMAT_DEFAULT) + " : " +elasticSearchAggregationListInfo.getAggregationMap().get(key).get("y").get(i));
                }

                logger.error("elapsed time(ms): " + elasticSearchAggregationListInfo.getElapsedMsTime());
            }

        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }

    /**
     * Search Start Time Get
     *
     * @param time - Standard Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param plusMinusMinute - Standard Time Minus Minute
     * @return
     */
    public static String getSearchStartTime(String time, int plusMinusMinute) throws Exception{

//        try{
            LocalDateTime localDateTime = convertLocalDateTime(time, "Asia/Seoul", DATE_FORMAT_DEFAULT);
            localDateTime = localDateTime.minusMinutes(plusMinusMinute);

            long millis = localDateTime.toInstant(ZoneId.systemDefault().getRules().getOffset(localDateTime)).toEpochMilli();

            return String.valueOf(millis);
//        }catch (Exception e){
//            logger.error(ExceptionUtils.getStackTrace(e));
//
//            LocalDateTime localDateTime = convertLocalDateTime(time, "Asia/Seoul", DATE_FORMAT_DEFAULT);
//
//            long millis = localDateTime.toInstant(ZoneId.systemDefault().getRules().getOffset(localDateTime)).toEpochMilli();
//            return String.valueOf(millis);
//        }
    }

    /**
     * Search End Time Get
     *
     * @param time - Standard Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @param plusMinusMinute - Standard Time Plus Minute
     * @return
     */
    public static String getSearchEndTime(String time, int plusMinusMinute) throws Exception{

//        try{

            LocalDateTime localDateTime = convertLocalDateTime(time, "Asia/Seoul", DATE_FORMAT_DEFAULT);
            localDateTime = localDateTime.plusMinutes(plusMinusMinute);

            long millis = localDateTime.toInstant(ZoneId.systemDefault().getRules().getOffset(localDateTime)).toEpochMilli();

            return String.valueOf(millis);
        /*}catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));

            LocalDateTime localDateTime = convertLocalDateTime(time, "Asia/Seoul", DATE_FORMAT_DEFAULT);

            long millis = localDateTime.toInstant(ZoneId.systemDefault().getRules().getOffset(localDateTime)).toEpochMilli();
            return String.valueOf(millis);
        }*/
    }

    /**
     * String Time Convert Milliseconds
     *
     * @param time - Standard Time (Format : yyyy-MM-dd HH:mm:ss.SSS)
     * @return
     */
    public static String getMilliTime(String time) throws Exception{

//        try{
        LocalDateTime localDateTime = convertLocalDateTime(time, "Asia/Seoul", DATE_FORMAT_DEFAULT);
        long millis = localDateTime.toInstant(ZoneId.systemDefault().getRules().getOffset(localDateTime)).toEpochMilli();

        return String.valueOf(millis);
        /*}catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));

            LocalDateTime localDateTime = convertLocalDateTime(time, "Asia/Seoul", DATE_FORMAT_DEFAULT);
            long millis = localDateTime.toInstant(ZoneId.systemDefault().getRules().getOffset(localDateTime)).toEpochMilli();

            return String.valueOf(millis);
        }*/
    }

    /**
     * Converting Other Locale to Current Locale
     * @param strTime
     * @param zoneId
     * @param format
     * @return
     */
    public static LocalDateTime convertLocalDateTime(String strTime, String zoneId, String format) throws Exception{
        LocalDateTime localDateTime = LocalDateTime.parse(strTime, DateTimeFormatter.ofPattern(format));
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(zoneId));

        return LocalDateTime.from(zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()));
    }

    /**
     * Period convert milliseconds
     *
     * @param period
     * @param periodUnit
     * @return
     * @throws Exception
     */
    public static long dateUnitConvertMillis(int period, String periodUnit) throws Exception{

        if(StringUtils.equals(periodUnit, PERIOD_UNIT_DAY)){
            return Duration.of(period, ChronoUnit.DAYS).toMillis();
        }else if(StringUtils.equals(periodUnit, PERIOD_UNIT_HOUR)){
            return Duration.of(period, ChronoUnit.HOURS).toMillis();
        }else if(StringUtils.equals(periodUnit, PERIOD_UNIT_MINUTE)){
            return Duration.of(period, ChronoUnit.MINUTES).toMillis();
        }else if(StringUtils.equals(periodUnit, PERIOD_UNIT_SECOND)){
            return Duration.of(period, ChronoUnit.SECONDS).toMillis();
        }else {
            throw new Exception();
        }
    }

    /**
     * Period convert DateHistogramInterval
     *
     * @param period
     * @param periodUnit
     * @return
     * @throws Exception
     */
    private static DateHistogramInterval dateUnitConvert(int period, String periodUnit) throws Exception{

        if(StringUtils.equals(periodUnit, PERIOD_UNIT_DAY)){
            return DateHistogramInterval.days(period);
        }else if(StringUtils.equals(periodUnit, PERIOD_UNIT_HOUR)){
            return DateHistogramInterval.hours(period);
        }else if(StringUtils.equals(periodUnit, PERIOD_UNIT_MINUTE)){
            return DateHistogramInterval.minutes(period);
        }else if(StringUtils.equals(periodUnit, PERIOD_UNIT_SECOND)){
            return DateHistogramInterval.seconds(period);
        }else {
            throw new Exception();
        }
    }

    /**
     * Period Unit Trancate Date
     *
     * @param strTime
     * @param period
     * @param periodUnit
     * @return
     * @throws Exception
     */
    private static String periodUnitTruncateDate(String strTime, int period, String periodUnit) throws Exception{

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Long.parseLong(getMilliTime(strTime)));

        if(StringUtils.equals(periodUnit, PERIOD_UNIT_DAY)){
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }else if(StringUtils.equals(periodUnit, PERIOD_UNIT_HOUR)){
            if(period == 24){
                calendar.set(Calendar.HOUR, 0);
            }
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }else if(StringUtils.equals(periodUnit, PERIOD_UNIT_MINUTE)){
            if(period == 60){
                calendar.set(Calendar.MINUTE, 0);
            }
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
        }else if(StringUtils.equals(periodUnit, PERIOD_UNIT_SECOND)){
            if(period == 60){
                calendar.set(Calendar.SECOND, 0);
            }
            calendar.set(Calendar.MILLISECOND, 0);
        }else {
            return strTime;
        }

        return DateFormatUtils.format(calendar, DATE_FORMAT_DEFAULT);
    }


    /**
     * Check Aggregation Data
     *
     * @param elasticSearchAggregationListInfo
     * @return
     */
    private static ElasticSearchAggregationListInfo checkAggregationData(ElasticSearchAggregationListInfo elasticSearchAggregationListInfo, String aggregationName){

        ElasticSearchAggregationListInfo elasticSearchAggregationListInfoBackup = elasticSearchAggregationListInfo;

        HashMap<String, HashMap<String, ArrayList<Long>>> aggregationMap = elasticSearchAggregationListInfo.getAggregationMap();
        HashMap<String, ArrayList<ElasticSearchAggregationInfo>> aggregationObjectMap = elasticSearchAggregationListInfo.getAggregationObjectMap();
        try{

            HashMap<String, HashMap<String, ArrayList<Long>>> aggregationMapResult = new HashMap<>();
            HashMap<String, ArrayList<ElasticSearchAggregationInfo>> aggregationObjectMapResult = new HashMap<>();

            // Aggregation Result Empty...
            if(aggregationObjectMap.size() == 0){

                // Aggregation Type ALL Only Set Empty Data
                if(StringUtils.equals(aggregationName, AGGREGATION_TYPE.ALL.name())){
                    return setAggregationDataEmpty(elasticSearchAggregationListInfo);
                }

                return elasticSearchAggregationListInfo;
            }


            for(Iterator<String> iter = aggregationMap.keySet().iterator(); iter.hasNext();){
                String key = iter.next();
                aggregationMapResult.put(key, checkAndDummySetAggregationData(aggregationMap.get(key).get("x"), aggregationMap.get(key).get("y"), elasticSearchAggregationListInfo.getRequestStartTime(), elasticSearchAggregationListInfo.getRequestEndTime(), elasticSearchAggregationListInfo.getPeriod(), elasticSearchAggregationListInfo.getPeriodUnit()));
                aggregationObjectMapResult.put(key, checkAndDummySetAggregationData(aggregationObjectMap.get(key), elasticSearchAggregationListInfo.getRequestStartTime(), elasticSearchAggregationListInfo.getRequestEndTime(), elasticSearchAggregationListInfo.getPeriod(), elasticSearchAggregationListInfo.getPeriodUnit()));
            }

            elasticSearchAggregationListInfo.setAggregationMap(aggregationMapResult);
            elasticSearchAggregationListInfo.setAggregationObjectMap(aggregationObjectMapResult);

            return elasticSearchAggregationListInfo;
        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
            return elasticSearchAggregationListInfoBackup;
        }
    }

    /**
     * Check And Dummy Data Set Aggregation Data (aggregationMap)
     *
     * @param xArray
     * @param yArray
     * @param startTime
     * @param endTime
     * @param period
     * @param periodUnit
     * @return
     * @throws Exception
     */
    private static HashMap<String, ArrayList<Long>> checkAndDummySetAggregationData(ArrayList<Long> xArray, ArrayList<Long> yArray, String startTime, String endTime, int period, String periodUnit) throws Exception{

        HashMap<String, ArrayList<Long>> resultMap = new HashMap<>();

        startTime = periodUnitTruncateDate(startTime, period, periodUnit);
        endTime = periodUnitTruncateDate(endTime, period, periodUnit);

        long startTimeMillis = Long.parseLong(getMilliTime(startTime));
        long endTimeMillis = Long.parseLong(getMilliTime(endTime));

        // Lost Data Nothing..
        if(xArray.get(0) <= startTimeMillis && xArray.get(xArray.size()-1) >= endTimeMillis){
            resultMap.put("x", xArray);
            resultMap.put("y", yArray);

            return resultMap;
        }


        if(xArray.get(0) > startTimeMillis){

            while (xArray.get(0)-dateUnitConvertMillis(period, periodUnit) >= startTimeMillis) {

                long dummyTime = xArray.get(0)-dateUnitConvertMillis(period, periodUnit);

                xArray.add(0, dummyTime);
                yArray.add(0, 0L);
            }
        }

        if(xArray.get(xArray.size()-1) < endTimeMillis){
            while (xArray.get(xArray.size()-1)+dateUnitConvertMillis(period, periodUnit) <= endTimeMillis) {

                long dummyTime = xArray.get(xArray.size()-1)+dateUnitConvertMillis(period, periodUnit);

                xArray.add(dummyTime);
                yArray.add(0L);
            }
        }

        resultMap.put("x", xArray);
        resultMap.put("y", yArray);

        return resultMap;
    }

    /**
     * Check And Dummy Data Set Aggregation Data (aggregationObjectMap)
     *
     * @param objectArray
     * @param startTime
     * @param endTime
     * @param period
     * @param periodUnit
     * @return
     * @throws Exception
     */
    private static ArrayList<ElasticSearchAggregationInfo> checkAndDummySetAggregationData(ArrayList<ElasticSearchAggregationInfo> objectArray, String startTime, String endTime, int period, String periodUnit) throws Exception{

        startTime = periodUnitTruncateDate(startTime, period, periodUnit);
        endTime = periodUnitTruncateDate(endTime, period, periodUnit);

        long startTimeMillis = Long.parseLong(getMilliTime(startTime));
        long endTimeMillis = Long.parseLong(getMilliTime(endTime));

        // Lost Data Nothing..
        if(objectArray.get(0).getTime() <= startTimeMillis && objectArray.get(objectArray.size()-1).getTime() >= endTimeMillis){
            return objectArray;
        }

        if(objectArray.get(0).getTime() > startTimeMillis){

            while (objectArray.get(0).getTime()-dateUnitConvertMillis(period, periodUnit) >= startTimeMillis) {

                long dummyTime = objectArray.get(0).getTime()-dateUnitConvertMillis(period, periodUnit);

                ElasticSearchAggregationInfo elasticSearchAggregationInfo = new ElasticSearchAggregationInfo();

                elasticSearchAggregationInfo.setCount(0L);
                elasticSearchAggregationInfo.setTime(dummyTime);
                elasticSearchAggregationInfo.setTimeStr(DateFormatUtils.format(dummyTime, DATE_FORMAT_DEFAULT));

                objectArray.add(0, elasticSearchAggregationInfo);
            }
        }

        if(objectArray.get(objectArray.size()-1).getTime() < endTimeMillis){

            while (objectArray.get(objectArray.size()-1).getTime()+dateUnitConvertMillis(period, periodUnit) <= endTimeMillis) {

                long dummyTime = objectArray.get(objectArray.size()-1).getTime()+dateUnitConvertMillis(period, periodUnit);

                ElasticSearchAggregationInfo elasticSearchAggregationInfo = new ElasticSearchAggregationInfo();

                elasticSearchAggregationInfo.setCount(0L);
                elasticSearchAggregationInfo.setTime(dummyTime);
                elasticSearchAggregationInfo.setTimeStr(DateFormatUtils.format(dummyTime, DATE_FORMAT_DEFAULT));

                objectArray.add(elasticSearchAggregationInfo);
            }
        }

        return objectArray;
    }

    /**
     * All Aggregation Set Dummy Data
     * @param elasticSearchAggregationListInfo
     * @return
     * @throws Exception
     */
    private static ElasticSearchAggregationListInfo setAggregationDataEmpty(ElasticSearchAggregationListInfo elasticSearchAggregationListInfo) throws Exception{

        String startTime = elasticSearchAggregationListInfo.getRequestStartTime();
        String endTime = elasticSearchAggregationListInfo.getRequestEndTime();

        startTime = periodUnitTruncateDate(startTime, elasticSearchAggregationListInfo.getPeriod(), elasticSearchAggregationListInfo.getPeriodUnit());
        endTime = periodUnitTruncateDate(endTime, elasticSearchAggregationListInfo.getPeriod(), elasticSearchAggregationListInfo.getPeriodUnit());

        long startTimeMillis = Long.parseLong(getMilliTime(startTime));
        long endTimeMillis = Long.parseLong(getMilliTime(endTime));

        HashMap<String, ArrayList<Long>> xyListMap = new HashMap<>();
        ArrayList<Long> xArray = new ArrayList<>();
        ArrayList<Long> yArray = new ArrayList<>();

        xArray.add(startTimeMillis);
        yArray.add(0L);

        while (xArray.get(xArray.size()-1)+dateUnitConvertMillis(elasticSearchAggregationListInfo.getPeriod(), elasticSearchAggregationListInfo.getPeriodUnit()) <= endTimeMillis) {

            long dummyTime = xArray.get(xArray.size()-1)+dateUnitConvertMillis(elasticSearchAggregationListInfo.getPeriod(), elasticSearchAggregationListInfo.getPeriodUnit());

            xArray.add(dummyTime);
            yArray.add(0L);
        }

        xyListMap.put("x", xArray);
        xyListMap.put("y", yArray);

        elasticSearchAggregationListInfo.putAggregationMap(AGGREGATION_TYPE.ALL.name(), xyListMap);

        ArrayList<ElasticSearchAggregationInfo> objectArray = new ArrayList<>();

        ElasticSearchAggregationInfo elasticSearchAggregationInfo = new ElasticSearchAggregationInfo();
        elasticSearchAggregationInfo.setCount(0L);
        elasticSearchAggregationInfo.setTime(startTimeMillis);
        elasticSearchAggregationInfo.setTimeStr(startTime);

        objectArray.add(elasticSearchAggregationInfo);

        while (objectArray.get(objectArray.size()-1).getTime()+dateUnitConvertMillis(elasticSearchAggregationListInfo.getPeriod(), elasticSearchAggregationListInfo.getPeriodUnit()) <= endTimeMillis) {

            long dummyTime = objectArray.get(objectArray.size()-1).getTime()+dateUnitConvertMillis(elasticSearchAggregationListInfo.getPeriod(), elasticSearchAggregationListInfo.getPeriodUnit());

            ElasticSearchAggregationInfo elasticSearchAggregationInfo1 = new ElasticSearchAggregationInfo();

            elasticSearchAggregationInfo1.setCount(0L);
            elasticSearchAggregationInfo1.setTime(dummyTime);
            elasticSearchAggregationInfo1.setTimeStr(DateFormatUtils.format(dummyTime, DATE_FORMAT_DEFAULT));

            objectArray.add(elasticSearchAggregationInfo1);
        }

        elasticSearchAggregationListInfo.putAggregationObjectMap(AGGREGATION_TYPE.ALL.name(), objectArray);

        return elasticSearchAggregationListInfo;
    }

    //TODO: getTime ( Start/End ) - OK
    //TODO: Search ( All, Host, File, Message ) - OK
    //TODO: dataGenerator - OK
    //TODO: paging get - OK
    //TODO: host, port, time, range(minute: ex:4  +-4) - OK
    //TODO: return value ListInfo - OK
    //TODO: search method argument modify( start/end time use ) - OK
    //TODO: Aggregation ( Host, File, Host_File ) - OK
    //TODO: Aggregation Dummy Data Set - OK

    public static void main(String[] args) {


        ArrayList<String> hostList = new ArrayList<>();

        hostList.add("test04");
//        hostList.add("test02");

        ArrayList<String> fileList = new ArrayList<>();

//        fileList.add("messages");
        fileList.add("/var/log/messages");
//        fileList.add("/var/log/dmesg");


//        LogSearchUtil.printMessage(LogSearchUtil.searchFilter("127.0.0.1",9400, "2017-11-13 15:08:00.000", 2, hostList,fileList, null, null, 100, 1));
//        LogSearchUtil.searchFilter("127.0.0.1",9400, "2017-10-18 02:00:00.000", 5, null,null, null, null, 30, 2);

//        LogSearchUtil.aggregationSearch("127.0.0.1",9400, AGGREGATION_TYPE.ALL, "2017-10-18 00:00:00.000",  "2017-10-19 00:00:00.000", 1, PERIOD_UNIT_HOUR);
//        LogSearchUtil.aggregationSearch("127.0.0.1",9400, AGGREGATION_TYPE.HOST, "2017-10-18 00:00:00.000",  "2017-10-19 00:00:00.000", 1, PERIOD_UNIT_HOUR);
//        LogSearchUtil.aggregationSearch("127.0.0.1",9400, AGGREGATION_TYPE.FILE, "2017-10-18 00:00:00.000",  "2017-10-19 00:00:00.000", 1, PERIOD_UNIT_HOUR);
//        LogSearchUtil.aggregation("127.0.0.1",9400, AGGREGATION_TYPE.HOST_FILE, "2017-10-18 00:00:00.000",  "2017-10-19 00:00:00.000", 1, PERIOD_UNIT_HOUR);

//         printAggregationData(LogSearchUtil.aggregationAll("127.0.0.1",9400,"2017-11-01 16:21:00.000",  "2017-11-24 16:21:00.000", 1, PERIOD_UNIT_HOUR));
         printAggregationData(ElasticSearchSearchUtil.aggregationAll("127.0.0.1",9400,"2017-11-07 03:00:00.000",  "2017-11-07 08:00:00.000", 60, PERIOD_UNIT_HOUR));
//         printAggregationData(LogSearchUtil.aggregationHost("127.0.0.1",9400,"2017-11-07 08:00:00.000",  "2017-11-07 03:00:00.000", 1, PERIOD_UNIT_HOUR));
//         printAggregationData(LogSearchUtil.aggregationFile("127.0.0.1",9400,"2017-11-16 15:00:00.000",  "2017-11-16 17:00:00.000", 60, PERIOD_UNIT_MINUTE));
//         printAggregationData(LogSearchUtil.aggregationHostFile("127.0.0.1",9400,"2017-11-07 08:00:00.000",  "2017-11-19 16:00:00.000", 60, PERIOD_UNIT_MINUTE));

        /*try{
            System.out.println(LogSearchUtil.dateUnitConvertMillis(1, PERIOD_UNIT_DAY));

            ArrayList<Long> arrayList = new ArrayList();

            arrayList.add(1L);
            arrayList.add(2L);
            arrayList.add(3L);
            arrayList.add(4L);
            arrayList.add(5L);

            arrayList.add(0, 0L);
            arrayList.add(0, -1L);
            arrayList.add(0, -2L);

            for(Long l : arrayList){
                System.out.println(l);
            }


        }catch (Exception e){
            e.printStackTrace();
        }*/
    }

}
