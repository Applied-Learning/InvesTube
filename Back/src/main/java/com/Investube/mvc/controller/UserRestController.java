package com.Investube.mvc.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.Investube.mvc.model.dto.User;
import com.Investube.mvc.model.service.UserService;

@RestController
@RequestMapping("/users")
public class UserRestController {

    private final UserService userService;

    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    // 업로더 정보 조회 (공개 프로필)
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable("userId") int userId) {
        User result = userService.getUserByUserId(userId);

        if (result == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<User> getMyInfo(HttpServletRequest request) {

        int userId = (int) request.getAttribute("userId");  // JWT에서 가져옴

        User result = userService.getMyInfo(userId);

        if (result == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 내 정보 수정 (닉네임, 프로필 이미지)
    @PutMapping("/me")
    public ResponseEntity<Integer> updateMyInfo(HttpServletRequest request,
                                                @RequestBody User user) {

        int userId = (int) request.getAttribute("userId");
        user.setUserId(userId);

        int result = userService.updateMyInfo(user);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 비밀번호 변경
    @PutMapping("/me/password")
    public ResponseEntity<Integer> updatePassword(HttpServletRequest request,
                                                  @RequestParam String password) {

        int userId = (int) request.getAttribute("userId");

        int result = userService.updatePassword(userId, password);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    // 회원 탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<Integer> deleteUser(HttpServletRequest request) {

        int userId = (int) request.getAttribute("userId");

        int result = userService.deleteUser(userId);

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
