package com.data7.instdesign.service;

import com.data7.instdesign.dto.mypage.SavedGoalsDTO;
import com.data7.instdesign.dto.search.SaveActivityDTO;
import com.data7.instdesign.dto.search.SaveContentDTO;
import com.data7.instdesign.dto.tools.ToolsDTO;
import com.data7.instdesign.mapper.MypageMapper;
import com.data7.instdesign.mapper.SearchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class MypageService {
    private final MypageMapper mypageMapper;

    public List<SavedGoalsDTO> getGoals(String userId) {
        List<SavedGoalsDTO> goals = mypageMapper.getGoals(userId);

        for (SavedGoalsDTO goal : goals) {
            int gradeNumber = Integer.parseInt(goal.getGrade());
            String gradeText = mapGradeToText(gradeNumber);
            goal.setGrade(gradeText);
        }

        return goals;
    }

    private String mapGradeToText(int grade) {
        return switch (grade) {
            case 1 -> "초1";
            case 2 -> "초2";
            case 3 -> "초3";
            case 4 -> "초4";
            case 5 -> "초5";
            case 6 -> "초6";
            case 7 -> "중1";
            case 8 -> "중2";
            case 9 -> "중3";
            case 10 -> "고1";
            case 11 -> "고2";
            case 12 -> "고3";
            default -> "알 수 없음";
        };
    }
    public List<SaveContentDTO> getContents(String goalId) {
        return mypageMapper.getContents(goalId);
    }
    public List<SaveActivityDTO> getActivities(String goalId) {
        return mypageMapper.getActivities(goalId);
    }
    public SavedGoalsDTO getSavedGoals(String goalId) {
        return mypageMapper.getSavedGoals(goalId);
    }
    public boolean deleteGoal(String goalId, String userId) {
        SavedGoalsDTO goal = mypageMapper.findGoalById(goalId);

        if (goal == null) {
            return false;
        }

        if (!goal.getUserId().equals(userId)) {
            return false;
        }

        return mypageMapper.deleteGoal(goalId);
    }

}
