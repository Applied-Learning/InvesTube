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

@RestController
@RequestMapping("/auth")
public class AuthRestController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthRestController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {

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

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        int result = userService.register(user);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }
}
