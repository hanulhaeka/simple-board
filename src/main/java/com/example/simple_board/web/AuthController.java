package com.example.simple_board.web;

import com.example.simple_board.exception.DuplicateUserIdException;
import com.example.simple_board.model.dto.SignupDto;
import com.example.simple_board.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // 회원 가입 폼 전송
    @GetMapping(value = "/auth/signup")
    public String signup(Model model) {
        model.addAttribute("signupDto", new SignupDto());
        return "auth/auth-signup";
    }

    // 회원 가입 요청 처리
    @PostMapping("/auth/signup")
    public String signup(@Validated @ModelAttribute("signupDto") SignupDto signupDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "auth/auth-signup";
        }

        try {
            authService.signup(signupDto);
        } catch (DuplicateUserIdException e) {
            bindingResult.rejectValue("userId", "duplicateUserId", "중복된 아이디가 있습니다.");
            return "auth/auth-signup";
        }

        return "redirect:/";

    }

    //로그인 폼 전송
    @GetMapping("/auth/signin")
    public String loginPage() {
        return "auth/auth-signin";
    }
}
