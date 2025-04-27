package com.data7.instdesign.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Log4j2
public class SaveGoalDTO {
    private String grade;
    private String subject;
    private String goal;
    private String userId;
    private Date createdAt;
}
