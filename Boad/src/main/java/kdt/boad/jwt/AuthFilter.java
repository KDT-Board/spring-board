package kdt.boad.jwt;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kdt.boad.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class AuthFilter extends GenericFilterBean {
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Request Header에서 AccessToken 추출
        String accessToken = jwtService.resolveAccessToken((HttpServletRequest) request);
        log.info("Filter AccessToken : {}", accessToken);

        try {
            // AccessToken 유효성 검사
            if (accessToken != null && jwtService.validateAccessToken(accessToken)) {
                // 토큰이 유효할 경우 토큰에서 Authentication 객체를 가지고 와서 SecurityContext에 저장
                Authentication authentication = jwtService.getAuthentication(accessToken);
                // 현재 사용자의 인증 정보
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        catch (ExpiredJwtException e) {
            // RefreshToken 유효성 검사
            if (jwtService.validateRefreshToken(accessToken)) {
                // 액세스 토큰이 만료된 경우 리프레쉬 토큰을 통해 액세스 토큰 재발급을 시도한다.
                String id = e.getClaims().getSubject();

                String newAccessToken = jwtService.createAccessToken(userRepository.findById(id));
                String newRefreshToken = jwtService.createRefreshToken(newAccessToken);

                log.info("New AccessToken : {}", newAccessToken);
                log.info("New RefreshToken : {}", newRefreshToken);

                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setHeader("AccessToken", newAccessToken); // 클라이언트에 새로 발급된 AccessToken 전달

                SecurityContextHolder.getContext().setAuthentication(jwtService.getAuthentication(newAccessToken));
            }
            else {
                ((HttpServletResponse) response).sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT Token");
                return;
            }
        }

        chain.doFilter(request, response); // 다음 필터로 넘어가거나, 요청 처리 진행
    }
}
