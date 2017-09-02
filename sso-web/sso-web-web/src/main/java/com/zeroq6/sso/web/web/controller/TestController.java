package com.zeroq6.sso.web.web.controller;
/**
 * Created by icgeass on 2017/3/25.
 */

import com.zeroq6.sso.web.client.utils.CookieUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


//@Controller
//@RequestMapping("/test")
public class TestController {

    private final Logger logger = LoggerFactory.getLogger(getClass());



    //@RequestMapping(value = "/cookie", method = {RequestMethod.GET, RequestMethod.POST})
    //@ResponseBody
    public String login(HttpServletRequest request, HttpServletResponse response, Model view) throws Exception {
        Cookie cookie = new Cookie("key", "value");
        cookie.setPath("/");
        cookie.setDomain("6zeroq.com");
        cookie.setMaxAge(60);
        response.addCookie(cookie);
        CookieUtils.set(response, cookie);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        CookieUtils.set(response, cookie);
        cookie.setMaxAge(-1);
        response.addCookie(cookie);
        CookieUtils.set(response, cookie);
        return System.currentTimeMillis()+"";
    }
}