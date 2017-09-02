package com.zeroq6.sso.web.client.utils;

import com.zeroq6.sso.web.client.config.PropertyHolder;
import com.zeroq6.sso.web.client.domain.SsoConfigResponseDomain;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.CRC32;

/**
 * Created by icgeass on 2017/2/22.
 */

public class SsoUtils {


    private final static Logger logger = LoggerFactory.getLogger(SsoUtils.class);

    private final static Pattern PATTERN_GROUP_ID = Pattern.compile("^[0-9a-zA-Z]{4,12}$");

    // http://zhangxugg-163-com.iteye.com/blog/1663687
    // 切记，$_SERVER['REMOTE_ADDR']  是由 nginx 传递给 php 的参数，就代表了与当前 nginx 直接通信的客户端的 IP （是不能伪造的）。
    // REMOTE_ADDR放在最前面
    private final static String[] HEADERS_IP_TO_TRY = {
            "REMOTE_ADDR",
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA"
    };

    /**
     * 得到客户端ip
     *
     * @param request
     * @return
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = null;
        for (int i = 0; i < HEADERS_IP_TO_TRY.length; i++) {
            ip = request.getHeader(HEADERS_IP_TO_TRY[i]);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                if (ip.contains(",")) {
                    try {
                        ip = ip.split("\\s*,\\s*")[0].trim();
                    } catch (Exception e) {
                        ip = ip.replaceAll("\\s*,\\s*", " ");
                    }
                }
                return ip;
            }
        }
        ip = request.getRemoteAddr();
        return ip;
    }


    /**
     * 将当前请求链接，包括参数用URLEncoder 指定charset，编码并返回
     *
     * @param request
     * @param ignoreParameterNames
     * @return
     * @throws Exception
     */
    public static String getQueryString(HttpServletRequest request, String... ignoreParameterNames) throws Exception {
        // 传参
        StringBuffer parameters = null;
        // 忽略参数名，传到登录页，不能有ticket的参数名
        List<String> ignoreList = new ArrayList<String>();
        if (null != ignoreParameterNames && ignoreParameterNames.length > 0) {
            for (String string : ignoreParameterNames) {
                ignoreList.add(string);
            }
        }
        // 处理参数
        String queryString = request.getQueryString();
        if (StringUtils.isNotBlank(queryString)) {
            List<String> params = Arrays.asList(queryString.split("&"));
            boolean first = true;
            for (String param : params) {
                String[] kv = param.split("=");
                if (kv.length == 2 && !ignoreList.contains(kv[0])) {
                    if (first) {
                        parameters = new StringBuffer();
                        parameters.append('?').append(kv[0]).append('=').append(kv[1]);
                        first = false;
                    } else {
                        parameters.append('&').append(kv[0]).append('=').append(kv[1]);
                    }
                }
            }
        }
        return parameters != null ? parameters.toString() : null;
    }


    public static boolean validateGroupId(String groupId) {
        return null == groupId ? false : PATTERN_GROUP_ID.matcher(groupId).matches();
    }


    /**
     * 如果groupId不合法，返回SsoConfigResponseDomain.DEFAULT_SSO_GROUP_ID，否则返回原groupId
     * <p/>
     * 没有groupId就传null
     *
     * @param groupId
     * @return
     */
    public static String getGroupId(String groupId) {
        return SsoUtils.validateGroupId(groupId) ? groupId : SsoConfigResponseDomain.DEFAULT_SSO_GROUP_ID;
    }


    public static String getCookieDomain(HttpServletRequest request) {
        if (("." + request.getServerName()).contains(PropertyHolder.getSsoConfigResponseDomain().getPrimaryDomain())) {
            return PropertyHolder.getSsoConfigResponseDomain().getPrimaryDomain();
        }
        return request.getServerName();
    }

    public static String wrapReturnUrl(String returnUrl){
        return toCrc32String(returnUrl, false) + "," + returnUrl;
    }

    public static String unwrapReturnUrl(String returnUrl){
        String[] arr = returnUrl.split(",", 2);
        if(!toCrc32String(arr[1], false).equals(arr[0])){
            return null;
        }
        return arr[1];
    }

    public static void outString(HttpServletResponse response, String responseBody) throws Exception{
        PrintWriter pw = response.getWriter();
        pw.write(responseBody);
        pw.close();
    }

    public static String toCrc32String(String string, boolean upperCase){
        if(null == string){
            throw new RuntimeException("string can not be null");
        }
        try {
            CRC32 crc32 = new CRC32();
            crc32.update(string.getBytes("utf-8"));
            String result = Long.toHexString(crc32.getValue());
            return upperCase ? result.toUpperCase() : result;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


}
