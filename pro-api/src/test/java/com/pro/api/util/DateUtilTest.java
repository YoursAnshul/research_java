package com.pro.api.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class DateUtilTest {


    @Test
    public void convertToTimeZoneStringTest() {
        String dateInString = "01-22-2015 10:15:55 AM";
        String cds = DateUtil.convertToTimeZoneString(dateInString,"Asia/Singapore","America/New_York");
        System.out.println(cds);
        Assertions.assertEquals(cds,"01-21-2015 09:15:55 PM");
    }

    @Test
    public void convertToTimeZoneStringTestForUTC() {
        //
        String dateInString = "01-22-2015 10:15:55 AM";
        String cds = DateUtil.convertToTimeZoneString(dateInString,"Asia/Singapore","UTC");
        System.out.println(cds);
        Assertions.assertEquals(cds,"01-22-2015 02:15:55 AM"); // Asia is GMT+8
    }

    @Test
    public void convertToTimeZoneStringTestForEurope() {
        //
        String dateInString = "01-22-2015 10:15:55 AM";
        String cds = DateUtil.convertToTimeZoneString(dateInString,"Asia/Singapore","Europe/London");
        System.out.println(cds);
        Assertions.assertEquals(cds,"01-22-2015 02:15:55 AM"); // Europe is Asia -8 .
    }

    @Test
    public void convertToTimeZoneStringTestAmerica() {
        String dateInString = "01-22-2015 10:15:55 AM";
        String cds = DateUtil.convertToTimeZoneString(dateInString,"Europe/London","America/New_York");
        System.out.println(cds);
        Assertions.assertEquals(cds,"01-22-2015 05:15:55 AM"); // Europe is America -4or5 .
    }


}
