package com.data7.instdesign.mapper;

import com.data7.instdesign.dto.auth.RegisterDTO;
import com.data7.instdesign.dto.auth.UserDTO;
import com.data7.instdesign.dto.tools.RelatedToolsDTO;
import com.data7.instdesign.dto.tools.ToolsDTO;
import com.data7.instdesign.dto.tools.YTLinksDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.repository.query.Param;

import java.util.List;

@Mapper
public interface ToolsMapper {
    List<ToolsDTO> getToolsList();
    ToolsDTO getToolById(@Param("id") int id);
    List<RelatedToolsDTO> getRelatedToolsList(@Param("id") int id);
    List<YTLinksDTO> getYTLinksList(@Param("id") int id);
    String getToolNameById(@Param("id") int id);
}
