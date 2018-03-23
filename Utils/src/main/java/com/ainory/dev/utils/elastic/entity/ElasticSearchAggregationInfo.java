package com.ainory.dev.utils.elastic.entity;

/**
 * @author ainory on 2017. 10. 19..
 */
public class ElasticSearchAggregationInfo {

    private String timeStr;
    private long time;
    private long count;

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
