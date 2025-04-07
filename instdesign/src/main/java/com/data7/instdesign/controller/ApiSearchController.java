package com.data7.instdesign.controller;

import com.data7.instdesign.dto.ApiResponse;
import com.data7.instdesign.dto.auth.*;
import com.data7.instdesign.dto.search.GoalRequestDTO;
import com.data7.instdesign.service.AuthService;
import com.data7.instdesign.service.SearchService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    //OpenAI API에서 받은 정보를 어떻게 매핑할 것인지, 이것에 대한 DTO로 Return해줘야 함. 아직 안만들어서 모름
    public ResponseEntity<ApiResponse<List<?>>> receiveGoalAndResponseAPI(@RequestBody GoalRequestDTO request){
        try{
            //여기서 DTO로 받은 값을 Python과 소통해야 함.
            //Python API와 소통 후, 결과값을 다시 View로 쏘아주는 형식.


            return null;
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.fail("OpenAI API 와의 커넥션 이슈. 다시 시도해주세요.(아니면 여기에서 오류별로 message 정의해두는것도 좋을 듯)"));
        }
    }



}