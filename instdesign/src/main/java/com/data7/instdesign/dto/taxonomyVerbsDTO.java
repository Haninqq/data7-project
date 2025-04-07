package com.data7.instdesign.dto;

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
public class taxonomyVerbsDTO {
    private Integer id;
    private String categoryId;
    private String verbEng;
    private String verbKor;
    private String desc;
}
