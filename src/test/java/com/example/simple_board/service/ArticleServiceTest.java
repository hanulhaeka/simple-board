package com.example.simple_board.service;

import com.example.simple_board.model.Article;
import com.example.simple_board.model.dto.ArticleDto;
import com.example.simple_board.repository.ArticleRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {

    @Mock
    ArticleRepository articleRepository;

    @InjectMocks
    ArticleService articleService;


    @Test
    @DisplayName("게시글 단건 조회")
    void find() {
        //given
        Article article = new Article("1234", "title", "Content");
        article.setId(1L);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        //when
        ArticleDto articleDto = articleService.findById(1L);

        //then
        assertThat(articleDto.getTitle()).isEqualTo("title");
        assertThat(articleDto.getContent()).isEqualTo("Content");
        assertThat(articleDto.getPassword()).isEqualTo("1234");

    }

    @Test
    @DisplayName("페이징 조회 테스트")
    void findAll() throws InterruptedException {
        //given
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        List<Article> content = new ArrayList<>();
        for (int i = 5; i >= 1; i--) {
            Article article = new Article("password" + i, "title" + i, "content" + i);
            article.setTime(LocalDateTime.now());
            content.add(article);
        }
        Page<Article> page = new PageImpl<>(content, pageable, 5L);

        when(articleRepository.findAll(pageable)).thenReturn(page);

        //when
        Page<ArticleDto> dtoPage = articleService.findAll(pageable);

        //then
        assertThat(dtoPage.getTotalElements()).isEqualTo(5);
        assertThat(dtoPage.getTotalPages()).isEqualTo(1);
        List<ArticleDto> dtos = dtoPage.getContent();
        assertThat(dtos.size()).isEqualTo(5);
        assertThat(dtos.getFirst().getTitle()).isEqualTo("title5");

    }

    @Test
    @DisplayName("저장 테스트")
    void createTest() {
        //given
        ArticleDto articleDto = new ArticleDto();
        articleDto.setTitle("title");
        articleDto.setContent("content");
        articleDto.setPassword("1234");

        Article saved = new Article("1234", "title", "content");
        saved.setId(1L);

        when(articleRepository.save(any(Article.class))).thenReturn(saved);

        //when
        ArticleDto savedDto = articleService.create(articleDto);

        //then
        assertThat(savedDto.getId()).isEqualTo(1L);
        assertThat(savedDto.getTitle()).isEqualTo("title");
        assertThat(savedDto.getContent()).isEqualTo("content");
    }

    @Test
    @DisplayName("수정 테스트")
    void updateTest() {
        //given
        ArticleDto articleDto = new ArticleDto();
        articleDto.setPassword("1234");
        articleDto.setTitle("title");
        articleDto.setContent("content");
        articleDto.setId(1L);

        Article article = new Article("1234", "beforeTitle", "beforeContent");
        article.setId(1L);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(article));

        //when
        ArticleDto updated = articleService.update(articleDto);

        //then
        assertThat(updated.getTitle()).isEqualTo("title");
        assertThat(updated.getContent()).isEqualTo("content");
    }

    @Test
    @DisplayName("삭제 테스트 - 예외 발생")
    void deleteTest() {
        when(articleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> articleService.deleteArticle(1L)).isInstanceOf(NoSuchElementException.class);

        verify(articleRepository).findById(1L);
        verify(articleRepository, never()).deleteById(anyLong());
    }
}
