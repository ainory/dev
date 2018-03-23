package com.ainory.dev.utils.elastic.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by ainory on 2017. 9. 26..
 */
public class ElasticSearchDataInfo {

    private String timeStr;
    private long time;
    private String fileName;
    private String host;
    private String message;

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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("timeStr", timeStr)
                .append("time", time)
                .append("fileName", fileName)
                .append("host", host)
                .append("message", message)
                .toString();
    }
}
