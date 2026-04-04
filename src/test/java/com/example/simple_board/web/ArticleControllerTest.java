package com.example.simple_board.web;

import com.example.simple_board.config.SecurityConfig;
import com.example.simple_board.exception.NotHaveAuthorityException;
import com.example.simple_board.model.Role;
import com.example.simple_board.model.dto.ArticleDto;
import com.example.simple_board.security.CustomUserDetails;
import com.example.simple_board.service.ArticleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(ArticleController.class)
@Import(SecurityConfig.class)
public class ArticleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ArticleService articleService;

    @Test
    @DisplayName("게시글 리스트 조회 테스트")
    void getListsTest() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 5);
        List<ArticleDto> dtos = List.of(
                new ArticleDto(1L,  "title", "content", LocalDateTime.of(2025,1,1,0,0), 1L),
                new ArticleDto(2L,  "title", "content", LocalDateTime.of(2025,1,1,0,0), 2L),
                new ArticleDto(3L,  "title", "content", LocalDateTime.of(2025,1,1,0,0), 3L),
                new ArticleDto(4L,  "title", "content", LocalDateTime.of(2025,1,1,0,0), 4L),
                new ArticleDto(5L,  "title", "content", LocalDateTime.of(2025,1,1,0,0), 5L)
        );
        Page<ArticleDto> page = new PageImpl<>(dtos, pageable, 10);
        given(articleService.findAll(any(Pageable.class))).willReturn(page);

        //when&then
        mockMvc.perform(get("/article/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("article/article-list"))
                .andExpect(model().attributeExists("page"))
                .andExpect(model().attribute("page", page));
    }

    @Test
    @DisplayName("게시글 작성 화면 전송 테스트")
    void getArticleFormTest() throws Exception {

        //given
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails customUserDetails = new CustomUserDetails(1L, "userId", "password", authorities);

        //when&then
        mockMvc.perform(get("/article/add")
                        .with(user(customUserDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("article/article-add"))
                .andExpect(model().attributeExists("articleDto"))
                .andExpect(model().attribute("articleDto",instanceOf(ArticleDto.class)));
    }


    @Test
    @DisplayName("게시글 등록 실패 - 검증 실패")
    void postArticleDtoFailTest() throws Exception {

        //given
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails customUserDetails = new CustomUserDetails(1L, "userId", "password", authorities);

        //when&then
        mockMvc.perform(post("/article/add")
                        .with(csrf())
                        .with(user(customUserDetails))
                    .param("title", "")
                    .param("content", "content"))
                .andExpect(status().isOk())
                .andExpect(view().name("article/article-add"))
                .andExpect(model().attributeHasFieldErrors(
                        "articleDto",  "title"
                ));


        verify(articleService, never()).create(any());
    }

    @Test
    @DisplayName("게시글 등록 성공")
    void postArticleDtoSuccessTest() throws Exception {

        //given
        ArticleDto saved = new ArticleDto(1L, "title", "content", LocalDateTime.of(2025,1,1,0,0), 1L);
        given(articleService.create(any(ArticleDto.class))).willReturn(saved);

        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails customUserDetails = new CustomUserDetails(1L, "userId", "password", authorities);

        //when&then
        mockMvc.perform(post("/article/add")
                        .with(csrf())
                        .with(user(customUserDetails))
                        .param("title", "title")
                        .param("content", "content"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/article/content/1"))
                .andExpect(redirectedUrl("/article/content/1"));

    }

    @Test
    @DisplayName("게시글 하나 조회")
    void getArticleTest() throws Exception {

        //given
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails customUserDetails = new CustomUserDetails(1L, "userId", "password", authorities);

        ArticleDto articleDto = new ArticleDto(1L, "title", "content", LocalDateTime.of(2025,1,1,0,0), 1L);
        given(articleService.findById(1L)).willReturn(articleDto);

        //when&then
        mockMvc.perform(get("/article/content/1")
                        .with(user(customUserDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("article/article-content"))
                .andExpect(model().attributeExists("articleDto"))
                .andExpect(model().attribute("articleDto", hasProperty("id", equalTo(1L))))
                .andExpect(model().attributeExists("loginUser"))
                .andExpect(model().attribute("loginUser", hasProperty("id", equalTo(1L))));
    }

    @Test
    @DisplayName("삭제 실패 - 본인 글 아님")
    void deletedFailTest() throws Exception {

        //given
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails customUserDetails = new CustomUserDetails(1L, "userId", "password", authorities);

        doThrow(new NotHaveAuthorityException("삭제 권한이 없습니다")).when(articleService).deleteArticle(1L);

        //when&then
        mockMvc.perform(post("/article/delete/1")
                        .with(csrf())
                        .with(user(customUserDetails)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/article/content/1"))
                .andExpect(flash().attribute("errorMessage", "삭제 권한이 없습니다"));

    }

    @Test
    @DisplayName("삭제 성공 - 본인 글 삭제")
    void deletedSuccess() throws Exception {

        //given
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails customUserDetails = new CustomUserDetails(1L, "userId", "password", authorities);

        //when&then
        mockMvc.perform(post("/article/delete/1")
                        .with(csrf())
                        .with(user(customUserDetails)))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/article/list"))
                .andExpect(redirectedUrl("/article/list"));

        verify(articleService).deleteArticle(1L);
    }

    @Test
    @DisplayName("게시글 수정 폼 전달 테스트")
    void getUpdateFormTest() throws Exception {

        //given
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails customUserDetails = new CustomUserDetails(1L, "userId", "password", authorities);

        ArticleDto articleDto = new ArticleDto(1L, "title", "content", LocalDateTime.of(2025,1,1,0,0), 1L);
        given(articleService.findById(1L)).willReturn(articleDto);

        //when&then
        mockMvc.perform(get("/article/update/1")
                        .with(user(customUserDetails)))
                .andExpect(status().isOk())
                .andExpect(view().name("article/article-update"))
                .andExpect(model().attributeExists("articleDto"))
                .andExpect(model().attribute("articleDto", instanceOf(ArticleDto.class)));
    }

    @Test
    @DisplayName("게시글 수정 실패 - 검증 실패")
    void updateFailTest() throws Exception {

        //given
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails customUserDetails = new CustomUserDetails(1L, "userId", "password", authorities);

        //when&then
        mockMvc.perform(post("/article/update")
                        .with(csrf())
                        .with(user(customUserDetails))
                        .param("id", "1")
                        .param("title", "")
                        .param("content", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("article/article-update"))
                .andExpect(model().attributeHasFieldErrors(
                        "articleDto", "title"));

        verify(articleService, never()).update(any(ArticleDto.class));
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updateSuccessTest() throws Exception {

        //given
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        CustomUserDetails customUserDetails = new CustomUserDetails(1L, "userId", "password", authorities);


        //when&then
        mockMvc.perform(post("/article/update")
                        .with(csrf())
                        .with(user(customUserDetails))
                    .param("id", "1")
                    .param("title", "updatedTitle")
                    .param("content", "updatedContent"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/article/content/1"))
                .andExpect(redirectedUrl("/article/content/1"));

        verify(articleService).update(any(ArticleDto.class));
    }
}
