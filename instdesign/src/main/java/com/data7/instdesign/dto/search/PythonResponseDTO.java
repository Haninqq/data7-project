package com.data7.instdesign.dto.search;

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
public class PythonResponseDTO {
    private List<OpenAIResponseDTO> gptResults;
    private ContentResponseDTO contentResults;
}