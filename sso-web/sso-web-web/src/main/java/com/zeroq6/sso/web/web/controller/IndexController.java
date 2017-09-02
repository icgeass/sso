package com.zeroq6.sso.web.web.controller;
/**
 * Created by icgeass on 2017/3/27.
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@Controller
@RequestMapping("/")
public class IndexController {



    @Value("${sso.loginUrl}")
    private String loginUrl;

    @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.POST})
    public String index() throws Exception {
        return "redirect:" + loginUrl;
    }
}