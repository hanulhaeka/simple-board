package com.example.simple_board.service;

import com.example.simple_board.exception.DuplicateUserIdException;
import com.example.simple_board.model.Role;
import com.example.simple_board.model.User;
import com.example.simple_board.model.dto.SigninDto;
import com.example.simple_board.model.dto.SignupDto;
import com.example.simple_board.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    AuthService authService;

    @Test
    @DisplayName("회원 가입 성공")
    void signupSuccess() {

        //given
        SignupDto signupDto = new SignupDto("userId", "password");
        when(userRepository.existsByUserId(signupDto.getUserId())).thenReturn(false);
        when(passwordEncoder.encode(signupDto.getPassword())).thenReturn("hashedPassword");

        //when
        authService.signup(signupDto);

        // then
        verify(userRepository).existsByUserId(signupDto.getUserId());
        verify(passwordEncoder).encode(signupDto.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("회원 가입 실패 - 중복 아이디")
    void signupFail() {

        //given
        SignupDto signupDto = new SignupDto("userId", "password");
        when(userRepository.existsByUserId(signupDto.getUserId())).thenReturn(true);

        //when & then
        assertThatThrownBy(() -> authService.signup(signupDto)).isInstanceOf(DuplicateUserIdException.class);



    }

    @Test
    @DisplayName("회원 가입 실패 - 중복 아이디 - db 예외")
    void signupFailDbException() {

        //given
        SignupDto signupDto = new SignupDto("userId", "password");
        when(passwordEncoder.encode(signupDto.getPassword())).thenReturn("hashedPassword");
        when(userRepository.existsByUserId(signupDto.getUserId())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenThrow(DataIntegrityViolationException.class);


        //when & then
        assertThatThrownBy(() -> authService.signup(signupDto)).isInstanceOf(DuplicateUserIdException.class);



    }

    @Test
    @DisplayName("로그인 성공")
    void signinSuccess() {

        //given
        SigninDto signinDto = new SigninDto("userId", "password");
        User user = new User("userId", "hashedPassword", Role.USER);
        when(passwordEncoder.matches(signinDto.getPassword(), user.getHashedPassword())).thenReturn(true);
        when(userRepository.findByUserId(signinDto.getUserId())).thenReturn(Optional.of(user));

        //when
        Optional<User> optionalUser = authService.login(signinDto);

        //then
        assertThat(optionalUser).isPresent();
        assertThat(optionalUser.get().getUserId()).isEqualTo("userId");
        assertThat(optionalUser.get().getHashedPassword()).isEqualTo("hashedPassword");

        verify(userRepository).findByUserId("userId");
        verify(passwordEncoder).matches("password", "hashedPassword");

    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 불일치")
    void signinFailWrongPassword() {

        //given
        SigninDto signinDto = new SigninDto("userId", "wrongPassword");
        User user = new User("userId", "hashedPassword", Role.USER);
        when(passwordEncoder.matches(signinDto.getPassword(), user.getHashedPassword())).thenReturn(false);
        when(userRepository.findByUserId(signinDto.getUserId())).thenReturn(Optional.of(user));

        //when
        Optional<User> optionalUser = authService.login(signinDto);

        //then
        assertThat(optionalUser).isEmpty();

        verify(passwordEncoder).matches("wrongPassword", "hashedPassword");
        verify(userRepository).findByUserId("userId");

    }

    @Test
    @DisplayName("로그인 실패 - 해당 유저 없음")
    void signinFailNotExistUser() {

        //given
        SigninDto signinDto = new SigninDto("userId", "password");
        when(userRepository.findByUserId(signinDto.getUserId())).thenReturn(Optional.empty());

        //when
        Optional<User> optionalUser = authService.login(signinDto);

        //then
        assertThat(optionalUser).isEmpty();

        verify(userRepository).findByUserId("userId");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
