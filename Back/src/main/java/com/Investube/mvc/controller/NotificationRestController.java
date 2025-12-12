package com.Investube.mvc.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Investube.mvc.model.dto.Notification;
import com.Investube.mvc.model.service.NotificationService;
import com.Investube.mvc.util.JwtUtil;

@RestController
@RequestMapping("/notifications")
public class NotificationRestController {

    private final NotificationService notificationService;
    private final JwtUtil jwtUtil;

    public NotificationRestController(NotificationService notificationService, JwtUtil jwtUtil) {
        this.notificationService = notificationService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getNotifications(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        Integer userId = jwtUtil.getUserIdByToken(token);
        if (userId == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        List<Notification> list = notificationService.getNotificationsForUser(userId);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Integer> getUnreadCount(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        Integer userId = jwtUtil.getUserIdByToken(token);
        if (userId == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        int count = notificationService.getUnreadCount(userId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable int id, @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        Integer userId = jwtUtil.getUserIdByToken(token);
        if (userId == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        if (notificationService.markAsRead(id, userId)) {
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PatchMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        String token = authorizationHeader.replace("Bearer ", "");
        Integer userId = jwtUtil.getUserIdByToken(token);
        if (userId == null) return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        notificationService.markAllAsRead(userId);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
