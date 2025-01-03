package kdt.boad.user.controller;

import kdt.boad.jwt.JwtService;
import kdt.boad.user.domain.Grade;
import kdt.boad.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/user")
@RequiredArgsConstructor
public class UserViewController {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @GetMapping("/join")
    public String joinPage() {
        return "joinPage"; // joinPage.html 렌더링
    }

    @GetMapping("/login")
    public String loginPage() {
        return "loginPage"; // loginPage.html 렌더링
    }

    @GetMapping("/logout")
    public String logoutPage() {
        return "logoutPage";
    }

    @GetMapping("/main")
    public String mainPage(@CookieValue(value = "accessToken", required = false) String accessToken, Model model) {
        model.addAttribute("loggedIn", false);
        model.addAttribute("notLoggedIn", true);

        if (accessToken != null && jwtService.validateAccessToken(accessToken)) {
            // 로그인 여부
            model.addAttribute("loggedIn", true);
            model.addAttribute("notLoggedIn", false);
        }

        // 사용자 데이터
        model.addAttribute("userCount", userRepository.count()); // 전체 사용자 수
        model.addAttribute("adminCount", userRepository.countByGrade(Grade.ADMIN)); // 관리자 수
        model.addAttribute("bronzeCount", userRepository.countByGrade(Grade.BRONZE)); // Bronze 등급 사용자 수
        model.addAttribute("silverCount", userRepository.countByGrade(Grade.SILVER)); // Silver 등급 사용자 수
        model.addAttribute("goldCount", userRepository.countByGrade(Grade.GOLD)); // Gold 등급 사용자 수
        model.addAttribute("blacklistCount", userRepository.countByGrade(Grade.BLACKLIST)); // Blacklist 사용자 수

        return "mainPage"; // mainPage.html 렌더링
    }
}