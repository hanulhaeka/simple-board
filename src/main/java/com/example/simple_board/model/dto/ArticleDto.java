package com.example.simple_board.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleDto {

    private Long id;

    @NotBlank(message = "비밀번호를 입력해야합니다.")
    private String password;

    @NotBlank(message = "제목을 입력해야합니다.")
    private String title;
    private String content;
    private LocalDateTime time;
}
