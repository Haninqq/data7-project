package com.data7.instdesign.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.List;

public class LoginCheckFilter implements Filter {

    // 로그인 없이 접근 가능한 경로들 (정적 자원, 로그인, 회원가입 등)
    private static final List<String> whiteList = List.of(
            "/auth/login",
            "/auth/register",
            "/auth/api/",
            "/css/", "/js/", "/images/", "/favicon.ico", "/logo/"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;
        HttpSession session = httpReq.getSession(false);

        String uri = httpReq.getRequestURI();

        boolean isLoginRequired = whiteList.stream().noneMatch(uri::startsWith);

        if (isLoginRequired && (session == null || session.getAttribute("user") == null)) {
            httpRes.sendRedirect("/auth/login");
            return;
        }

        chain.doFilter(request, response);
    }
}
