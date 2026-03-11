package com.example.simple_board.web;

import com.example.simple_board.auth.SessionUser;
import com.example.simple_board.exception.DuplicateUserIdException;
import com.example.simple_board.model.User;
import com.example.simple_board.model.dto.SigninDto;
import com.example.simple_board.model.dto.SignupDto;
import com.example.simple_board.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Optional;

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

    // 로그인 폼 전송
    @GetMapping(value = "/auth/signin")
    public String signin(Model model){
        model.addAttribute("signinDto", new SigninDto());
        return "auth/auth-signin";
    }

    // 로그인
    @PostMapping(value = "/auth/signin")
    public String signin(@Validated @ModelAttribute("signinDto") SigninDto signinDto, BindingResult bindingResult, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "auth/auth-signin";
        }

        Optional<User> optionalUser = authService.login(signinDto);

        if (optionalUser.isEmpty()) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다.");
            return "auth/auth-signin";
        }

        User loginUser = optionalUser.get();
        HttpSession session = request.getSession();
        request.changeSessionId();
        SessionUser sessionUser = new SessionUser(loginUser.getId(), loginUser.getRole());
        session.setAttribute("loginUser", sessionUser);

        return "redirect:/";
    }


    // 로그아웃
    @PostMapping(value = "/auth/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/";
    }
}
