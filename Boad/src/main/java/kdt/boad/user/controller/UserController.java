package kdt.boad.user.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import kdt.boad.user.domain.User;
import kdt.boad.user.dto.*;
import kdt.boad.user.repository.UserRepository;
import kdt.boad.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<UserLoginRes> userLogin(@RequestBody UserLoginReq userLoginReq) {
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
        return ResponseEntity.status(HttpStatus.OK).body(userLoginRes);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> userLogout(Authentication authentication, HttpServletRequest request) {
        // AccessToken 정보를 가져오기 위해 인자로 HttpServletRequest를 받아옴
        if (authentication == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 사용자입니다.");

        if (!userService.logoutUser(userRepository.findById(authentication.getName()), request))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("로그아웃에 실패했습니다.");
        return ResponseEntity.status(HttpStatus.OK).body("로그아웃에 성공했습니다.");
    }

    @GetMapping("/info")
    public ResponseEntity<UserInfoDTO> userInfo(Authentication authentication) {
        if (authentication == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);

        return ResponseEntity.status(HttpStatus.OK).body(userService.getUserInfo(userRepository.findById(authentication.getName())));
    }
}
