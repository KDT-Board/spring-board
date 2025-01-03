package kdt.boad.user.service;

import jakarta.servlet.http.HttpServletRequest;
import kdt.boad.jwt.JwtService;
import kdt.boad.user.domain.User;
import kdt.boad.user.dto.*;
import kdt.boad.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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

        return new UserLoginRes(loginUser, token);
    }

    public boolean logoutUser(User logoutUser, HttpServletRequest request) {
        return jwtService.blacklistToken(logoutUser, request);
    }

    public UserInfoDTO getUserInfo(User user) {
        return UserInfoDTO.builder()
                .user(user)
                .build();
    }
}
