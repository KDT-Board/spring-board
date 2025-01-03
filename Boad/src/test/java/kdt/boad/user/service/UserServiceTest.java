package kdt.boad.user.service;

import jakarta.servlet.http.HttpServletRequest;
import kdt.boad.jwt.JwtService;
import kdt.boad.user.domain.User;
import kdt.boad.user.dto.*;
import kdt.boad.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private UserService userService;
    @Mock
    private JwtService jwtService;


    @BeforeEach
    void setup() {
        userRepository.deleteAll(); // 데이터 초기화
    }

    @Test
    @DisplayName("사용자 정보 생성 - UserJoinRes 검증")
    void createUser() {
        // Given
        UserJoinReq userJoinReq = new UserJoinReq("testUser1", "testUser1!", "testUser1!",
                "testUser1");

        // Mock 객체 정의
        User mockUser = User.builder()
                .id("testUser1")
                .password("encodedPw")
                .nickname("testUser1")
                .build();

        // Mock 리턴 값 설정
        when(passwordEncoder.encode("testUser1!")).thenReturn("encodedPw");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        // When
        UserJoinRes testUserJoinRes = userService.createUser(userJoinReq);

        // Then
        // 생성된 userJoinRes와 특정 유저를 기반으로 생성한 UserJoinRes 비교
        assertThat(testUserJoinRes).isEqualTo(UserJoinRes.builder().user(mockUser).build());
        verify(passwordEncoder).encode("testUser1!");
    }

    @Test
    @DisplayName("사용자 로그인 - UserLoginRes 검증")
    void loginUser() {
        // Given
        User mockUser = User.builder()
                .id("testUser1")
                .password("encodedPw")
                .nickname("testUser1")
                .build();

        TokenDTO mockToken = TokenDTO.builder()
                .accessToken("mockAccessToken")
                .build();

        when(jwtService.createToken(any(User.class))).thenReturn(mockToken);

        // When
        UserLoginRes testLoginRes = userService.loginUser(mockUser);

        // Then
        assertThat(testLoginRes).isEqualTo(UserLoginRes.builder()
                .loginUser(mockUser)
                .tokenDTO(mockToken)
                .build());
        verify(jwtService).createToken(mockUser);
    }

    @Test
    @DisplayName("사용자 로그아웃 검증")
    void logoutUser() {
        // Given
        User mockUser = User.builder()
                .id("testUser1")
                .password("testUser1!")
                .nickname("testUser1")
                .build();

        HttpServletRequest mockRequest = Mockito.mock(HttpServletRequest.class);

        when(jwtService.blacklistToken(any(User.class), any(HttpServletRequest.class))).thenReturn(true);

        // When
        boolean testLogoutUser = userService.logoutUser(mockUser, mockRequest);

        // Then
        assertTrue(testLogoutUser);
        verify(jwtService).blacklistToken(mockUser, mockRequest);
    }

    @Test
    @DisplayName("사용자 정보 검증")
    void getUserInfo() {
        // Given
        User mockUser = User.builder()
                .id("testUser1")
                .password("testUser1!")
                .nickname("testUser1")
                .build();

        UserInfoDTO mockUserInfo = UserInfoDTO.builder()
                .user(mockUser)
                .build();

        // When
        UserInfoDTO testUserInfo = userService.getUserInfo(mockUser);

        // Then
        assertThat(testUserInfo).isEqualTo(mockUserInfo);
    }

    @Transactional
    @Test
    @DisplayName("사용자 정보 업데이트 검증")
    void updateUserInfo() {
        // Given
        User mockUser = User.builder()
                .id("testUser1")
                .password("testUser1!")
                .nickname("testUser1")
                .build();
        userRepository.save(mockUser);

        UpdateUserInfoReq mockUserInfoReq = new UpdateUserInfoReq("updateUser1!", "updateUser1!", "updateUser1");
        when(passwordEncoder.encode("updateUser1!")).thenReturn("encodedPw");

        // When
        UpdateUserInfoRes userInfoRes = userService.updateUserInfo(mockUser, mockUserInfoReq);

        // Then
        User updateUser = userRepository.findById(mockUser.getId());
        assertThat(updateUser.getNickname())
                .as("Nickname이 updateUser1이어야 합니다.")
                .isEqualTo("updateUser1");
        assertThat(updateUser.getPassword())
                .as("Password가 encodedPw여야 합니다.")
                .isEqualTo("encodedPw");
        assertThat(userInfoRes)
                .as("Nickname ; updateUser1, PW : encodedPw여야 합니다.")
                .isEqualTo(new UpdateUserInfoRes("encodedPw", "updateUser1"));
    }

    @Transactional
    @Test
    @DisplayName("사용자 삭제 검증")
    void deleteUser() {
        // Given
        User mockUser = User.builder()
                .id("testUser1")
                .password("testUser1!")
                .nickname("testUser1")
                .build();
        userRepository.save(mockUser);

        // When
        String msg = userService.deleteUser(mockUser);

        // Then
        assertThat(msg)
                .as("응답 메시지가 올바르지 않습니다.")
                .isEqualTo(mockUser.getId() + "님의 회원 정보를 삭제했습니다.\n");
        assertThat(userRepository.findById("testUser1"))
                .as("사용자 정보가 삭제되지 않았습니다.")
                .isEqualTo(null);
    }
}