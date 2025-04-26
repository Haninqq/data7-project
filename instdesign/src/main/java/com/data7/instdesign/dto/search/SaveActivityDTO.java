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
public class SaveActivityDTO {
    private String goalId;
    private String toolId;
    private String toolName;
    private String activitySentence;
    private String activityDesc;
}
