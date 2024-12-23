package kdt.boad.user.dto;

import kdt.boad.user.domain.Grade;
import kdt.boad.user.domain.User;
import lombok.Builder;
import lombok.Getter;

@Getter
public class UserLoginRes {
    private TokenDTO token;
    private String id;
    private String nickname;
    private Grade grade;

    @Builder
    public UserLoginRes(User loginUser, TokenDTO tokenDTO) {
        this.token = tokenDTO;
        this.id = loginUser.getId();
        this.nickname = loginUser.getNickname();
        this.grade = loginUser.getGrade();
    }
}
