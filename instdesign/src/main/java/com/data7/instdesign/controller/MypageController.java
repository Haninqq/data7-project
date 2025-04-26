package com.data7.instdesign.controller;

import com.data7.instdesign.dto.auth.UserDTO;
import com.data7.instdesign.dto.mypage.SavedGoalsDTO;
import com.data7.instdesign.service.AuthService;
import com.data7.instdesign.service.MypageService;
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

import java.util.List;

@Controller
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/mypage")
public class MypageController {

    private final MypageService mypageService;
    @GetMapping("")
    public String myPage(Model model, HttpSession session) {
        UserDTO user = (UserDTO) session.getAttribute("user");
        List<SavedGoalsDTO> savedGoalsList = mypageService.getGoals(user.getUserId());
        model.addAttribute("goalsList", savedGoalsList);
        return "mypage/mypage";
    }


}