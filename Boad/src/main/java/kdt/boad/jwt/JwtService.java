package kdt.boad.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import kdt.boad.user.domain.User;
import kdt.boad.user.dto.TokenDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.time.Duration;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class JwtService {
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7일
    private static final String PREFIX_REFRESH = "REFRESH: ";
    private static final String PREFIX_LOGOUT_ACCESS = "LOGOUT_ACCESS: ";
    private static final String PREFIX_LOGOUT_REFRESH = "LOGOUT_REFRESH: ";

    private final Key key;
    private final StringRedisTemplate redisTemplate;

    public JwtService(@Value("${jwt.secret-key}") String jwtKey, StringRedisTemplate redisTemplate) {
        // 컴퓨터가 이해할 수 있도록 바이트화
        byte[] keyByte = Decoders.BASE64.decode(jwtKey);
        // HMAC 알고리즘 위한 암호화 키
        this.key = Keys.hmacShaKeyFor(keyByte);
        this.redisTemplate = redisTemplate;
    }

    // 토큰 정보 생성 및 Redis 저장
    public TokenDTO createToken(User loginUser) {
        String accessToken = createAccessToken(loginUser);
        String refreshToken = createRefreshToken(accessToken);

        log.info("Create AccessToken : {}", accessToken);
        log.info("Create RefreshToken : {}", refreshToken);

        return new TokenDTO(accessToken);
    }

    // 액세스 토큰 생성
    protected String createAccessToken(User loginUser) {
        long now = new Date().getTime();

        return Jwts.builder()
                .setSubject(loginUser.getId())
                .claim("grade", loginUser.getGrade())
                .setExpiration(new Date(now + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 리프레쉬 토큰 생성
    protected String createRefreshToken(String accessToken) {
        long now = new Date().getTime();

        String refreshToken =  Jwts.builder()
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        // RefreshToken 만료 기한 설정
        Date refreshExpired = parseClaims(refreshToken).getExpiration();
        try {
            redisTemplate.opsForValue()
                    .set(PREFIX_REFRESH + accessToken, refreshToken, Duration.ofSeconds(refreshExpired.getTime()));
            return refreshToken;
        } catch (Exception e) {
            log.error("Redis 저장 과정에서 에러가 발생했습니다.");
            return null;
        }
    }

    // 토큰 정보 추출
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token) // parseClaimsJwt와 차이점 : 서명을 검증해 토큰 변조 여부를 확인
                .getBody();

        } catch (ExpiredJwtException e) {
            return e.getClaims();
        } catch (Exception e) {
            throw new RuntimeException("유효하지 않은 토큰 정보입니다.");
        }
    }

    // 토큰 블랙리스트 처리
    public boolean blacklistToken(User logoutUser, HttpServletRequest request) {
        try {
            String accessToken = request.getHeader("Authorization").substring(7); // 요청 헤더가 Authorization인 것을 불러와 토큰 값 Get
            String refreshToken = redisTemplate.opsForValue().get(PREFIX_REFRESH + accessToken);

            Date accessExpired = parseClaims(accessToken).getExpiration();
            Date refreshExpired = parseClaims(refreshToken).getExpiration();
            log.info("Access Expired : {}", accessExpired.toString());
            log.info("Refresh Expired : {}", refreshExpired.toString());

            redisTemplate.opsForValue()
                    .set(PREFIX_LOGOUT_ACCESS + logoutUser.getId(), accessToken, Duration.ofSeconds(accessExpired.getTime() - new Date().getTime()));
            redisTemplate.opsForValue()
                    .set(PREFIX_LOGOUT_REFRESH + accessToken, refreshToken, Duration.ofSeconds(refreshExpired.getTime() - new Date().getTime()));
            return true;
        } catch (Exception e) {
            log.error("Redis 저장 과정에서 에러가 발생했습니다.");
            return false;
        }
    }

    // 요청 헤더에서 AccessToken 추출
    public String resolveAccessToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (!StringUtils.hasText(bearerToken) || !bearerToken.startsWith("Bearer"))
            return null;
        return bearerToken.substring(7);
    }

    // 유효한 AccessToken인지 검증
    public boolean validateAccessToken(String accessToken) {
        try {
            String id = parseClaims(accessToken).getSubject();
            String logoutToken = redisTemplate.opsForValue().get(PREFIX_LOGOUT_ACCESS + id);

            log.info("제공된 토큰 : {}", accessToken);
            log.info("로그아웃 된 토큰 : {}", logoutToken);

            if (accessToken.equals(logoutToken))
                return false;

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken); // 리턴 값과는 무관하지만 올바른 토큰인지 검증해 예외를 던지는 역할 수행

            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.info("변조되었거나 잘못된 형식의 토큰입니다.", e);
        } catch (ExpiredJwtException e) {
            log.info("만료된 토큰입니다.", e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }

    // 유효한 RefreshToken인지 검증
    public boolean validateRefreshToken(String id, String accessToken) {
        String logoutAccessToken = redisTemplate.opsForValue().get(PREFIX_LOGOUT_ACCESS + id);

        if (logoutAccessToken.equals(accessToken))
            return false;

        String refreshToken = redisTemplate.opsForValue().get(PREFIX_REFRESH + accessToken);

        return !refreshToken.isEmpty();
    }

    // 유저 인증 정보 추출
    public Authentication getAuthentication (String accessToken) {
        Claims claims = parseClaims(accessToken); // 등급 정보 추출하기 위해 필요

        if (claims.get("grade") == null)
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");

        // SimpleGrantedAuthority : SpringSecurity에서 권한 표현하는 클래스
        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(claims.get("grade").toString()));
        UserDetails principal = new org.springframework.security.core.userdetails.User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
}
