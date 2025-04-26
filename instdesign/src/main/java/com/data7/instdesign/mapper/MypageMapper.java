package com.data7.instdesign.mapper;

import com.data7.instdesign.dto.mypage.SavedGoalsDTO;
import com.data7.instdesign.dto.tools.ToolsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface MypageMapper {
    List<SavedGoalsDTO> getGoals(@Param("userId") String userId);

}
