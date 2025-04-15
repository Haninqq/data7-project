package com.data7.instdesign.service;

import com.data7.instdesign.dto.auth.LoginDTO;
import com.data7.instdesign.dto.auth.RegisterDTO;
import com.data7.instdesign.dto.auth.UserDTO;
import com.data7.instdesign.dto.tools.RelatedToolsDTO;
import com.data7.instdesign.dto.tools.ToolsDTO;
import com.data7.instdesign.mapper.AuthMapper;
import com.data7.instdesign.mapper.ToolsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class ToolsService {

    private final ToolsMapper toolsMapper;

    public List<ToolsDTO> getToolsList(){
        return toolsMapper.getToolsList();
    }

    public ToolsDTO getToolById(Integer id) {
        ToolsDTO toolsDTO = toolsMapper.getToolById(id);

        List<RelatedToolsDTO> relatedToolsList = toolsMapper.getRelatedToolsList(id);
        relatedToolsList.forEach(relatedToolsDTO -> {
            relatedToolsDTO.setRelatedToolName(
                    toolsMapper.getToolNameById(relatedToolsDTO.getRelatedToolId())
            );
        });
        log.info("relatedToolsList: {}", relatedToolsList);

        toolsDTO.setRelatedTools(relatedToolsList);
        toolsDTO.setYoutubeLinks(toolsMapper.getYTLinksList(id));

        return toolsDTO;
    }
}
