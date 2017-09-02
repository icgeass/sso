
package com.zeroq6.common.web;


import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ContextBindingFilter implements Filter{

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        ContextHolder.setRequest(((HttpServletRequest)request));
        ContextHolder.setResponse(((HttpServletResponse)response));
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
