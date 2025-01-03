package kdt.boad;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("")
public class FrontController {
    @GetMapping("")
    public String connectMainPage() {
        return "redirect:/view/user/main";
    }
}
