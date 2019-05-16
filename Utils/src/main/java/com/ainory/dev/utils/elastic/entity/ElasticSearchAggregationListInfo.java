package com.ainory.dev.utils.elastic.entity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author ainory on 2017. 10. 19..
 */
public class ElasticSearchAggregationListInfo {

    private double elapsedMsTime = 0.0;

    //               key   ,         x or y, value
    private  HashMap<String, HashMap<String, ArrayList<Long>>> aggregationMap = new HashMap<>();
    private  HashMap<String, ArrayList<ElasticSearchAggregationInfo>> aggregationObjectMap = new HashMap<>();

    private String requestStartTime;
    private String requestEndTime;

    private int period;
    private String periodUnit;

    public double getElapsedMsTime() {
        return elapsedMsTime;
    }

    public void setElapsedMsTime(double elapsedMsTime) {
        this.elapsedMsTime = elapsedMsTime;
    }

    public HashMap<String, HashMap<String, ArrayList<Long>>> getAggregationMap() {
        return aggregationMap;
    }

    public void setAggregationMap(HashMap<String, HashMap<String, ArrayList<Long>>> aggregationMap) {
        this.aggregationMap = aggregationMap;
    }

    public void putAggregationMap(String key, HashMap<String, ArrayList<Long>> aggregationMap) {
        this.aggregationMap.put(key, aggregationMap);
    }

    public HashMap<String, ArrayList<ElasticSearchAggregationInfo>> getAggregationObjectMap() {
        return aggregationObjectMap;
    }

    public void setAggregationObjectMap(HashMap<String, ArrayList<ElasticSearchAggregationInfo>> aggregationObjectMap) {
        this.aggregationObjectMap = aggregationObjectMap;
    }

    public void putAggregationObjectMap(String key, ArrayList<ElasticSearchAggregationInfo> aggregationObjectMap) {
        this.aggregationObjectMap.put(key, aggregationObjectMap);
    }

    public String getRequestStartTime() {
        return requestStartTime;
    }

    public void setRequestStartTime(String requestStartTime) {
        this.requestStartTime = requestStartTime;
    }

    public String getRequestEndTime() {
        return requestEndTime;
    }

    public void setRequestEndTime(String requestEndTime) {
        this.requestEndTime = requestEndTime;
    }

    public int getPeriod() {
        return period;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public String getPeriodUnit() {
        return periodUnit;
    }

    public void setPeriodUnit(String periodUnit) {
        this.periodUnit = periodUnit;
    }
}
