package com.data7.instdesign.mapper;

import com.data7.instdesign.dto.auth.LoginDTO;
import com.data7.instdesign.dto.auth.RegisterDTO;
import com.data7.instdesign.dto.auth.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

@Mapper
public interface AuthMapper {
    String checkId(String userId);
    String checkEmail(String email);
    Boolean register(RegisterDTO registerDTO);
    UserDTO findByUserId(String userId);
    Boolean saveToken(@Param("userId") String userId, @Param("token") String token);
    String findByToken(String token);
    boolean deleteToken(String userId);
}
