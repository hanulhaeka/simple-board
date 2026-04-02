package com.example.simple_board.repository;


import com.example.simple_board.config.JpaAuditingConfig;
import com.example.simple_board.model.Article;

import com.example.simple_board.model.Role;
import com.example.simple_board.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;


import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(JpaAuditingConfig.class)
public class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    EntityManager em;

    @Test
    @DisplayName("게시글 저장 및 조회 테스트")
    void saveAndFind() {
        //given
        Article article = new Article( "title", "content");
        User savedUser = userRepository.save(new User("userId", "password", Role.USER));
        article.setUser(savedUser);

        //when
        Article saved = articleRepository.save(article);

        em.flush();
        em.clear();

        Article found = articleRepository.findById(article.getId()).get();

        //then
        assertThat(found.getId()).isEqualTo(article.getId());
        assertThat(found.getTitle()).isEqualTo(article.getTitle());
        assertThat(found.getContent()).isEqualTo(article.getContent());
        assertThat(found.getUser().getUserId()).isEqualTo(savedUser.getUserId());
    }

    @Test
    @DisplayName("삭제 테스트")
    void delete() {
        //given
        Article article = new Article("title", "content");
        User savedUser = userRepository.save(new User("userId", "password", Role.USER));
        article.setUser(savedUser);
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
        Article article = new Article("title", "content");
        User savedUser = userRepository.save(new User("userId", "password", Role.USER));
        article.setUser(savedUser);
        Article saved = articleRepository.save(article);

        //when
        saved.setTitle("updatedTitle");
        saved.setContent("updatedContent");

        em.flush();
        em.clear();

        Article found = articleRepository.findById(saved.getId()).orElseThrow();

        //then
        assertThat(found.getTitle()).isEqualTo("updatedTitle");
        assertThat(found.getContent()).isEqualTo("updatedContent");
    }

    @Test
    @DisplayName("페이징 테스트")
    void paging() {
        //given
        for (int i = 1; i <= 10 ; i++) {
            User savedUser = userRepository.save(new User("userId" + i, "password" + i, Role.USER));
            Article article = new Article("title " + i, "content " + i);
            article.setUser(savedUser);
            articleRepository.save(article);
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
        assertThat(articles.getFirst().getUser().getUserId()).isEqualTo("userId10");


    }

    @Test
    @DisplayName("Auditing test")
    void auditing() {
        //given
        LocalDate today = LocalDate.now();
        Article article = new Article("title", "content");
        User savedUser = userRepository.save(new User("userId", "password", Role.USER));
        article.setUser(savedUser);

        //when
        Article saved = articleRepository.save(article);
        em.flush();
        em.clear();
        Article found = articleRepository.findById(saved.getId()).orElseThrow();

        //then
        assertThat(found.getTime().toLocalDate()).isEqualTo(today);
    }
}
