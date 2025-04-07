package com.data7.instdesign.controller;

import com.data7.instdesign.dto.ApiResponse;

import com.data7.instdesign.dto.auth.*;
import com.data7.instdesign.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/auth/api")
public class ApiAuthController {

    private final AuthService authService;

    @PostMapping("/checkId")
    public ResponseEntity<ApiResponse<String>> checkId(@RequestBody IdRequestDTO idRequestDTO) {
        try {
            log.info("중복체크 요청 들어옴: {}", idRequestDTO.getUserId());
            boolean flag = authService.checkId(idRequestDTO.getUserId());
            if(flag){
                return ResponseEntity.ok(ApiResponse.ok("중복없음"));
            }

            return ResponseEntity.badRequest().body(ApiResponse.fail("중복존재"));

        } catch (Exception e) {
            log.error("아이디 중복 체크 중 오류 발생",e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("서버 내부 오류가 발생했습니다"));
        }
    }

    @PostMapping("/checkEmail")
    public ResponseEntity<ApiResponse<String>> checkEmail(@RequestBody EmailRequestDTO emailRequestDTO){
        try {
            boolean flag = authService.checkEmail(emailRequestDTO.getEmail());
            if(flag){
                return ResponseEntity.ok(ApiResponse.ok("중복없음"));
            }

            return ResponseEntity.badRequest().body(ApiResponse.fail("중복존재"));

        } catch (Exception e) {
            log.error("아이디 중복 체크 중 오류 발생",e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("서버 내부 오류가 발생했습니다"));
        }


    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterDTO registerDTO) {
        try {
            boolean flag = authService.register(registerDTO);
            if (flag) {
                return ResponseEntity.ok(ApiResponse.ok("회원가입 성공"));
            }
            return ResponseEntity.badRequest().body(ApiResponse.fail("회원가입 중 오류 발생"));
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생", e);

            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("서버 내부 오류가 발생했습니다"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginDTO loginDTO, HttpServletResponse response, HttpSession session) {
        try {
            // 1. 로그인 처리 (userDTO 반환)
            UserDTO user = authService.login(loginDTO);

            if (user == null) {
                return ResponseEntity.badRequest().body(ApiResponse.fail("아이디 또는 비밀번호가 일치하지 않습니다"));
            }

            //세션에 로그인 정보 저장. 나중에 필터로 user 확인 후 DB 조회하든가
            session.setAttribute("user", user);

            // 3. rememberMe 쿠키 처리
            if (loginDTO.getRememberMe()) {
                String token = UUID.randomUUID().toString();
                boolean tokenSaved = authService.saveToken(user.getUserId(), token);

                if(tokenSaved){
                    Cookie cookie = new Cookie("rememberMe", token);
                    cookie.setHttpOnly(true);
                    cookie.setSecure(false); // 개발 중이면 false로 --> 왜요?
                    cookie.setMaxAge(24 * 60 * 60); // 1일
                    cookie.setPath("/");
                    response.addCookie(cookie);
                } else {
                    return ResponseEntity.badRequest().body(ApiResponse.fail("로그인 유지 실패. 다시 시도해주세요"));
                }

            }
            return ResponseEntity.ok(ApiResponse.ok("로그인 성공"));

        } catch (Exception e) {
            log.error("로그인 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.fail("서버 내부 오류가 발생했습니다"));
        }
    }

}