package com.zeroq6.sso.web.web.controller;
/**
 * Created by icgeass on 2017/3/27.
 */

import com.zeroq6.sso.web.client.domain.SsoConfigResponseDomain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;


@Controller
@RequestMapping("/")
public class IndexController {


    @Autowired
    private SsoConfigResponseDomain ssoConfigResponseDomain;

    @RequestMapping(value = "", method = {RequestMethod.GET, RequestMethod.POST})
    public String index() throws Exception {
        return "redirect:" + ssoConfigResponseDomain.getLoginUrl();
    }
}