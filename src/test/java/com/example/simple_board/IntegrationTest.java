package com.example.simple_board;

import com.example.simple_board.model.Article;
import com.example.simple_board.model.dto.ArticleDto;
import com.example.simple_board.repository.ArticleRepository;
import com.example.simple_board.service.ArticleService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class IntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ArticleRepository articleRepository;

    @Test
    @DisplayName("게시글 저장")
    void saveArticleTest() throws Exception {

        mockMvc.perform(post("/article/add")
                    .param("password", "password")
                    .param("title", "title")
                    .param("content", "content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/article/content/*"));

        assertThat(articleRepository.count()).isEqualTo(1);

        Article article = articleRepository.findAll().get(0);

        assertThat(article.getId()).isNotNull();
        assertThat(article.getPassword()).isEqualTo("password");
        assertThat(article.getTitle()).isEqualTo("title");
        assertThat(article.getContent()).isEqualTo("content");
        assertThat(article.getTime()).isNotNull();

    }

    @Test
    @DisplayName("게시글 조회")
    void findArticleTest() throws Exception {

        Article article = new Article("password", "title", "content");
        articleRepository.save(article);

        mockMvc.perform(get("/article/content/" + article.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("article-content"))
                .andExpect(model().attributeExists("articleDto"))
                .andExpect(model().attributeExists("deletedForm"))
                .andExpect(model().attribute("articleDto",
                        hasProperty("id", equalTo(article.getId()))))
                .andExpect(model().attribute("articleDto",
                        hasProperty("password", equalTo("password"))))
                .andExpect(model().attribute("articleDto",
                        hasProperty("title", equalTo("title"))))
                .andExpect(model().attribute("articleDto",
                        hasProperty("content", equalTo("content"))))
                .andExpect(model().attribute("deletedForm",
                        hasProperty("id", equalTo(article.getId()))));
    }

    @Test
    @DisplayName("게시글 수정")
    void updateArticleTest() throws Exception {

        Article article = new Article("password", "title", "content");
        articleRepository.save(article);

        mockMvc.perform(post("/article/update")
                    .param("id", String.valueOf(article.getId()))
                    .param("password", "password")
                    .param("title", "updatedTitle")
                    .param("content", "updatedContent"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/article/content/*"));

        Article updated = articleRepository.findById(article.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("updatedTitle");
        assertThat(updated.getContent()).isEqualTo("updatedContent");

    }

    @Test
    @DisplayName("게시글 삭제")
    void deleteArticleTest() throws Exception {

        Article article = new Article("password", "title", "content");
        articleRepository.save(article);

        mockMvc.perform(post("/article/delete")
                .param("id", String.valueOf(article.getId()))
                .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/article/list"));

        assertThat(articleRepository.findById(article.getId())).isEmpty();
    }

}
