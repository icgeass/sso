package com.zeroq6.common.xss;

import org.apache.commons.lang3.StringEscapeUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Created by icgeass on 2016/12/18.
 */


public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    public XssHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getHeader(String name) {
        return StringEscapeUtils.escapeHtml4(transferEncoding(super.getHeader(name)));
    }

    @Override
    public String getQueryString() {
        String result = StringEscapeUtils.escapeHtml4(transferEncoding(super.getQueryString()));
        return null != result ? result.replace("&amp;", "&") : result;
    }

    @Override
    public String getParameter(String name) {
        return StringEscapeUtils.escapeHtml4(transferEncoding(super.getParameter(name)));
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (values != null) {
            int length = values.length;
            String[] escapeValues = new String[length];
            for (int i = 0; i < length; i++) {
                escapeValues[i] = StringEscapeUtils.escapeHtml4(transferEncoding(values[i]));
            }
            return escapeValues;
        }
        return null;
    }


    private String transferEncoding(String string){
        if(null == string){
            return null;
        }
        try{
            if("POST".equals(super.getMethod())){
                return string;
            }
            return new String(string.getBytes("iso-8859-1"), super.getCharacterEncoding() == null ? "utf-8" : super.getCharacterEncoding());
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

}
