package com.example.simple_board;

import com.example.simple_board.model.Article;
import com.example.simple_board.model.Role;
import com.example.simple_board.model.User;
import com.example.simple_board.model.dto.ArticleDto;
import com.example.simple_board.repository.ArticleRepository;
import com.example.simple_board.repository.UserRepository;
import com.example.simple_board.security.CustomUserDetails;
import com.example.simple_board.service.ArticleService;
import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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

    @Autowired
    EntityManager em;

    @Test
    @DisplayName("게시글 저장")
    void saveArticleTest() throws Exception {

        //given
        User user = new User("userId", "password", Role.USER);
        userRepository.save(user);

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails customUserDetails = new CustomUserDetails(user.getId(), "userId", "password", authorities);



        //when&then
        mockMvc.perform(post("/article/add")
                        .with(csrf())
                        .with(user(customUserDetails))
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
        assertThat(article.getUser().getId()).isEqualTo(user.getId());
        assertThat(article.getUser().getUserId()).isEqualTo("userId");

    }

    @Test
    @DisplayName("게시글 조회")
    void findArticleTest() throws Exception {

        //given
        Article article = new Article("title", "content");
        User user = new User("userId", "password", Role.USER);
        article.setUser(user);
        articleRepository.save(article);


        //when&then
        mockMvc.perform(get("/article/content/" + article.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("article/article-content"))
                .andExpect(model().attributeExists("articleDto"))
                .andExpect(model().attribute("articleDto",
                        hasProperty("id", equalTo(article.getId()))))
                .andExpect(model().attribute("articleDto",
                        hasProperty("title", equalTo("title"))))
                .andExpect(model().attribute("articleDto",
                        hasProperty("content", equalTo("content"))))
                .andExpect(model().attribute("loginUser", nullValue()));
    }

    @Test
    @DisplayName("게시글 수정")
    void updateArticleTest() throws Exception {

        //given
        User user = new User("userId", "password", Role.USER);
        userRepository.save(user);

        Article article = new Article( "title", "content");
        article.setUser(user);
        articleRepository.save(article);

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails customUserDetails = new CustomUserDetails(user.getId(), "userId", "password", authorities);

        //when&then
        mockMvc.perform(post("/article/update")
                        .with(user(customUserDetails))
                        .with(csrf())
                    .param("id", String.valueOf(article.getId()))
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

        //given
        User user = new User("userId", "password", Role.USER);
        userRepository.save(user);

        Article article = new Article( "title", "content");
        article.setUser(user);
        articleRepository.save(article);

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails customUserDetails = new CustomUserDetails(user.getId(), "userId", "password", authorities);

        //when&then
        mockMvc.perform(post("/article/delete/" + article.getId())
                        .with(user(customUserDetails))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/article/list"));

        assertThat(articleRepository.findById(article.getId())).isEmpty();
    }

}
