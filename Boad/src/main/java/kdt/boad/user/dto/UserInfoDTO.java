package kdt.boad.user.dto;

import kdt.boad.user.domain.Grade;
import kdt.boad.user.domain.User;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class UserInfoDTO {
    public String id;
    public String nickname;
    public Grade grade;

    @Builder
    public UserInfoDTO(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.grade = user.getGrade();
    }
}
