package com.csp.utillib;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Description: 日期时间格式转换类
 * <p>Create Date: 2016/05/15
 * <p>Modify Date: 2016/09/03
 *
 * @author csp
 * @version 1.0.1
 * @since JavaLibrary 1.0.0
 */
public class CalendarFormat {
    public interface Format {
        String DATETIME_FORMAT_0 = "yyyy-MM-dd HH:mm:ss";
        String DATETIME_FORMAT_1 = "yyyy/MM/dd HH:mm:ss";
        String DATETIME_FORMAT_2 = "yyyyMMdd HHmmss";

        String DATE_FORMAT_0 = "yyyy-MM-dd";
        String DATE_FORMAT_1 = "yyyy/MM/dd";
        String DATE_FORMAT_2 = "yyyyMMdd";

        String TIME_FORMAT_0 = "HH:mm:ss";
    }

    /**
     * 本地时间(字符串) -> 本地时间(Calendar)
     *
     * @param dateStr 本地时间(字符串)
     * @param format  上述时间字符串格式
     * @return 本地日历(Calendar)
     * @throws ParseException ParseException
     */
    public static Calendar getCalendar(String dateStr, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.CHINA);
        Date date = sdf.parse(dateStr);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    /**
     * 本地时间(字符串) -> UTC时间(Calendar)
     *
     * @param dateStr 本地时间(字符串)
     * @param format  上述时间字符串格式
     * @return Calendar UTC时间(Calendar)
     * @throws ParseException ParseException
     */
    public static Calendar getUTCCalendar(String dateStr, String format) throws ParseException {
        Calendar calendar = getCalendar(dateStr, format);
        TimeZone timeZone = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, -timeZone.getRawOffset());
        return calendar;
    }

    /**
     * Date -> 日期字符串
     *
     * @param date   指定日期
     * @param format 指定日期字符串的格式
     * @return 日期字符串
     */
    public static String getDateFormat(Date date, String format) {
        return new SimpleDateFormat(format, Locale.CHINA).format(date);
    }

    /**
     * 当前日期 -> 日期字符串
     *
     * @param format 指定日期字符串的格式
     * @return String 日期字符串
     */
    public static String getNowDateFormat(String format) {
        return getDateFormat(new Date(), format);
    }
}
