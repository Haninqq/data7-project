package com.data7.instdesign.service;

import com.data7.instdesign.dto.auth.LoginDTO;
import com.data7.instdesign.dto.auth.RegisterDTO;
import com.data7.instdesign.dto.auth.UserDTO;
import com.data7.instdesign.mapper.AuthMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Log4j2
@RequiredArgsConstructor
public class AuthService {
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;

    public boolean checkId(String userId){
        String ifExists = authMapper.checkId(userId);
        return ifExists == null;
    }

    public boolean checkEmail(String email){
        String ifExists = authMapper.checkEmail(email);
        return ifExists == null;
    }

    public boolean register(RegisterDTO registerDTO){

        //암호화
        String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());
        registerDTO.setPassword(encodedPassword);

        return authMapper.register(registerDTO);
    }

    public UserDTO login(LoginDTO loginDTO) {
        UserDTO userDTO = authMapper.findByUserId(loginDTO.getUserId());
        authMapper.deleteToken(loginDTO.getUserId());

        if (userDTO == null) {
            return null;
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), userDTO.getPassword())) {
            return null;
        }

        return userDTO;
    }

    public boolean saveToken(String userId, String token) {
        return authMapper.saveToken(userId, token);
    }

    public UserDTO cookieCheck(String token){
        String userId = authMapper.findByToken(token);
        return authMapper.findByUserId(userId);
    }

    public boolean deleteToken(String userId) {
        return authMapper.deleteToken(userId);

    }
}
