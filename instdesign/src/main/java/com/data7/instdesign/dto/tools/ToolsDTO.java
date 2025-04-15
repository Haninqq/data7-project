package com.data7.instdesign.dto.tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Log4j2
public class ToolsDTO {
    private Integer id;
    private String name;
    private String description;
    private String url;
    private String usageExample;
    private List<YTLinksDTO> youtubeLinks;
    private List<RelatedToolsDTO> relatedTools;
}
