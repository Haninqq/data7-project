package com.data7.instdesign.mapper;

import com.data7.instdesign.dto.auth.RegisterDTO;
import com.data7.instdesign.dto.auth.UserDTO;
import com.data7.instdesign.dto.search.GoalRequestDTO;
import com.data7.instdesign.dto.search.SaveActivityDTO;
import com.data7.instdesign.dto.search.SaveContentDTO;
import com.data7.instdesign.dto.search.SaveGoalDTO;
import com.data7.instdesign.dto.tools.ToolsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface SearchMapper {
    List<String> getSubject(@Param("gradeCode") String gradeCode);
    List<ToolsDTO> getTools();
    boolean saveGoal(SaveGoalDTO saveGoalDTO);
    boolean saveContent(SaveContentDTO saveContentDTO);
    boolean saveActivity(SaveActivityDTO saveActivityDTO);
    String lastId(@Param("userId") String userId);
}
