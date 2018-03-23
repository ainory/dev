package com.ainory.dev.utils.elastic.entity;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;

/**
 * Created by ainory on 2017. 9. 26..
 */
public class ElasticSearchDataListInfo {

    private long totalCount = 0;
    private int totalPageCount = 0;
    private int currentPageCount = 0;
    private double elapsedMsTime = 0.0;

    private ArrayList<ElasticSearchDataInfo> dataList = new ArrayList<>();
    private ArrayList<String> messageList = new ArrayList<>();

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(long totalCount) {
        this.totalCount = totalCount;
    }

    public int getTotalPageCount() {
        return totalPageCount;
    }

    public void setTotalPageCount(int totalPageCount) {
        this.totalPageCount = totalPageCount;
    }

    public int getCurrentPageCount() {
        return currentPageCount;
    }

    public void setCurrentPageCount(int currentPageCount) {
        this.currentPageCount = currentPageCount;
    }

    public double getElapsedMsTime() {
        return elapsedMsTime;
    }

    public void setElapsedMsTime(double elapsedMsTime) {
        this.elapsedMsTime = elapsedMsTime;
    }

    public ArrayList<ElasticSearchDataInfo> getDataList() {
        return dataList;
    }

    public void addDataList(ElasticSearchDataInfo elasticSearchDataInfo) {
        dataList.add(elasticSearchDataInfo);
    }

    public void setDataList(ArrayList<ElasticSearchDataInfo> dataList) {
        this.dataList = dataList;
    }

    public ArrayList<String> getMessageList() {
        return messageList;
    }

    public void setMessageList(ArrayList<String> messageList) {
        this.messageList = messageList;
    }

    public void addMessageList(String message) {
        messageList.add(message);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("totalCount", totalCount)
                .append("totalPageCount", totalPageCount)
                .append("currentPageCount", currentPageCount)
                .append("dataList", dataList)
                .append("messageList", messageList)
                .toString();
    }
}
