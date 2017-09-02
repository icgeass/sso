
package com.zeroq6.common.web;



import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ContextHolder {

    private final static ThreadLocal<HttpServletRequest> requestThreadLocal = new ThreadLocal<HttpServletRequest>();

    private final static ThreadLocal<HttpServletResponse> responseThreadLocal = new ThreadLocal<HttpServletResponse>();


    private ContextHolder(){}

    public static HttpServletRequest getRequest(){
        return requestThreadLocal.get();
    }

    public static void setRequest(HttpServletRequest req){
        requestThreadLocal.set(req);
    }

    public static HttpServletResponse getResponse(){
        return responseThreadLocal.get();
    }

    public static void setResponse(HttpServletResponse resp){
        responseThreadLocal.set(resp);
    }


}
