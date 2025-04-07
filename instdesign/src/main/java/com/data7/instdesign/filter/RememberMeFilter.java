package com.data7.instdesign.filter;

import com.data7.instdesign.dto.auth.UserDTO;
import com.data7.instdesign.service.AuthService;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

public class RememberMeFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public RememberMeFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            Cookie[] cookies = request.getCookies();

            if (cookies != null) {
                Arrays.stream(cookies)
                    .filter(cookie -> "rememberMe".equals(cookie.getName()))
                    .findFirst()
                    .ifPresent(cookie -> {
                        String token = cookie.getValue();
                        UserDTO user = authService.cookieCheck(token);

                        if (user != null) {
                            HttpSession newSession = request.getSession(true);
                            newSession.setAttribute("user", user);  // 세션 로그인 처리
                        }
                    });
            }
        }

        filterChain.doFilter(request, response);
    }
}
