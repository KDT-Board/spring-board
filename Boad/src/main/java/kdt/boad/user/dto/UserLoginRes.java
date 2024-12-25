package kdt.boad.user.dto;

import kdt.boad.user.domain.Grade;
import kdt.boad.user.domain.User;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class UserLoginRes {
    private final TokenDTO token;
    private final String id;
    private final String nickname;
    private final Grade grade;

    @Builder
    public UserLoginRes(User loginUser, TokenDTO tokenDTO) {
        this.token = tokenDTO;
        this.id = loginUser.getId();
        this.nickname = loginUser.getNickname();
        this.grade = loginUser.getGrade();
    }
}
