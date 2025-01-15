package kdt.boad.auth.dto;

import com.nimbusds.jose.shaded.gson.JsonElement;
import com.nimbusds.jose.shaded.gson.JsonObject;
import com.nimbusds.jose.shaded.gson.JsonParser;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@ToString
public class KakaoProfile {
    private String id;
    private String nickname;
    private LocalDateTime connectedAt;

    public KakaoProfile(String jsonResponseBody) {
        JsonElement element = JsonParser.parseString(jsonResponseBody);
        this.id = element.getAsJsonObject().get("id").getAsString();

        String connectedAtStr = element.getAsJsonObject().get("connected_at").getAsString();
        this.connectedAt = LocalDateTime.parse(connectedAtStr.substring(0, connectedAtStr.length() - 1),
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

        JsonObject properties = element.getAsJsonObject().getAsJsonObject("properties");
        this.nickname = properties.get("nickname").getAsString();
    }
}