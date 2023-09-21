package com.socialmedia.clover_network.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeHelper {
    private static org.slf4j.Logger logger = LoggerFactory.getLogger(TimeHelper.class);

    public static SimpleDateFormat dtf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat dtf2 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSS");
    public static SimpleDateFormat dtf3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    public static SimpleDateFormat dtf4 = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    public static SimpleDateFormat df5 = new SimpleDateFormat("dd-MM-yyyy");

    public static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");


    /**
     * @param formatTime: format by pattern: "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
     * @return
     * @throws ParseException
     */
    public static Date parseDateTime(String formatTime) throws ParseException {
        Date parse = df.parse(formatTime);
        Calendar instance = Calendar.getInstance();
        instance.setTime(parse);
        return instance.getTime();
    }

    public static Date formatDateTime(String dateTime) throws ParseException {
        if (StringUtils.isNotEmpty(dateTime)) {
            dateTime = dateTime.replace("/", "-");
            try {
                return df.parse(dateTime);
            } catch (Exception e) {
                return df5.parse(dateTime);
            }
        }

        return new Date();
    }

    public static Date formatDateTimeNew(String dateTime) throws ParseException {
        Date parse = dtf2.parse(dateTime);
        return parse;
    }

    public static String convertFormatDateTime(String dateTime) throws ParseException {
        String format = dtf2.format(dtf3.parse(dateTime));
        return format;
    }

    public static boolean compareDateTime(String dateTimeStart, String dateTimeEnd) throws ParseException {
        boolean result = false;
        Date parseStart = dtf2.parse(dateTimeStart);
        Date parseEnd = dtf2.parse(dateTimeEnd);
        if (parseStart.before(parseEnd)) {
            result = true;
        }
        return result;
    }

    public static String toUTC(String formatTime) throws ParseException {
        Date parse = dtf2.parse(formatTime);
        return toUTC(parse);
    }

    public static String toUTC(Date parse) throws ParseException {
        return dtf2.format(toUTCDate(parse));
    }


    public static Date toUTCDate(Date parse) throws ParseException {
        Calendar instance = Calendar.getInstance();
        instance.setTime(parse);
        instance.add(Calendar.HOUR, -7);
        return instance.getTime();
    }

    public static Date fromUTCDate(Date parse) throws ParseException {
        Calendar instance = Calendar.getInstance();
        instance.setTime(parse);
        instance.add(Calendar.HOUR, +7);
        return instance.getTime();
    }

    public static long toEpoch(String formatTime) throws ParseException {
        Date date = dtf.parse(formatTime);
        long epoch = date.getTime();
        return epoch;
    }

    public static String fromUTC(String dateString) {
        try {
            Date date = dtf2.parse(dateString);
            Calendar instance = Calendar.getInstance();
            instance.setTime(date);
            instance.add(Calendar.HOUR, +7);
            return formatDateTime(instance.getTime());
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return null;

    }


    public static String formatDateTime(Date i) {
        return dtf.format(i);
    }

    public static String formatDateTimeToUTC(Date parse) throws ParseException {
        return dtf.format(toUTCDate(parse));
    }

    public static String nowString() {
        return formatDateTime(new Date());
    }

    public static String currentDateTime() {
        Date dNow = new Date();
        return dtf.format(dNow);
    }

    public static String convertStartOfThisDay(String date) throws ParseException {
        Date parse = df.parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(parse);
        resetCalendar(cal);
        return dtf.format(cal.getTime());
    }

    public static String convertEndOfThisDay(String date) throws ParseException {
        Date parse = df.parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(parse);
        resetCalendar(cal);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return dtf.format(cal.getTime());
    }

    public static String formatStartOfThisDay() {
        return dtf.format(startOfThisDay());
    }

    public static String formatEndOfThisDay() {
        return dtf.format(endOfThisDay());
    }

    public static String currentDateTimeZone() {
        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        System.out.println("Date time hien tai: " + ft.format(dNow));
        return ft.format(dNow);
    }

    public static String currentDateTimeFormat() {
        Date dNow = new Date();
        System.out.println("Date time hien tai: " + dtf2.format(dNow));
        return dtf2.format(dNow);
    }

    private static Calendar resetCalendar(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        return cal;
    }

    private static Calendar baseCalendar() {
        Calendar cal = resetCalendar(Calendar.getInstance());
        return cal;
    }

    public static Date after90Days(long date) throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(date);
        cal.add(Calendar.DATE, 90);
        return cal.getTime();
    }

    public static String add90Days() throws ParseException {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 90);
        return dtf2.format(cal.getTime());
    }

    public static Date startOfThisDay() {
        Calendar cal = baseCalendar();
        return cal.getTime();
    }

    public static Date startOfThisDay(String date) throws ParseException {
        Date parse = df.parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(parse);
        resetCalendar(cal);
        return cal.getTime();
    }

    public static Date endOfThisDay(String date) throws ParseException {
        Date parse = df.parse(date);
        Calendar cal = Calendar.getInstance();
        cal.setTime(parse);
        resetCalendar(cal);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    public static Date endOfThisDay() {
        Calendar cal = baseCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    public static Date startOfThisWeek() {
        Calendar cal = baseCalendar();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        return cal.getTime();
    }

    public static Date startOfNextWeek() {
        Calendar cal = baseCalendar();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        return cal.getTime();
    }

    public static Date endOfNextWeek() {
        Calendar cal = baseCalendar();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.add(Calendar.WEEK_OF_YEAR, 1);
        cal.add(Calendar.DAY_OF_WEEK, 5);
        cal.add(Calendar.SECOND, -1);
        return cal.getTime();
    }

    public static Date endOfThisWorkWeek() {
        Calendar cal = baseCalendar();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.add(Calendar.DAY_OF_WEEK, 5);
        cal.add(Calendar.SECOND, -1);
        return cal.getTime();
    }

    public static Date startOfThisMonth() {
        Calendar cal = baseCalendar();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal.getTime();
    }

    public static Date startOfThisYear() {
        Calendar cal = baseCalendar();
        cal.set(Calendar.DAY_OF_YEAR, 1);
        return cal.getTime();
    }

    public static Date startOfNextMonth() {
        Calendar cal = baseCalendar();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.add(Calendar.MONTH, 1);
        return cal.getTime();
    }

    public static String calculateDayOfWork(String parseTime) {
        if (parseTime == null || parseTime.isEmpty()) return "0";

        try {
            Date dateTime = TimeHelper.parseDateTime(parseTime);
            Date curr = new Date();
            long diff = curr.getTime() - dateTime.getTime();
            if (diff == 0 || diff < 0) return "0";

            long differenceInYears = (diff / (1000l * 60 * 60 * 24 * 365));
            long differenceInDays = (diff / (1000 * 60 * 60 * 24)) % 365;
            String yearOfWork;
            if (differenceInYears == 0) {
                yearOfWork = "";
            } else if (differenceInYears == 1) {
                yearOfWork = differenceInYears + " Year";
            } else {
                yearOfWork = differenceInYears + " Years";
            }
            String dayString;
            if (differenceInDays == 0) {
                dayString = "";
            } else if (differenceInDays == 1) {
                dayString = differenceInDays + " Day";
            } else {
                dayString = differenceInDays + " Days";
            }

            return yearOfWork + dayString;
        } catch (ParseException ex) {
            logger.info("Error: " + ex.getMessage());
            return "0";
        }
    }

    public static String calculateDateDOW(String parseDate) {
        if (parseDate == null || parseDate.isEmpty()) return "0";

        try {
            Date dateTime = TimeHelper.formatDateTime(parseDate);
            Date curr = new Date();
            long diff = curr.getTime() - dateTime.getTime();
            if (diff == 0 || diff < 0) return "0";

            long differenceInYears = (diff / (1000l * 60 * 60 * 24 * 365));
            long differenceInDays = (diff / (1000 * 60 * 60 * 24)) % 365;
            String yearOfWork;
            if (differenceInYears == 0) {
                yearOfWork = "";
            } else if (differenceInYears == 1) {
                yearOfWork = differenceInYears + " Year ";
            } else {
                yearOfWork = differenceInYears + " Years ";
            }
            String dayString;
            if (differenceInDays == 0) {
                dayString = "";
            } else if (differenceInDays == 1) {
                dayString = differenceInDays + " Day";
            } else {
                dayString = differenceInDays + " Days";
            }

            return yearOfWork + dayString;
        } catch (ParseException ex) {
            logger.info("Error: " + ex.getMessage());
            return "0";
        }
    }

    public static String getCurrentDate() {
        Date date = Calendar.getInstance().getTime();
        DateFormat dateFormat = new SimpleDateFormat("dd");
        return dateFormat.format(date);
    }

    public static String convertStartDate(String dateFrom) {
        try {
            Date startTime = startOfThisDay(dateFrom);
            Date startUTC = toUTCDate(startTime);

            return formatDateTime(startUTC);
        } catch (Exception e) {
            return dateFrom;
        }
    }

    public static String convertEndDate(String dateTo) {
        try {
            Date endTime = endOfThisDay(dateTo);
            Date startUTC = toUTCDate(endTime);

            return formatDateTime(startUTC);
        } catch (Exception e) {
            return dateTo;
        }
    }

    public static String excelFormatDate(long date) {
        return dtf4.format(date);
    }

    public static long convertToTimeStamp(String dateTimeStr) {
        try {
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = formatter.parse(dateTimeStr);
            Timestamp timeStampDate = new Timestamp(date.getTime());
            return timeStampDate.getTime();
        } catch (Exception e) {
            logger.info("[convertToTimeStamp] convert date to timestamp fail | dateTime: " + dateTimeStr);
            return 0;
        }
    }

}
