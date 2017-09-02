
package com.zeroq6.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MyDateUtils {

    public final static SimpleDateFormat yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd");

    public final static SimpleDateFormat yyyyMMddHHmmss = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static String yyyyMMdd(Date date){
        return yyyyMMdd.format(date);
    }

    public static String yyyyMMddHHmmss(Date date){
        return yyyyMMddHHmmss.format(date);
    }

    public static String format(Date date){
        return yyyyMMddHHmmss(date);
    }

    public static String format(Date date, String pattern){
        return new SimpleDateFormat(pattern).format(date);
    }

    public static Date parse(String dateString, String pattern){
        try{
            return new SimpleDateFormat(pattern).parse(dateString);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


}
