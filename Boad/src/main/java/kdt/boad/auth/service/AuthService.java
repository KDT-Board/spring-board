package kdt.boad.auth.service;

import kdt.boad.auth.dto.KakaoAPI;
import kdt.boad.auth.dto.KakaoProfile;
import kdt.boad.auth.dto.KakaoToken;
import kdt.boad.jwt.JwtService;
import kdt.boad.user.domain.User;
import kdt.boad.user.dto.UserLoginRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final KakaoAPI kakaoAPI;
    private final JwtService jwtService;

    @Transactional
    public KakaoToken getKakaoRes(String code)
    {
        String reqUrl = kakaoAPI.getKAUTH_TOKEN_URL_HOST() + "/oauth/token";
        RestTemplate restTemplate = new RestTemplate();

        // HttpHeader Object
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

        // HttpBody Object
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoAPI.getClientId());
        params.add("redirect_uri", kakaoAPI.getRedirectUrl());
        params.add("code", code);

        // http 바디 params와 http 헤더 headers를 가진 엔티티
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        // reqUrl로 Http 요청, POST 방식
        ResponseEntity<KakaoToken> response = restTemplate.exchange(
                reqUrl,
                HttpMethod.POST,
                kakaoTokenRequest,
                KakaoToken.class);

        KakaoToken kakaoToken = response.getBody();
        log.info("RESPONSE : " + kakaoToken);

        return kakaoToken;
    }

    public KakaoProfile getUserInfo(String accessToken) {
        String reqUrl = "https://kapi.kakao.com/v2/user/me";
        RestTemplate restTemplate = new RestTemplate();

        //HttpHeader 오브젝트
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        //http 헤더(headers)를 가진 엔티티
        HttpEntity<Void> request = new HttpEntity<>(headers);

        //reqUrl로 Http 요청 , POST 방식
        ResponseEntity<String> response = restTemplate.exchange(reqUrl, HttpMethod.POST, request, String.class);

        return new KakaoProfile(response.getBody());
    }

    public UserLoginRes kakaoLogin(User kakaoUser) {
        return UserLoginRes.from(kakaoUser, jwtService.createToken(kakaoUser));
    }
}
