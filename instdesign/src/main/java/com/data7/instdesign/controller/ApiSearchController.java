package com.data7.instdesign.controller;

import com.data7.instdesign.dto.ApiResponse;
import com.data7.instdesign.dto.auth.*;
import com.data7.instdesign.dto.search.*;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.webauthn.api.PublicKeyCose;
import org.springframework.web.bind.annotation.*;
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
    public Mono<ApiResponse<PythonResponseDTO>> receiveGoalAndResponseAPI(@RequestBody GoalRequestDTO request) {
        try {
            String[] gradeMap = {
                    "초1", "초2", "초3", "초4", "초5", "초6",
                    "중1", "중2", "중3",
                    "고1", "고2", "고3"
            };

            int gradeNum = Integer.parseInt(request.getGrade());
            if (gradeNum >= 1 && gradeNum <= 12) {
                request.setGrade(gradeMap[gradeNum - 1]);
            }
            log.info("request: {}", request);

            String fastApiUrl = "http://127.0.0.1:8000/submit/";
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(request);
            log.info("Generated JSON: {}", json);

            WebClient webClient = webClientBuilder.baseUrl(fastApiUrl).build();

            return webClient
                    .post()
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(json)
                    .retrieve()
                    .bodyToMono(PythonResponseDTO.class)
                    .map(response -> {
                        log.info("FastAPI 응답 수신 완료. GPT 결과 수: {}, Content 결과 수: {}, content : {}, gptResult : {}",
                                response.getGptResults() != null ? response.getGptResults().size() : 0,
                                response.getContentResults() != null ? response.getContentResults().size() : 0,
                                response.getContentResults(),
                                response.getGptResults());
                        return ApiResponse.ok(response);  // ApiResponse<CombinedResponseDTO>
                    })
                    .onErrorResume(e -> {
                        log.error("FastAPI 호출 중 예외 발생: ", e);
                        return Mono.just(ApiResponse.fail("OpenAI API와의 커넥션 이슈. 다시 시도해주세요."));
                    });

        } catch (Exception e) {
            log.error("예외 발생: ", e);
            return Mono.just(ApiResponse.fail("OpenAI API와의 커넥션 이슈. 다시 시도해주세요."));
        }
    }

    @PostMapping("/save/goal")
    public ResponseEntity<ApiResponse<String>> saveGoal(@RequestBody SaveGoalDTO saveGoalDTO, HttpSession session) {
        try {
            UserDTO user = (UserDTO) session.getAttribute("user");
            saveGoalDTO.setUserId(user.getUserId());
            boolean flag = searchService.saveGoal(saveGoalDTO);
            if(flag){
                return ResponseEntity.ok(ApiResponse.ok("학습목표 저장 성공"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.fail("학습목표 저장 실패"));
            }
        } catch(Exception e){
            log.error("학습목표 저장 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("서버 내부 오류가 발생했습니다"));
        }
    }
    @GetMapping("/getGoalId")
    public ResponseEntity<ApiResponse<String>> getGoalId(HttpSession session){
        try{
            String id = searchService.lastId(session);
            return ResponseEntity.ok(ApiResponse.ok(id));
        } catch (Exception e){
            log.error("id조회 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("서버 내부 오류가 발생했습니다"));
        }
    }

    @PostMapping("/save/activity")
    public ResponseEntity<ApiResponse<String>> saveActivity(@RequestBody SaveActivityDTO saveActivityDTO) {
        try {
            boolean flag = searchService.saveActivity(saveActivityDTO);
            if(flag){
                return ResponseEntity.ok(ApiResponse.ok("활동 저장 성공"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.fail("활동 저장 실패"));
            }
        } catch(Exception e){
            log.error("활동 저장 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("서버 내부 오류가 발생했습니다"));
        }
    }
    @PostMapping("/save/content")
    public ResponseEntity<ApiResponse<String>> saveContent(@RequestBody SaveContentDTO saveContentDTO) {
        try {
            boolean flag = searchService.saveContent(saveContentDTO);
            if(flag){
                return ResponseEntity.ok(ApiResponse.ok("콘텐츠 저장 성공"));
            } else {
                return ResponseEntity.badRequest().body(ApiResponse.fail("콘텐츠 저장 실패"));
            }
        } catch(Exception e){
            log.error("콘텐츠 저장 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("서버 내부 오류가 발생했습니다"));
        }
    }






}