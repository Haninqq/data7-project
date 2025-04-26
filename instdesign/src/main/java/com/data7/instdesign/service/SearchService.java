package com.data7.instdesign.service;

import com.data7.instdesign.dto.auth.LoginDTO;
import com.data7.instdesign.dto.auth.RegisterDTO;
import com.data7.instdesign.dto.auth.UserDTO;
import com.data7.instdesign.dto.tools.ToolsDTO;
import com.data7.instdesign.mapper.AuthMapper;
import com.data7.instdesign.mapper.SearchMapper;
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

}
