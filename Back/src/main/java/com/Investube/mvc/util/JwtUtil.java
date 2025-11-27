package com.Investube.mvc.util;

import java.util.Date;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

/**
 * 최신 JJWT 방식으로 구현한 JWT Utility
 * - parser() 대신 parserBuilder() 사용
 * - Keys.hmacShaKeyFor() 사용해 SecretKey 객체 생성
 */
@Component
public class JwtUtil {

    // 🔐 더 안전한 방식: 32바이트 이상 길이의 secret key 필요
    private final String SECRET_KEY = "INVESTUBE_SECRET_KEY_12345678901234567890";

    // SecretKey 객체 생성 (보안 권장 방식)
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    // JWT 유효 시간: 6시간
    private final long EXP = 1000L * 60 * 60 * 6;

    /**
     * JWT 토큰 생성
     */
    public String createToken(int userId) {

        // Claims: JWT 안에 담을 데이터(subject 외 추가도 가능)
        Claims claims = Jwts.claims();
        claims.setSubject(String.valueOf(userId)); // userId 저장

        return Jwts.builder()
                .setClaims(claims)                 // payload에 데이터 넣기
                .setIssuedAt(new Date())           // 발급시간
                .setExpiration(new Date(System.currentTimeMillis() + EXP)) // 만료시간
                .signWith(key, SignatureAlgorithm.HS256) // 최신 방식: key 객체로 서명
                .compact();
    }

    /**
     * 토큰에서 userId(subject) 추출
     */
    public Integer getUserIdByToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Integer.parseInt(claims.getSubject());  // JWT에서 userId 추출
        } catch (JwtException | IllegalArgumentException e) {
            return null; // 유효하지 않거나 잘못된 토큰일 경우 null 반환
        }
    }


    /**
     * JWT 유효성 검증
     */
    public boolean validate(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);   // 서명/만료 등 전체 검증

            return true; // 정상 → 유효한 토큰
        } catch (Exception e) {
            return false; // 문제 발생 → 잘못된 토큰
        }
    }
}
