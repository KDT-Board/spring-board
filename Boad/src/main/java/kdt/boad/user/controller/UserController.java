package kdt.boad.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kdt.boad.user.domain.User;
import kdt.boad.user.dto.*;
import kdt.boad.user.repository.UserRepository;
import kdt.boad.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/join")
    public ResponseEntity<UserJoinRes> userJoin(@RequestBody @Valid UserJoinReq userJoinReq, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            log.info("유효성 검사 에러 : {}", bindingResult.getAllErrors().getFirst().getDefaultMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        if (!userJoinReq.getPassword().equals(userJoinReq.getRePassword())) {
            log.info("Error : 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        else if (userRepository.existsById(userJoinReq.getId())) {
            log.info("Error : 이미 존재하는 아이디입니다.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
        else if (userRepository.existsByNickname(userJoinReq.getNickname())){
            log.info("Error : 이미 존재하는 닉네임입니다.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(userJoinReq));
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginRes> userLogin(@RequestBody UserLoginReq userLoginReq, HttpServletResponse response) {
        if (!userRepository.existsById(userLoginReq.getId())) {
            log.info("Error : 존재하지 않는 아이디입니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        User loginUser = userRepository.findById(userLoginReq.getId());
        if (!passwordEncoder.matches(userLoginReq.getPassword(), loginUser.getPassword())) {
            log.info("Error : 비밀번호가 일치하지 않습니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        UserLoginRes userLoginRes = userService.loginUser(loginUser);
        if (userLoginRes == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);

        // Header에 Cookie 추가
        ResponseCookie cookie = ResponseCookie.from("accessToken", userLoginRes.getToken().getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(30 * 60)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.status(HttpStatus.OK).body(userLoginRes);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> userLogout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        User deleteUser = userService.validUser(authentication);
        if (deleteUser == null) {
            log.info("Error : 유효하지 않은 사용자입니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        if (!userService.logoutUser(userRepository.findById(authentication.getName()), request))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("로그아웃에 실패했습니다.");

        // 쿠키 삭제
        ResponseCookie deleteCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)  // 즉시 만료
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.status(HttpStatus.OK).body("로그아웃에 성공했습니다.");
    }

    @GetMapping("/info")
    public ResponseEntity<UserInfoDTO> userInfo(Authentication authentication) {
        User deleteUser = userService.validUser(authentication);
        if (deleteUser == null) {
            log.info("Error : 유효하지 않은 사용자입니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserInfo(userRepository.findById(authentication.getName())));
    }

    @PatchMapping("/info")
    public ResponseEntity<UpdateUserInfoRes> updateUserInfo(Authentication authentication, @RequestBody @Valid UpdateUserInfoReq userInfoReq, BindingResult bindingResult) {
        User deleteUser = userService.validUser(authentication);
        if (deleteUser == null) {
            log.info("Error : 유효하지 않은 사용자입니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        if (bindingResult.hasErrors()) {
            log.info("유효성 검사 에러 : {}", bindingResult.getAllErrors().getFirst().getDefaultMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        User updateUser = userRepository.findByNickname(userInfoReq.getNickname());
        if (updateUser != null && !updateUser.equals(userRepository.findById(authentication.getName()))) {
            log.info("Error : 이미 존재하는 닉네임입니다.\n");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        if (!userInfoReq.getPassword().equals(userInfoReq.getRePassword())) {
            log.info("Error : 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(userService.updateUserInfo(userRepository.findById(authentication.getName()), userInfoReq));
    }

    @DeleteMapping("/{number}")
    public ResponseEntity<String> deleteUser(Authentication authentication, HttpServletResponse response) {
        User deleteUser = userService.validUser(authentication);
        if (deleteUser == null) {
            log.info("Error : 유효하지 않은 사용자입니다.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        // 쿠키 삭제
        ResponseCookie deleteCookie = ResponseCookie.from("accessToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)  // 즉시 만료
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());

        return ResponseEntity.status(HttpStatus.OK).body(userService.deleteUser(userRepository.findById(authentication.getName())));
    }
}
