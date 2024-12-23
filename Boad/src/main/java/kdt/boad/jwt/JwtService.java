package kdt.boad.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import kdt.boad.user.domain.User;
import kdt.boad.user.dto.TokenDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Service
@Slf4j
public class JwtService {
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7일
    private static final String PREFIX_REFRESH = "REFRESH: ";

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
        String refreshToken = createRefreshToken();

        // RefreshToken 만료 기한 설정
        Date refreshExpired = parseClaims(refreshToken).getExpiration();
        redisTemplate.opsForValue()
                .set(PREFIX_REFRESH + accessToken, refreshToken, Duration.ofSeconds(refreshExpired.getTime() - new Date().getTime()));
        log.info("AccessToken : {}", accessToken);
        log.info("RefreshToken : {}", redisTemplate.opsForValue().get(PREFIX_REFRESH + accessToken));

        return new TokenDTO(accessToken);
    }

    // 액세스 토큰 생성
    private String createAccessToken(User loginUser) {
        long now = new Date().getTime();

        return Jwts.builder()
                .setSubject(loginUser.getId())
                .claim("grade", loginUser.getGrade())
                .setExpiration(new Date(now + ACCESS_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 리프레쉬 토큰 생성
    private String createRefreshToken() {
        long now = new Date().getTime();

        return Jwts.builder()
                .setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
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
}
