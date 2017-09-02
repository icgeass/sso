
package com.zeroq6.common.utils;

import org.apache.commons.lang3.StringUtils;


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

    public static String findDomain(String url){
        String domainCookie = null;
        if(StringUtils.isBlank(url)){
            throw new RuntimeException("传入请求URL不能为空");
        }
        url = url.replace("https://", "").replace("http://", "");
        return url.substring(0, url.indexOf("/") < 0 ? url.length(): url.indexOf("/"));

    }

    // 销毁cookie的时候不需要，因为子域始终能访问父域下的cookie，销毁时不需要带域名
    public static String findDomainPrimary(String url){
        url = findDomain(url);
        int count = MyStringUtils.findSubStringTimes(url, ".");
        if(count > 1){
            // 查找主域
            String tmp = url.substring(0, url.lastIndexOf("."));
            url = url.substring(tmp.lastIndexOf("."), url.length());
        }else if(count == 1){
            url = "." + url;
        }
        return url;
    }


}
