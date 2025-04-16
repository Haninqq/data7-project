package com.data7.instdesign.controller;

import com.data7.instdesign.dto.auth.UserDTO;
import com.data7.instdesign.service.AuthService;
import com.data7.instdesign.util.JSFunc;
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
@RequestMapping("/mypage")
public class MypageController {

    @GetMapping("")
    public String myPage(Model model, HttpServletRequest request) {
        return "mypage/mypage";
    }


}