package kdt.boad.user.dto;

import kdt.boad.user.domain.User;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class UserJoinRes {
    private String id;
    private String password;
    private String nickname;

    @Builder
    public UserJoinRes(User user) {
        this.id = user.getId();
        this.password = user.getPassword();
        this.nickname = user.getNickname();
    }
}
