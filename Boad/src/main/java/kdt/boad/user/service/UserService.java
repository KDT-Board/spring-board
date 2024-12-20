package kdt.boad.user.service;

import kdt.boad.user.domain.User;
import kdt.boad.user.dto.UserJoinReq;
import kdt.boad.user.dto.UserJoinRes;
import kdt.boad.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserJoinRes createUser(UserJoinReq userJoinReq) {
        User createUser = User.builder()
                .id(userJoinReq.getId())
                .password(passwordEncoder.encode(userJoinReq.getPassword()))
                .nickname(userJoinReq.getNickname())
                .build();

        userRepository.save(createUser);

        return new UserJoinRes(createUser);
    }
}
