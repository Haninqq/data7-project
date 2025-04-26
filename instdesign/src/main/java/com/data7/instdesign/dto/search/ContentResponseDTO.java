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
public class ContentResponseDTO {
    private String topic;
    private String subtitle;
    private String title;
    private String url;
    private Integer similarity;
    private String message;
}