package com.data7.instdesign.controller;

import com.data7.instdesign.dto.ApiResponse;
import com.data7.instdesign.dto.auth.*;
import com.data7.instdesign.dto.search.GoalRequestDTO;
import com.data7.instdesign.dto.search.GoalResponseDTO;
import com.data7.instdesign.service.AuthService;
import com.data7.instdesign.service.SearchService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/search/api")
public class ApiSearchController {

    private final SearchService searchService;

    @Autowired
    private WebClient.Builder webClientBuilder;

    @PostMapping("/subject")
    public ResponseEntity<ApiResponse<List<String>>> getSubject(@RequestBody Map<String, String> request) {
        try{
            String gradeCode = request.get("gradeCode");
            List<String> subjectList = searchService.getSubject(gradeCode);
            if(!subjectList.isEmpty()){
                return ResponseEntity.ok(ApiResponse.ok(subjectList));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.fail("과목 출력 실패. 다시 시도해주세요."));
            }
        } catch (Exception e){
            log.error("과목 출력 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("서버 내부 오류가 발생했습니다"));
        }
    }

    @PostMapping("/goal")
    public Mono<ApiResponse<String>> receiveGoalAndResponseAPI(@RequestBody GoalRequestDTO request) {
        try {
            // grade parsing
            String[] gradeMap = {
                    "초1", "초2", "초3", "초4", "초5", "초6",
                    "중1", "중2", "중3",
                    "고1", "고2", "고3"
            };

            int gradeNum = Integer.parseInt(request.getGrade());
            if (gradeNum >= 1 && gradeNum <= 12) {
                request.setGrade(gradeMap[gradeNum - 1]);
            }
            log.info("request:{}", request);

            // FastAPI 서버로 요청 보내기
            String fastApiUrl = "http://127.0.0.1:8000/submit/";
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(request);
            log.info("Generated JSON: {}", json);

            // WebClient로 POST 요청
            WebClient webClient = webClientBuilder.baseUrl(fastApiUrl).build();

            return webClient
                    .post()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)  // JSON 헤더 설정
                    .bodyValue(json)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> {
                        log.info("Response from FastAPI: {}", response);
                        // 여기서 그냥 Return type에 맞춰서 ObjectMapper쓰면 되는 거 아님?
                        return ApiResponse.ok(response);
                    })
                    .onErrorResume(e -> {
                        log.error("Error occurred while calling FastAPI: ", e);
                        return Mono.just(ApiResponse.fail("OpenAI API와의 커넥션 이슈. 다시 시도해주세요."));
                    });

        } catch (Exception e) {
            // 여기서 ApiResponse.fail()에 제네릭 타입을 명시
            return Mono.just(ApiResponse.fail("OpenAI API와의 커넥션 이슈. 다시 시도해주세요."));
        }
    }






}