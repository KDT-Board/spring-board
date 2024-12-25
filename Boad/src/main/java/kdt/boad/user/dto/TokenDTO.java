package kdt.boad.user.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class TokenDTO {
    private final String grantType;
    private final String accessToken;

    @Builder
    public TokenDTO(String accessToken) {
        this.grantType = "Bearer";
        this.accessToken = accessToken;
    }
}
