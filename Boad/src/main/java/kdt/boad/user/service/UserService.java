package kdt.boad.user.service;

import jakarta.servlet.http.HttpServletRequest;
import kdt.boad.auth.dto.KakaoProfile;
import kdt.boad.jwt.JwtService;
import kdt.boad.user.domain.User;
import kdt.boad.user.dto.*;
import kdt.boad.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public User validUser(Authentication authentication) {
        if (authentication == null)
            return null;

        return userRepository.findById(authentication.getName());
    }

    @Transactional
    public UserJoinRes createUser(UserJoinReq userJoinReq) {
        User createUser = User.builder()
                .id(userJoinReq.getId())
                .password(passwordEncoder.encode(userJoinReq.getPassword()))
                .nickname(userJoinReq.getNickname())
                .build();

        userRepository.save(createUser);

        return new UserJoinRes(createUser);
    }

    public UserLoginRes loginUser(User loginUser) {
        TokenDTO token = jwtService.createToken(loginUser);

        if (token == null)
            return null;

        return UserLoginRes.from(loginUser, token);
    }

    public boolean logoutUser(User logoutUser, HttpServletRequest request) {
        return jwtService.blacklistToken(logoutUser, request);
    }

    public UserInfoDTO getUserInfo(User user) {
        return UserInfoDTO.builder()
                .user(user)
                .build();
    }

    @Transactional
    public UpdateUserInfoRes updateUserInfo(User user, UpdateUserInfoReq userInfoReq) {
        user.setNickname(userInfoReq.getNickname());
        user.setPassword(passwordEncoder.encode(userInfoReq.getPassword()));

        userRepository.save(user);

        return new UpdateUserInfoRes(user.getPassword(), user.getNickname());
    }

    @Transactional
    public String deleteUser(User user) {
        userRepository.delete(user);

        return user.getId() + "님의 회원 정보를 삭제했습니다.\n";
    }

    @Transactional
    public User createKakaoUser(KakaoProfile userInfo) {
        String nickname = userInfo.getNickname();
        String uuid = UUID.randomUUID().toString();

        // 유저 닉네임 중복 방지
        while (userRepository.existsByNickname(nickname + uuid)) {
            uuid = UUID.randomUUID().toString();
        }

        User kakaoUser = User.of(userInfo.getId(), null, userInfo.getNickname() + uuid);
        userRepository.save(kakaoUser);

        return kakaoUser;
    }
}
