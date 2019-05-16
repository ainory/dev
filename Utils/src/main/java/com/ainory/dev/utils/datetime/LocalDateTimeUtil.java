package com.ainory.dev.utils.datetime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

/**
 * Created by ainory on 2016. 11. 3..
 *
 * LocalDateTimeUtil use Java 8
 */
public class LocalDateTimeUtil {

    private static final String ZONE_ID_UTC = "UTC";
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * Converting UTC Time to Current Locale
     * @param strTime format: yyyy-MM-ddTHH:mm:ss.SSSZ
     * @return
     */
    public static LocalDateTime convertUTCtoLocalDateTime(String strTime){
        ZonedDateTime zonedDateTime = Instant.parse(strTime).atZone(ZoneId.of(ZONE_ID_UTC));

        return LocalDateTime.from(zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()));
    }

    /**
     * Converting Other Locale to Current Locale
     * @param strTime
     * @param zoneId
     * @param format
     * @return
     */
    public static LocalDateTime convertLocalDateTime(String strTime, String zoneId, String format){
        LocalDateTime localDateTime = LocalDateTime.parse(strTime, DateTimeFormatter.ofPattern(format));
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.of(zoneId));

        return LocalDateTime.from(zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()));
    }

    /**
     * Converting UTC Time to Milliseconds
     * @param strTime
     * @return
     */
    public static long convertUTCtoMillis(String strTime){
        LocalDateTime localDateTime = convertUTCtoLocalDateTime(strTime);

        return localDateTime.toInstant(ZoneId.systemDefault().getRules().getOffset(localDateTime)).toEpochMilli();
    }

    /**
     * Converting UTC Time to Milliseconds
     * @param localDateTime
     * @return
     */
    public static long convertUTCtoMillis(LocalDateTime localDateTime){

        return localDateTime.toInstant(ZoneId.systemDefault().getRules().getOffset(localDateTime)).toEpochMilli();
    }

    public static LocalDateTime convertMillisToLocalDateTime(long millis) {
        if (millis == 0)
            return null;
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), TimeZone.getDefault().toZoneId());
    }

    /**
     * Str time Plus Millis
     * @param strTime
     * @param millis
     * @return
     */
    public static LocalDateTime getPlusMillisLocalDateTime(String strTime, long millis){
        return convertUTCtoLocalDateTime(strTime).plus(millis, ChronoUnit.MILLIS);
    }

    /**
     * LocalDateTime Diff
     * @param diffTime1
     * @param diffTime2
     * @return diffTime1 > diffTime2 return negative
     *         diffTime1 < diffTime2 return positive
     *         diffTime1 == diffTime2 return 0
     */
    public static long getLocalDateTimeSecondDiff(LocalDateTime diffTime1, LocalDateTime diffTime2){
        return ChronoUnit.SECONDS.between(diffTime1,diffTime2);
    }

    /**
     * LocalDateTime Diff
     * @param diffTime1
     * @param diffTime2
     * @return diffTime1 > diffTime2 return negative
     *         diffTime1 < diffTime2 return positive
     *         diffTime1 == diffTime2 return 0
     */
    public static long getLocalDateTimeMillisDiff(LocalDateTime diffTime1, LocalDateTime diffTime2){
        return ChronoUnit.MILLIS.between(diffTime1,diffTime2);
    }

    /**
     * Converting LocalDateTime to String
     * @param localDateTime
     * @param format
     * @return
     */
    public static String getStringLocalDateTime(LocalDateTime localDateTime, String format){
        return localDateTime.format(DateTimeFormatter.ofPattern(format));
    }

    /**
     * Converting LocalDateTime to String
     * @param localDateTime
     * @return
     */
    public static String getStringLocalDateTime(LocalDateTime localDateTime){
        return getStringLocalDateTime(localDateTime, DEFAULT_DATE_FORMAT);
    }



    public static void main(String[] args) {

//        String time = "2016-11-03T00:21:56.877Z";
        String time = "2016-11-03 00:21:56.877";

//        System.out.println(time);
//        System.out.println(convertLocalDateTime(time, "Australia/Darwin", DEFAULT_DATE_FORMAT));
//        System.out.println(convertLocalDateTime(time, "Asia/Seoul", DEFAULT_DATE_FORMAT));

        String time2 = "2016-11-08T09:29:55.443Z";

        /*ZoneId.systemDefault().getRules().getOffset(convertUTCtoLocalDateTime(time2));
        System.out.println(convertUTCtoLocalDateTime(time2).toInstant(ZoneId.systemDefault().getRules().getOffset(convertUTCtoLocalDateTime(time2))).toEpochMilli());
        System.out.println(DateFormatUtils.format(convertUTCtoLocalDateTime(time2).toInstant(ZoneId.systemDefault().getRules().getOffset(convertUTCtoLocalDateTime(time2))).toEpochMilli(), "yyyy-MM-dd HH:mm:ss.SSS"));

        System.out.println(DateFormatUtils.format(Instant.parse(time2).toEpochMilli(), "yyyy-MM-dd HH:mm:ss.SSS"));*/

//        System.out.println(convertUTCtoLocalDateTime(time2).getLong(ChronoField.));

        /*System.out.println(convertUTCtoLocalDateTime(time));
        System.out.println(getPlusMillisLocalDateTime(time, 12103));
        System.out.println(getStringLocalDateTime(getPlusMillisLocalDateTime(time, 12103)));*/



    }
}
