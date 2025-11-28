package com.Investube.mvc.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Investube.mvc.model.dto.User;
import com.Investube.mvc.model.service.UserService;
import com.Investube.mvc.util.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "인증 API", description = "로그인, 회원가입 관련 API")
@RestController
@RequestMapping("/auth")
public class AuthRestController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthRestController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인하여 JWT 토큰을 발급받습니다")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "로그인 성공"),
        @ApiResponse(responseCode = "401", description = "로그인 실패 - 아이디 또는 비밀번호 오류")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Parameter(description = "로그인 정보 (아이디, 비밀번호)") @RequestBody User user) {

        User loginUser = userService.login(user);

        if (loginUser == null) {
            return new ResponseEntity<>("fail", HttpStatus.UNAUTHORIZED);
        }

        // JWT 생성
        String token = jwtUtil.createToken(loginUser.getUserId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", loginUser.getUserId());
        result.put("nickname", loginUser.getNickname());

        return ResponseEntity.ok(result);
    }

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "회원가입 성공"),
        @ApiResponse(responseCode = "400", description = "회원가입 실패 - 중복된 아이디 등")
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Parameter(description = "회원가입 정보") @RequestBody User user) {
        int result = userService.register(user);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
