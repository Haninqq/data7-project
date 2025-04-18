package com.data7.instdesign.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class LoginCheckFilter implements Filter {

    // 정확히 일치해야 통과되는 URI들
    private static final List<String> exactMatchList = List.of(
            "/", "/auth/login", "/auth/register", "/favicon.ico"
    );

    // 이 경로들로 시작하면 통과되는 URI들
    private static final List<String> prefixMatchList = List.of(
            "/auth/api/", "/css/", "/js/", "/image/", "/logo/", "/tools/"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpReq = (HttpServletRequest) request;
        HttpServletResponse httpRes = (HttpServletResponse) response;
        HttpSession session = httpReq.getSession(false);

        String uri = httpReq.getRequestURI();

        if (uri.equals("/search")) {
            if (session == null) {
                session = httpReq.getSession(true); // 필요할 때만 생성
            }
            session.setAttribute("redirectURI", uri);
        }

        boolean isWhite =
                exactMatchList.contains(uri) ||
                        prefixMatchList.stream().anyMatch(uri::startsWith);

        boolean isLoggedIn = session != null && session.getAttribute("user") != null;

        if (!isWhite && !isLoggedIn) {
            httpRes.sendRedirect("/auth/login");
            return;
        }

        chain.doFilter(request, response);
    }
}
