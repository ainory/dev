package com.ainory.dev.utils.http;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by ainory on 2016. 6. 21..
 */
public class HttpRequestUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpRequestUtil.class);

    /**
     * Get request
     *
     * @param url
     * @param retryCnt
     * @param connTimeout
     * @param requestTimeout
     * @param socketTimeout
     * @return
     */
    public static String sendGET(String url, int retryCnt, int connTimeout, int requestTimeout, int socketTimeout)  {

        if(StringUtils.isEmpty(url)){
            logger.error("Url is Null!!");
            return null;
        }

        int cnt = 0;
        while (true){

            logger.debug( (cnt>0)?"[retry - "+cnt+"] "+url : url);
            try{
//                RequestConfig config = RequestConfig.custom().setConnectTimeout(PropertiesUtil.getInt("http.timeout.second") * 1000)
//                        .setConnectionRequestTimeout(PropertiesUtil.getInt("http.timeout.second") * 1000)
//                        .setSocketTimeout(PropertiesUtil.getInt("http.timeout.second") * 1000)
//                        .build();
                RequestConfig config = RequestConfig.custom().setConnectTimeout(connTimeout * 1000)
                        .setConnectionRequestTimeout(requestTimeout * 1000)
                        .setSocketTimeout(socketTimeout * 1000)
                        .build();

                CloseableHttpClient httpClient = HttpClientBuilder.create()
                        .setDefaultRequestConfig(config)
                        .build();

                HttpGet httpGet = new HttpGet(url);
                CloseableHttpResponse httpResponse = httpClient.execute(httpGet);

                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }
                reader.close();
                httpClient.close();

                return StringUtils.contains(response.toString(),"<html>") ? null : response.toString();
            }catch (Exception e){
                logger.error(ExceptionUtils.getMessage(e)+" - "+ ((cnt>0)?"[retry - "+cnt+"] "+url : url));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }

            if(cnt == retryCnt){
                return null;
            }
            cnt++;
        }
    }

    /**
     * Post Request
     *
     * @param url
     * @param body
     * @param retryCnt
     * @param connTimeout
     * @param requestTimeout
     * @param socketTimeout
     * @return
     */
    public static String sendPOST(String url, String body, int retryCnt, int connTimeout, int requestTimeout, int socketTimeout)  {

        int cnt = 0;
        while (true){

            logger.debug( (cnt>0)?"[retry - "+cnt+"] "+url : url);
            try{
                RequestConfig config = RequestConfig.custom().setConnectTimeout(connTimeout * 1000)
                        .setConnectionRequestTimeout(requestTimeout * 1000)
                        .setSocketTimeout(socketTimeout * 1000)
                        .build();

                CloseableHttpClient httpClient = HttpClientBuilder.create()
                        .setDefaultRequestConfig(config)
                        .build();

                HttpPost httpPost = new HttpPost(url);
                httpPost.setEntity(new StringEntity(body));
                CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

                BufferedReader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));

                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = reader.readLine()) != null) {
                    response.append(inputLine);
                }
                reader.close();
                httpClient.close();

                return StringUtils.contains(response.toString(),"<html>") ? null : response.toString();
            }catch (Exception e){
                logger.error(ExceptionUtils.getMessage(e)+" - "+ ((cnt>0)?"[retry - "+cnt+"] "+url : url));
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }

            if(cnt == retryCnt){
                return null;
            }
            cnt++;
        }
    }

    public static void main(String[] args) {
        try{

//            logger.debug(DateFormatUtils.format(getSearchMilliSeconds(0),DateFormatUtils.ISO_DATETIME_FORMAT.getPattern()+".SSS") +" - " + getSearchMilliSeconds(15));
//            logger.debug(DateFormatUtils.format(getSearchMilliSeconds(15),DateFormatUtils.ISO_DATETIME_FORMAT.getPattern()+".SSS") +" - " + getSearchMilliSeconds(30));

            /*logger.debug(RestUtil.convertAppToJob("application_1456939661858_3759120"));
            logger.debug(RestUtil.getApplicationListUrl("sun-001-02","8088"));
            logger.debug(RestUtil.getApplicationUrl("http://sun-001-02:8088/proxy/application_1456939661858_3760870/","application_1456939661858_3760870" ));
            logger.debug(RestUtil.getJobListUrl("sun-001-02","19888"));
            logger.debug(RestUtil.getJobCountersUrl("sun-001-02","19888", "job_1456939661858_3759120"));
            logger.debug(RestUtil.getJobConfUrl("sun-001-02","19888","job_1456939661858_3759120"));


            logger.debug(DateFormatUtils.format(new Date(1466499592279L), DateFormatUtils.ISO_DATETIME_FORMAT.getPattern()+".SSS"));

            logger.debug(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(1466500289111L-1466500153182L)));*/

        }catch (Exception e){
            logger.error(ExceptionUtils.getStackTrace(e));
        }
    }
}
