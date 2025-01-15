package kdt.boad.auth.controller;

import jakarta.servlet.http.HttpServletResponse;
import kdt.boad.auth.dto.KakaoAPI;
import kdt.boad.auth.dto.KakaoProfile;
import kdt.boad.auth.dto.KakaoToken;
import kdt.boad.auth.service.AuthService;
import kdt.boad.user.domain.User;
import kdt.boad.user.dto.UserLoginRes;
import kdt.boad.user.repository.UserRepository;
import kdt.boad.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/view/auth")
@RequiredArgsConstructor
public class AuthViewController {
    private final KakaoAPI kakaoAPI;
    private final AuthService authService;
    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/kakao")
    public String kakaoLoginView(Model model) {

        model.addAttribute("clientId", kakaoAPI.getClientId());
        model.addAttribute("redirectUrl", kakaoAPI.getRedirectUrl());

        return "kakaoLoginPage";
    }

    @GetMapping("/kakao-login")
    public String userKakaoLogin(@RequestParam("code") String code, HttpServletResponse response, Model model) {
        // 카카오 인가를 통한 토큰
        KakaoToken kakaoToken = authService.getKakaoRes(code);
        // 카카오 유저 정보 받아오기
        KakaoProfile userInfo = authService.getUserInfo(kakaoToken.getAccessToken());

        // 기존 회원 정보 확인 및 저장
        User user = userRepository.findById(userInfo.getId());
        if (user == null) {
            user = userService.createKakaoUser(userInfo);
        }

        // 회원 정보 바탕으로 로그인 및 토큰 발급
        UserLoginRes userLoginRes = authService.kakaoLogin(user);

        // Header에 Cookie 추가
        ResponseCookie cookie = ResponseCookie.from("accessToken", userLoginRes.getToken().getAccessToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(30 * 60)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        model.addAttribute("accessToken", "Bearer " + userLoginRes.getToken().getAccessToken());

        // 회원 정보 바탕으로 로그인 및 토큰 발급
        return "kakaoLoginPage"; // 로그인 페이지로 이동
    }


}
