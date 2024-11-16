package com.pro.api.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {
    private static final String DATE_FORMAT = "MM-dd-yyyy hh:mm:ss a";
    private static final DateTimeFormatter formatter =  DateTimeFormatter.ofPattern(DATE_FORMAT);
    private DateUtil(){// do not instantiate

    }


    public static String convertToTimeZoneString(String dateString, String fromTimeZone,String toTimeZone){

        LocalDateTime ldt = LocalDateTime.parse(dateString,formatter);
        ZoneId zone = ZoneId.of(fromTimeZone);
        ZonedDateTime zdt = ldt.atZone(zone);
        ZoneId tzdt = ZoneId.of(toTimeZone);
        ZonedDateTime ctz = zdt.withZoneSameInstant(tzdt);
        return formatter.format(ctz);
    }

    public static Date convertToTimeZoneDate(String dateString, String fromTimeZone,String toTimeZone){

        LocalDateTime ldt = LocalDateTime.parse(dateString,formatter);
        ZoneId zone = ZoneId.of(fromTimeZone);
        ZonedDateTime zdt = ldt.atZone(zone);
        ZoneId tzdt = ZoneId.of(toTimeZone);
        ZonedDateTime ctz = zdt.withZoneSameInstant(tzdt);
        return Date.from(ctz.toInstant());
    }
}
