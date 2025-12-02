package com.example.simple_board.web;

import com.example.simple_board.model.dto.ArticleDto;
import com.example.simple_board.service.ArticleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(ArticleController.class)
public class ArticleControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ArticleService articleService;

    @Test
    @DisplayName("게시글 리스트 조회 테스트")
    void getListsTest() throws Exception {
        //given
        Pageable pageable = PageRequest.of(0, 5);
        List<ArticleDto> dtos = List.of(
                new ArticleDto(1l, "1234", "title", "content", LocalDateTime.of(2025,1,1,0,0)),
                new ArticleDto(2l, "1234", "title", "content", LocalDateTime.of(2025,1,1,0,0)),
                new ArticleDto(3l, "1234", "title", "content", LocalDateTime.of(2025,1,1,0,0)),
                new ArticleDto(4l, "1234", "title", "content", LocalDateTime.of(2025,1,1,0,0)),
                new ArticleDto(5l, "1234", "title", "content", LocalDateTime.of(2025,1,1,0,0))
        );
        Page<ArticleDto> page = new PageImpl<>(dtos, pageable, 10);
        given(articleService.findAll(any(Pageable.class))).willReturn(page);

        //when&then
        mockMvc.perform(get("/article/list"))
                .andExpect(status().isOk())
                .andExpect(view().name("article-list"))
                .andExpect(model().attributeExists("page"))
                .andExpect(model().attribute("page", page));
    }

    @Test
    @DisplayName("게시글 작성 화면 전송 테스트")
    void getArticleFormTest() throws Exception {

        mockMvc.perform(get("/article/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("article-add"))
                .andExpect(model().attributeExists("articleDto"))
                .andExpect(model().attribute("articleDto",instanceOf(ArticleDto.class)));
    }


    @Test
    @DisplayName("게시글 등록 실패 - 검증 실패")
    void postArticleDtoFailTest() throws Exception {

        mockMvc.perform(post("/article/add")
                    .param("password", "")
                    .param("title", "")
                    .param("content", "content"))
                .andExpect(status().isOk())
                .andExpect(view().name("article-add"))
                .andExpect(model().attributeHasFieldErrors(
                        "articleDto", "password", "title"
                ));


        verify(articleService, never()).create(any());
    }

    @Test
    @DisplayName("게시글 등록 성공")
    void postArticleDtoSuccessTest() throws Exception {

        //given
        ArticleDto saved = new ArticleDto(1L, "password", "title", "content", LocalDateTime.of(2025,1,1,0,0));
        given(articleService.create(any(ArticleDto.class))).willReturn(saved);

        //when&then
        mockMvc.perform(post("/article/add")
                        .param("password", "password")
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
        ArticleDto articleDto = new ArticleDto(1L, "password", "title", "content", LocalDateTime.of(2025,1,1,0,0));
        given(articleService.findById(1L)).willReturn(articleDto);

        //when&then
        mockMvc.perform(get("/article/content/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("article-content"))
                .andExpect(model().attributeExists("articleDto"))
                .andExpect(model().attribute("articleDto", hasProperty("id", equalTo(1L))))
                .andExpect(model().attributeExists("deletedForm"))
                .andExpect(model().attribute("deletedForm", hasProperty("id", equalTo(1L))));
    }

    @Test
    @DisplayName("삭제 실패")
    void deletedFailTest() throws Exception {

        //given
        ArticleDto articleDto = new ArticleDto(1L, "password", "title", "content", LocalDateTime.of(2025,1,1,0,0));
        given(articleService.findById(1L)).willReturn(articleDto);

        //when&then
        mockMvc.perform(post("/article/delete")
                        .param("id", "1")
                        .param("password", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("article-content"))
                .andExpect(model().attributeHasFieldErrors(
                        "deletedForm", "password"));

        verify(articleService, never()).deleteArticle(any(Long.class));
    }

    @Test
    @DisplayName("삭제 성공")
    void deletedSuccess() throws Exception {

        //given
        ArticleDto articleDto = new ArticleDto(1L, "password", "title", "content", LocalDateTime.of(2025,1,1,0,0));
        given(articleService.findById(1L)).willReturn(articleDto);

        //when&then
        mockMvc.perform(post("/article/delete")
                    .param("id", "1")
                    .param("password", "password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/article/list"))
                .andExpect(redirectedUrl("/article/list"));

        verify(articleService).deleteArticle(1L);
    }

    @Test
    @DisplayName("게시글 수정 폼 전달 테스트")
    void getUpdateFormTest() throws Exception {

        //given
        ArticleDto articleDto = new ArticleDto(1L, "password", "title", "content", LocalDateTime.of(2025,1,1,0,0));
        given(articleService.findById(1L)).willReturn(articleDto);

        //when&then
        mockMvc.perform(get("/article/update/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("article-update"))
                .andExpect(model().attributeExists("articleDto"))
                .andExpect(model().attribute("articleDto", instanceOf(ArticleDto.class)));
    }

    @Test
    @DisplayName("게시글 수정 실패")
    void updateFailTest() throws Exception {

        //given
        ArticleDto articleDto = new ArticleDto(1L, "password", "title", "content", LocalDateTime.of(2025,1,1,0,0));
        given(articleService.findById(1L)).willReturn(articleDto);

        //when&then
        mockMvc.perform(post("/article/update")
                        .param("id", "1")
                    .param("password", "")
                    .param("title", "")
                    .param("content", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("article-update"))
                .andExpect(model().attributeHasFieldErrors(
                        "articleDto", "password", "title"));

        verify(articleService, never()).update(any(ArticleDto.class));
    }

    @Test
    @DisplayName("게시글 수정 성공")
    void updateSuccessTest() throws Exception {

        //given
        ArticleDto articleDto = new ArticleDto(1L, "password", "title", "content", LocalDateTime.of(2025,1,1,0,0));
        given(articleService.findById(1L)).willReturn(articleDto);
        ArticleDto updated = new ArticleDto(1L, "password", "updateTitle", "updatedContent", LocalDateTime.of(2025,1,1,0,0));
        given(articleService.update(any(ArticleDto.class))).willReturn(updated);

        //when&then
        mockMvc.perform(post("/article/update")
                    .param("id", "1")
                        .param("password", "password")
                    .param("title", "updatedTitle")
                    .param("content", "updatedContent"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/article/content/1"))
                .andExpect(redirectedUrl("/article/content/1"));

        verify(articleService).update(any(ArticleDto.class));
    }
}
