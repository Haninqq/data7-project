package com.data7.instdesign.mapper;

import com.data7.instdesign.dto.mypage.SavedGoalsDTO;
import com.data7.instdesign.dto.search.SaveActivityDTO;
import com.data7.instdesign.dto.search.SaveContentDTO;
import com.data7.instdesign.dto.tools.ToolsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface MypageMapper {
    List<SavedGoalsDTO> getGoals(@Param("userId") String userId);
    List<SaveActivityDTO> getActivities(@Param("goalId") String goalId);
    List<SaveContentDTO> getContents(@Param("goalId") String goalId);
    SavedGoalsDTO getSavedGoals(@Param("goalId") String goalId);
    boolean deleteGoal(@Param("goalId") String goalId);
    SavedGoalsDTO findGoalById(@Param("goalId") String goalId);
}
