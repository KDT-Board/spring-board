package kdt.boad.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
@AllArgsConstructor
public class UpdateUserInfoReq {
    @NotBlank(message = "PW - 비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]*$", message = "PW - 패턴에 맞게 입력해주세요.")
    @Size(min = 8, max = 20, message = "PW - 최소, 최대 길이에 맞게 입력해주세요.")
    private String password;
    @NotBlank(message = "PW - 비밀번호를 입력해주세요.")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&#])[A-Za-z\\d@$!%*?&#]*$", message = "PW - 패턴에 맞게 입력해주세요.")
    @Size(min = 8, max = 20, message = "PW - 최소, 최대 길이에 맞게 입력해주세요.")
    private String rePassword;
    @NotBlank(message = "NICK - 닉네임을 입력해주세요.")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9]*$", message = "NICK - 패턴에 맞게 입력해주세요.")
    @Size(min = 3, max = 10, message = "NICK - 최소, 최대 길이에 맞게 입력해주세요.")
    private String nickname;
}
