package com.example.simple_board.service;

import com.example.simple_board.exception.DuplicateUserIdException;
import com.example.simple_board.model.Role;
import com.example.simple_board.model.User;
import com.example.simple_board.model.dto.SigninDto;
import com.example.simple_board.model.dto.SignupDto;
import com.example.simple_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원 가입
    @Transactional
    public void signup(SignupDto signupDto) {

        if (userRepository.existsByUserId(signupDto.getUserId())) {
            throw new DuplicateUserIdException(signupDto.getUserId());
        }

        try {
            String encoded = passwordEncoder.encode(signupDto.getPassword());

            User user = new User(signupDto.getUserId(), encoded, Role.USER);

            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateUserIdException(signupDto.getUserId());
        }


    }

    // 로그인
    @Transactional(readOnly = true)
    public Optional<User> login(SigninDto signinDto) {
        return userRepository.findByUserId(signinDto.getUserId())
                .filter(user -> passwordEncoder.matches(signinDto.getPassword(), user.getHashedPassword()));
    }
}
