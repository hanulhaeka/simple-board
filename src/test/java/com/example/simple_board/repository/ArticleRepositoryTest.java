package com.example.simple_board.repository;


import com.example.simple_board.model.Article;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
public class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("게시글 저장 및 조회 테스트")
    void saveAndFind() {
        //given
        Article article = new Article("1234", "title", "content");

        //when
        Article saved = articleRepository.save(article);

        em.flush();
        em.clear();

        Article found = articleRepository.findById(article.getId()).get();

        //then
        assertThat(found.getId()).isEqualTo(article.getId());
        assertThat(found.getPassword()).isEqualTo(article.getPassword());
        assertThat(found.getTitle()).isEqualTo(article.getTitle());
        assertThat(found.getContent()).isEqualTo(article.getContent());
    }

    @Test
    @DisplayName("삭제 테스트")
    void delete() {
        //given
        Article article = new Article("1234", "title", "content");
        articleRepository.save(article);
        Long id = article.getId();

        //when
        articleRepository.delete(article);
        em.flush();
        em.clear();

        //then
        assertThat(articleRepository.findById(id)).isEmpty();

    }

    @Test
    @DisplayName("수정 테스트")
    void update() {
        //given
        Article saved = articleRepository.save(new Article("1234", "title", "content"));

        //when
        saved.setTitle("updatedTitle");
        saved.setContent("updatedContent");
        saved.setPassword("0000");

        em.flush();
        em.clear();

        //then
        Article found = articleRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getTitle()).isEqualTo("updatedTitle");
        assertThat(found.getContent()).isEqualTo("updatedContent");
        assertThat(found.getPassword()).isEqualTo("0000");
    }

    @Test
    @DisplayName("페이징 테스트")
    void paging() {
        //given
        for (int i = 1; i <= 10 ; i++) {
            articleRepository.save(new Article("password " + i, "title " + i, "content " + i));
        }

        //when
        Pageable pageable = PageRequest.of(0, 5, Sort.by("time").descending());
        Page<Article> page = articleRepository.findAll(pageable);

        //then
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getTotalElements()).isEqualTo(10);
        List<Article> articles = page.getContent();
        assertThat(articles.size()).isEqualTo(5);
        assertThat(articles.getFirst().getTitle()).isEqualTo("title 10");
        assertThat(articles.getFirst().getContent()).isEqualTo("content 10");
        assertThat(articles.getFirst().getPassword()).isEqualTo("password 10");


    }

    @Test
    @DisplayName("Auditing test")
    void auditing() {
        //given
        LocalDate today = LocalDate.now();
        Article article = articleRepository.save(new Article("1234", "title", "content"));

        //when
        Article found = articleRepository.findById(article.getId()).orElseThrow();

        //then
        assertThat(found.getTime().toLocalDate()).isEqualTo(today);
    }
}
