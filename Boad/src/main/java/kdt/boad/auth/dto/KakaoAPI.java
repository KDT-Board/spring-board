package kdt.boad.auth.dto;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class KakaoAPI {
    @Value("${kakao.client_id}")
    private String clientId;

    @Value("${kakao.redirect-url}")
    private String redirectUrl;

    private final String KAUTH_TOKEN_URL_HOST ="https://kauth.kakao.com";
}
