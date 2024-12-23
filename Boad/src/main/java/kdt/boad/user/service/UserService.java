package kdt.boad.user.service;

import kdt.boad.jwt.JwtService;
import kdt.boad.user.domain.User;
import kdt.boad.user.dto.*;
import kdt.boad.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

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
        return new UserLoginRes(loginUser, token);
    }
}
