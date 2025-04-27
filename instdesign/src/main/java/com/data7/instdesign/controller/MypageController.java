package com.data7.instdesign.controller;

import com.data7.instdesign.dto.ApiResponse;
import com.data7.instdesign.dto.auth.UserDTO;
import com.data7.instdesign.dto.mypage.SavedGoalsDTO;
import com.data7.instdesign.dto.search.SaveActivityDTO;
import com.data7.instdesign.dto.search.SaveContentDTO;
import com.data7.instdesign.dto.tools.ToolsDTO;
import com.data7.instdesign.service.AuthService;
import com.data7.instdesign.service.MypageService;
import com.data7.instdesign.util.JSFunc;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/results/{goalId}")
    public String view(@PathVariable String goalId, Model model) {


        String[] gradeMap = {
                "초1", "초2", "초3", "초4", "초5", "초6",
                "중1", "중2", "중3",
                "고1", "고2", "고3"
        };


        List<SaveContentDTO> contentList = mypageService.getContents(goalId);
        List<SaveActivityDTO> activityList = mypageService.getActivities(goalId);
        SavedGoalsDTO goals = mypageService.getSavedGoals(goalId);
        int gradeNum = Integer.parseInt(goals.getGrade());
        if (gradeNum >= 1 && gradeNum <= 12) {
            goals.setGrade(gradeMap[gradeNum - 1]);
        }
        model.addAttribute("goal", goals);
        model.addAttribute("contentList", contentList);
        model.addAttribute("activityList", activityList);
        return "mypage/view";
    }
    @ResponseBody
    @PostMapping("/delete/{goalId}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable String goalId, HttpSession session, HttpServletResponse response) {
        response.setCharacterEncoding("UTF-8");
        try {
            UserDTO user = (UserDTO)session.getAttribute("user");
            if(user == null){
                JSFunc.alertBack("로그인하세요", response);
            }
            String userId = user.getUserId();
            boolean flag = mypageService.deleteGoal(goalId, userId);
            if(flag){
                return ResponseEntity.ok(ApiResponse.ok("삭제 성공"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.fail("삭제 실패"));
            }
        } catch(Exception e){
            log.error(e);
            return ResponseEntity.badRequest().body(ApiResponse.fail("삭제 실패"));
        }

    }

}