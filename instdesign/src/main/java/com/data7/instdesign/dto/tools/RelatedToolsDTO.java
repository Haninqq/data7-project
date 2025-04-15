package com.data7.instdesign.dto.tools;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Log4j2
public class RelatedToolsDTO {
    private Integer toolId;
    private Integer relatedToolId;
    private String relatedToolName;
}
