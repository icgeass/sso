package com.zeroq6.sso.web.client.utils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CookieUtils {

    public static String get(HttpServletRequest req, String key) {
        Cookie cookie = getCookie(req, key);
        if (null == cookie) {
            return null;
        }
        return cookie.getValue();
    }


    // http://stackoverflow.com/questions/4620172/expires-string-in-cookie-header
    // sso.ticket=1dc387a9e001c38b06c0b58afb25fece86c969e368a6c877e72e6395642372f6; Domain=www.a.com; Expires=Sun, 26-Mar-2017 21:39:22 GMT; Path=/
    public static void set(HttpServletResponse resp, Cookie cookie) {
        StringBuffer sb = new StringBuffer("");
        String suffix = "; ";
        // cookie name-value
        sb.append(cookie.getName() + "=" + cookie.getValue() + suffix);
        // domain
        String domain = cookie.getDomain();
        if (null != domain && domain.trim().length() != 0) {
            sb.append("Domain=" + cookie.getDomain() + suffix);
        }
        // expire
        String expireString = null;
        DateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss zzz", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        Date expire = new Date();
        if (cookie.getMaxAge() > 0) {
            expire.setTime(expire.getTime() + (cookie.getMaxAge() * 1000));
            expireString = df.format(expire);
        } else if (cookie.getMaxAge() == 0) {
            expire.setTime(10000L);
            expireString = df.format(expire);
        }
        if (null != expireString) {
            sb.append("Expires=" + expireString + suffix);
        }
        // path
        String path = cookie.getPath();
        if (null != path && path.trim().length() != 0) {
            sb.append("Path=" + cookie.getPath() + suffix);
        }
        // HttpOnly
        sb.append("HttpOnly");
        resp.addHeader("Set-Cookie", sb.toString());
        // resp.addCookie(cookie);
    }


    public static void delete(HttpServletRequest req, HttpServletResponse resp, String key, String domain, String path) {
        Cookie cookie = getCookie(req, key);
        if (null != cookie) {
            cookie.setMaxAge(0);
            cookie.setValue(null);
            if (null != domain && domain.trim().length() != 0) {
                cookie.setDomain(domain);
            }
            if (null != path && path.trim().length() != 0) {
                cookie.setPath(path);
            }
            resp.addCookie(cookie);
        }

    }

    public static Cookie getCookie(HttpServletRequest req, String key) {
        Cookie[] cookies = req.getCookies();
        if (null != cookies) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(key)) {
                    return cookie;
                }
            }
        }
        return null;
    }

}
