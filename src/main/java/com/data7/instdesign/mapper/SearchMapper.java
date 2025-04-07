package com.data7.instdesign.mapper;

import com.data7.instdesign.dto.auth.RegisterDTO;
import com.data7.instdesign.dto.auth.UserDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface SearchMapper {
    List<String> getSubject(@Param("gradeCode") String gradeCode);
}
