package com.data7.instdesign.service;

import com.data7.instdesign.dto.auth.LoginDTO;
import com.data7.instdesign.dto.auth.RegisterDTO;
import com.data7.instdesign.dto.auth.UserDTO;
import com.data7.instdesign.dto.search.SaveActivityDTO;
import com.data7.instdesign.dto.search.SaveContentDTO;
import com.data7.instdesign.dto.search.SaveGoalDTO;
import com.data7.instdesign.dto.tools.ToolsDTO;
import com.data7.instdesign.mapper.AuthMapper;
import com.data7.instdesign.mapper.SearchMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Log4j2
@RequiredArgsConstructor
public class SearchService {
    private final SearchMapper searchMapper;

    public List<String> getSubject(String gradeCode){
        return searchMapper.getSubject(gradeCode);
    }
    public List<ToolsDTO> getTools(){
        return searchMapper.getTools();
    }
    public boolean saveGoal(SaveGoalDTO saveGoalDTO){
        return searchMapper.saveGoal(saveGoalDTO);
    }
    public boolean saveContent(SaveContentDTO saveContentdto){
        return searchMapper.saveContent(saveContentdto);
    }
    public boolean saveActivity(SaveActivityDTO saveActivityDTO){
        return searchMapper.saveActivity(saveActivityDTO);
    }
    public String lastId(HttpSession session){
        UserDTO user = (UserDTO)session.getAttribute("user");
        String id = user.getUserId();
        return searchMapper.lastId(id);
    }
}
