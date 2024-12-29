package kdt.boad.user.dto;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class UpdateUserInfoRes {
    private String password;
    private String nickname;
}
