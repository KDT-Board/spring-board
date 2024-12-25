package kdt.boad.user.controller;

import kdt.boad.user.domain.Grade;
import kdt.boad.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view/user")
@RequiredArgsConstructor
public class UserViewController {
    private final UserRepository userRepository;

    @GetMapping("/join")
    public String joinPage() {
        return "joinPage"; // joinPage.html 렌더링
    }

    @GetMapping("/login")
    public String loginPage() {
        return "loginPage"; // loginPage.html 렌더링
    }

    @GetMapping("/main")
    public String mainPage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isLoggedIn = authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getName());

        // 로그 출력
        System.out.println("Authentication Name: " + authentication.getName());
        System.out.println("Is Logged In: " + isLoggedIn);

        // 로그인 여부
        model.addAttribute("loggedIn", isLoggedIn);
        model.addAttribute("notLoggedIn", !isLoggedIn);

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