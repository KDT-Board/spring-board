package kdt.boad.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import kdt.boad.user.domain.User;
import kdt.boad.user.dto.*;
import kdt.boad.user.repository.UserRepository;
import kdt.boad.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {
    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    UserService userService;
    @MockitoBean
    UserRepository userRepository;
    @MockitoBean
    PasswordEncoder passwordEncoder;


    @BeforeEach
    void setup() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 - 유효성 검사 : id size")
    void userJoin_invalid_id_size() throws Exception {
        // Given
        UserJoinReq invalidReq = new UserJoinReq("no", "testUser1!", "testUser1!", "testUser1");

        // When & Then
        mockMvc.perform(post("/user/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 - 유효성 검사 : pw null")
    void userJoin_invalid_pw_null() throws Exception {
        // Given
        UserJoinReq invalidReq = new UserJoinReq("testUser1", "", "testUser1!", "testUser1");

        // When & Then
        mockMvc.perform(post("/user/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 - 유효성 검사 : re-pw pattern")
    void userJoin_invalid_re_pw_pattern() throws Exception {
        // Given
        UserJoinReq invalidReq = new UserJoinReq("testUser1", "testUser1!", "testUser1", "testUser1");

        // When & Then
        mockMvc.perform(post("/user/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 - 유효성 검사 : nickname pattern")
    void userJoin_invalid_nick_pattern() throws Exception {
        // Given
        UserJoinReq invalidReq = new UserJoinReq("testUser1", "testUser1!", "testUser1!", "!@#$");

        // When & Then
        mockMvc.perform(post("/user/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidReq)))
                .andExpect(status().isBadRequest())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 - 비밀번호, 비밀번호 확인 불일치")
    void userJoin_pw_re_pw_match() throws Exception {
        // Given
        UserJoinReq invalidReq = new UserJoinReq("testUser1", "testUser1!", "testUser1@", "testUser1");

        // When & Then
        mockMvc.perform(post("/user/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(invalidReq)))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 - 중복 아이디")
    void userJoin_overlap_id() throws Exception {
        // Given
        UserJoinReq overlapReq = new UserJoinReq("testUser1", "testUser1!", "testUser1!", "testUser1");
        when(userRepository.existsById("testUser1")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/user/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(overlapReq)))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 - 중복 닉네임")
    void userJoin_overlap_nick() throws Exception {
        // Given
        UserJoinReq overlapReq = new UserJoinReq("testUser1", "testUser1!", "testUser1!", "testUser1");
        when(userRepository.existsById("testUser1")).thenReturn(false);
        when(userRepository.existsByNickname("testUser1")).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/user/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(overlapReq)))
                .andExpect(status().isConflict())
                .andDo(print());
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void userJoin_success() throws Exception {
        // Given
        UserJoinReq validReq = new UserJoinReq("testUser1", "testUser1!", "testUser1!", "testUser1");

        User mockUser = User.builder()
                .id("testUser1")
                .password("encodedPw")
                .nickname("testUser1")
                .build();
        UserJoinRes mockRes = new UserJoinRes(mockUser);

        when(userRepository.existsById("testUser1")).thenReturn(false);
        when(userRepository.existsByNickname("testUser1")).thenReturn(false);
        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPw");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(userService.createUser(validReq)).thenReturn(mockRes);

        // When & Then
        mockMvc.perform(post("/user/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(validReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("testUser1"))
                .andExpect(jsonPath("$.password").value("encodedPw"))
                .andExpect(jsonPath("$.id").value("testUser1"))
                .andDo(print());
    }

    @Test
    @DisplayName("로그인 - 존재하지 않는 아이디")
    void userLogin_non_exist_id() throws Exception {
        // Given
        UserLoginReq userLoginReq = new UserLoginReq("testUser1", "testUser1!");
        when(userRepository.existsById(userLoginReq.getId())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userLoginReq)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("로그인 - 비밀번호 불일치")
    void userLogin_pw_match() throws Exception {
        // Given
        UserLoginReq userLoginReq = new UserLoginReq("testUser1", "testUser1!");
        User mockUser = User.builder()
                .id("testUser1")
                .password("encodedPw")
                .nickname("testUser1")
                .build();

        when(userRepository.existsById(userLoginReq.getId())).thenReturn(true);
        when(userRepository.findById(userLoginReq.getId())).thenReturn(mockUser);
        when(passwordEncoder.matches(userLoginReq.getPassword(), mockUser.getPassword())).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(userLoginReq)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("로그인 - 성공")
    void userLogin_success() throws Exception {
        // Given
        UserLoginReq userLoginReq = new UserLoginReq("testUser1", "testUser1!");
        User mockUser = User.builder()
                .id("testUser1")
                .password("encodedPw")
                .nickname("testUser1")
                .build();
        UserLoginRes mockRes = UserLoginRes.builder()
                .loginUser(mockUser)
                .build();

        when(userRepository.existsById(userLoginReq.getId())).thenReturn(true);
        when(userRepository.findById(userLoginReq.getId())).thenReturn(mockUser);
        when(passwordEncoder.matches(userLoginReq.getPassword(), mockUser.getPassword())).thenReturn(true);
        when(userService.loginUser(mockUser)).thenReturn(mockRes);

        // When & Then
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(userLoginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("testUser1"))
                .andExpect(jsonPath("$.nickname").value("testUser1"));
    }

    @Test
    @DisplayName("로그아웃 - 토큰 블랙리스트 실패")
    void userLogout_fail() throws Exception {
        // Given
        Authentication mockAuth = Mockito.mock(Authentication.class);
        User mockUser = User.builder()
                .id("testUser")
                .password("testUser1!")
                .nickname("testUser")
                .build();

        when(mockAuth.getName()).thenReturn(mockUser.getId());
        when(userRepository.findById(mockUser.getId())).thenReturn(mockUser);
        when(userService.validUser(mockAuth)).thenReturn(mockUser);
        when(userService.logoutUser(mockUser, null)).thenReturn(false);

        // When & Then
        mockMvc.perform(post("/user/logout").principal(mockAuth))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("로그아웃에 실패했습니다."));
    }

    @Test
    @DisplayName("로그아웃 - 성공")
    void userLogout_success() throws Exception {
        Authentication mockAuth = Mockito.mock(Authentication.class);
        User mockUser = User.builder()
                .id("testUser")
                .password("testUser1!")
                .nickname("testUser")
                .build();

        when(mockAuth.getName()).thenReturn(mockUser.getId());
        when(userRepository.findById(mockUser.getId())).thenReturn(mockUser);
        when(userService.validUser(mockAuth)).thenReturn(mockUser);
        when(userService.logoutUser(eq(mockUser), any(HttpServletRequest.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/user/logout").principal(mockAuth))
                .andExpect(status().isOk())
                .andExpect(content().string("로그아웃에 성공했습니다."));
    }


    @Test
    @DisplayName("사용자 정보 - 성공")
    void userInfo() throws Exception {
        // When
        Authentication mockAuth = Mockito.mock(Authentication.class);
        User mockUser = User.builder()
                .id("testUser1")
                .password("testUser1!")
                .nickname("testUser1")
                .build();
        UserInfoDTO userInfoDTO = UserInfoDTO.builder()
                .user(mockUser)
                .build();
        String mockGrade = "BRONZE";

        when(mockAuth.getName()).thenReturn(mockUser.getId());
        when(userRepository.findById(mockAuth.getName())).thenReturn(mockUser);
        when(userService.validUser(mockAuth)).thenReturn(mockUser);
        when(userService.getUserInfo(mockUser)).thenReturn(userInfoDTO);

        // When & Then
        mockMvc.perform(get("/user/info").principal(mockAuth))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(mockUser.getId()))
                .andExpect(jsonPath("$.nickname").value(mockUser.getNickname()))
                .andExpect(jsonPath("$.grade").value(mockGrade));
    }

    @Test
    @DisplayName("사용자 정보 업데이트 - overlap nickname")
    void updateUserInfo_overlap_nickname() throws Exception {
        // Given
        Authentication mockAuth = Mockito.mock(Authentication.class);

        List<User> mockUserList = new ArrayList<>();
        User overlapUser = User.builder()
                .id("testUser1")
                .nickname("testUser1")
                .password("testUser1!")
                .build();
        User updateUser = User.builder()
                .id("testUser2")
                .nickname("testUser2")
                .password("testUser2!")
                .build();
        mockUserList.add(overlapUser);
        mockUserList.add(updateUser);
        userRepository.saveAll(mockUserList);

        UpdateUserInfoReq mockUpdateReq = new UpdateUserInfoReq("updatePw1!", "updatePw1!", "testUser1");

        when(userRepository.findByNickname(mockUpdateReq.getNickname())).thenReturn(overlapUser);
        when(userRepository.findById(updateUser.getId())).thenReturn(updateUser);
        when(userService.validUser(mockAuth)).thenReturn(updateUser);

        // When  & Then
        mockMvc.perform(patch("/user/info").principal(mockAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(mockUpdateReq)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("사용자 정보 업데이트 - 성공")
    void updateUserInfo() throws Exception {
        // Given
        Authentication mockAuth = Mockito.mock(Authentication.class);

        User updateUser = User.builder()
                .id("testUser2")
                .nickname("testUser2")
                .password("testUser2!")
                .build();
        userRepository.save(updateUser);

        UpdateUserInfoReq mockUpdateReq = new UpdateUserInfoReq("updatePw1!", "updatePw1!", "testUser2");

        when(mockAuth.getName()).thenReturn("testUser2");
        when(userRepository.findByNickname(mockUpdateReq.getNickname())).thenReturn(updateUser);
        when(userRepository.findById(mockAuth.getName())).thenReturn(updateUser);
        when(userService.updateUserInfo(userRepository.findById(mockAuth.getName()), mockUpdateReq)).thenReturn(new UpdateUserInfoRes("updatePw1!", "testUser2"));
        when(userService.validUser(mockAuth)).thenReturn(updateUser);

        // When & Then
        mockMvc.perform(patch("/user/info").principal(mockAuth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(mockUpdateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").value("updatePw1!"))
                .andExpect(jsonPath("$.nickname").value("testUser2"));
    }

    @Test
    @DisplayName("사용자 삭제 - 성공")
    void deleteUser() throws Exception {
        // Given
        Authentication mockAuth = Mockito.mock(Authentication.class);

        User mockUser = User.builder()
                .id("testUser1")
                .password("testUser1!")
                .nickname("testUser1")
                .build();
        userRepository.save(mockUser);

        when(mockAuth.getName()).thenReturn("testUser1");
        when(userRepository.findById(mockAuth.getName())).thenReturn(mockUser);
        when(userService.validUser(mockAuth)).thenReturn(mockUser);
        when(userService.deleteUser(mockUser)).thenReturn(mockUser.getId() + "님의 회원 정보를 삭제했습니다.\n");

        // When & Then
        mockMvc.perform(delete("/user").principal(mockAuth)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(mockUser.getId() + "님의 회원 정보를 삭제했습니다.\n"));
    }
}
