package com.example.simple_board.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeletedForm {

    private Long id;

    @NotBlank(message = "비밀번호를 입력해야합니다.")
    private String password;
}
