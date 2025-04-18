package com.data7.instdesign.controller;

import com.data7.instdesign.dto.auth.UserDTO;
import com.data7.instdesign.service.AuthService;
import com.data7.instdesign.util.JSFunc;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        model.addAttribute("redirectURI", request.getSession().getAttribute("redirectURI"));
        log.info(request.getSession().getAttribute("redirectURI"));
        return "auth/login";
    }
    @PostMapping("/login")
    public String loginCheck() {
        return "redirect:/";
    }

    @GetMapping("/register")
    public String register() {
        return "auth/register";
    }

    @PostMapping("/regsiter")
    public String registerCheck() {
        return "redirect:/auth/login";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        UserDTO user = null;
        String userId = "";
        if (session.getAttribute("user") != null) {
            user = (UserDTO) session.getAttribute("user");
            userId = user.getUserId();
        }
        session.invalidate();
        Cookie cookie = new Cookie("rememberMe", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        Cookie cookie1 = new Cookie("JSESSIONID", null);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie1);
        boolean flag = authService.deleteToken(userId);
        if(!flag){
            JSFunc.alert("로그아웃 실패",response);
        }
        return "redirect:/";
    }

}