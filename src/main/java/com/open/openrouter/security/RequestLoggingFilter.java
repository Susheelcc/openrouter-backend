package com.open.openrouter.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class RequestLoggingFilter implements Filter {

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        System.out.println("Incoming Request:");
        System.out.println("Method: " + httpRequest.getMethod());
        System.out.println("Path: " + httpRequest.getRequestURI());

        chain.doFilter(request, response);
    }
}