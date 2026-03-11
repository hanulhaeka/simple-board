package com.example.simple_board;

import com.example.simple_board.auth.SessionUser;
import com.example.simple_board.model.Article;
import com.example.simple_board.model.Role;
import com.example.simple_board.model.User;
import com.example.simple_board.model.dto.ArticleDto;
import com.example.simple_board.repository.ArticleRepository;
import com.example.simple_board.repository.UserRepository;
import com.example.simple_board.service.ArticleService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
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

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.save(new User("userID", "password",Role.USER));

    }

    @Test
    @DisplayName("게시글 저장 - 로그인")
    void saveArticleTest() throws Exception {

        mockMvc.perform(post("/article/add")
                        .sessionAttr("loginUser", new SessionUser(1L, Role.USER))
                    .param("title", "title")
                    .param("content", "content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/article/content/*"));

        assertThat(articleRepository.count()).isEqualTo(1);

        Article article = articleRepository.findAll().get(0);

        assertThat(article.getId()).isNotNull();
        assertThat(article.getTitle()).isEqualTo("title");
        assertThat(article.getContent()).isEqualTo("content");
        assertThat(article.getTime()).isNotNull();
        assertThat(article.getUser().getId()).isEqualTo(1L);
        assertThat(article.getUser().getRole()).isEqualTo(Role.USER);

    }

    @Test
    @DisplayName("게시글 조회 - 비로그인")
    void findArticleTest() throws Exception {

        Article article = new Article( "title", "content");
        User user = userRepository.findAll().get(0);
        article.setUser(user);
        articleRepository.save(article);

        mockMvc.perform(get("/article/content/" + article.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("article/article-content"))
                .andExpect(model().attributeExists("articleDto"))
                .andExpect(model().attribute("articleDto",
                        hasProperty("id", equalTo(article.getId()))))
                .andExpect(model().attribute("articleDto",
                        hasProperty("title", equalTo("title"))))
                .andExpect(model().attribute("articleDto",
                        hasProperty("content", equalTo("content"))));
    }

    @Test
    @DisplayName("게시글 수정 - 본인 글 일 경우")
    void updateArticleTest() throws Exception {

        Article article = new Article( "title", "content");
        User user = userRepository.findAll().get(0);
        article.setUser(user);
        articleRepository.save(article);

        mockMvc.perform(post("/article/update")
                    .param("id", String.valueOf(article.getId()))
                    .param("title", "updatedTitle")
                    .param("content", "updatedContent")
                        .sessionAttr("loginUser", new SessionUser(user.getId(), Role.USER)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/article/content/*"));

        Article updated = articleRepository.findById(article.getId()).orElseThrow();
        assertThat(updated.getTitle()).isEqualTo("updatedTitle");
        assertThat(updated.getContent()).isEqualTo("updatedContent");

    }

    @Test
    @DisplayName("게시글 삭제 - 본인 글 일 경우")
    void deleteArticleTest() throws Exception {

        Article article = new Article( "title", "content");
        User user = userRepository.findAll().get(0);
        article.setUser(user);
        articleRepository.save(article);

        mockMvc.perform(post("/article/delete/" + article.getId())
                        .sessionAttr("loginUser", new SessionUser(user.getId(), Role.USER)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/article/list"));

        assertThat(articleRepository.findById(article.getId())).isEmpty();
    }

}
