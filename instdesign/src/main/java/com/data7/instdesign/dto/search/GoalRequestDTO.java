package com.data7.instdesign.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Log4j2
public class GoalRequestDTO {
    private String grade;
    private String subject;
    private String goal;
}
