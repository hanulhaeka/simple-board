package com.example.simple_board.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupDto {

    @NotBlank(message = "아이디를 입력해야합니다.")
    private String userId;
    @NotBlank(message = "비밀번호를 입력해야합니다.")
    private String password;
}
