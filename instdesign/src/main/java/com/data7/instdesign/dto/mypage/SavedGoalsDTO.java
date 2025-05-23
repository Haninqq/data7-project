package com.data7.instdesign.dto.mypage;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Log4j2
public class SavedGoalsDTO {
    private int id;
    private String userId;
    private String grade;
    private String subject;
    private String goal;
    private LocalDateTime createdAt;
}
