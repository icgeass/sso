
package com.zeroq6.common.utils;

/**
 * 取这个名字是因为还没见到三方框架用这个名字
 */
public class MyStringUtils {


    public static String format(String template, Object[] objects) {
        String re = String.valueOf(template); // if null then "null";
        if(null != objects){
            for (int i = 0; i < objects.length; i++) {
                re = re.replace("{" + i + "}", null == objects[i] ? "null" : objects[i].toString());
            }
        }
        return re;
    }


    public static int findSubStringTimes(String str, String findStr) {
        if (null == str || null == findStr) {
            throw new RuntimeException("str和findStr不能为null");
        }
        int lastIndex = 0;
        int count = 0;
        int findStrLength = findStr.length();
        while ((lastIndex = str.indexOf(findStr, lastIndex)) != -1) {
            count++;
            lastIndex += findStrLength;
        }
        return count;
    }



}
