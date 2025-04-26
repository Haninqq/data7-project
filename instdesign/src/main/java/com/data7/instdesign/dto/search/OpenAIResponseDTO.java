package com.data7.instdesign.dto.search;

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
public class OpenAIResponseDTO {
    private String activity_title;
    private String tool_name;
    private String activity_desc;
    private String activity_sentence;
}