package com.example.simple_board.web;

import com.example.simple_board.exception.NotHaveAuthorityException;
import com.example.simple_board.model.dto.ArticleDto;
import com.example.simple_board.security.CustomUserDetails;
import com.example.simple_board.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Slf4j
@Controller
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    // 홈 url 게시글 리스트 url 리다이렉트
    @GetMapping("/")
    public String redirectToArticleList() {
        return "redirect:/article/list";
    }

    // 게시글 리스트 조회
    @GetMapping(value = "/article/list")
    public String getArticleList(@PageableDefault(page = 0, size = 15,
            sort = "time", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
        Page<ArticleDto> page = articleService.findAll(pageable);
        model.addAttribute("page", page);

        return "article/article-list";
    }

    // 게시글 저장 폼 전달
    @GetMapping(value = "/article/add")
    public String getArticleForm(Model model) {
        model.addAttribute("articleDto", new ArticleDto());

        return "article/article-add";
    }

    // 게시글 저장
    @PostMapping(value = "/article/add")
    public String addArticleForm(@Validated @ModelAttribute("articleDto") ArticleDto articleDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "article/article-add";
        }

        ArticleDto savedArticleDto = articleService.create(articleDto);

        return "redirect:/article/content/" + savedArticleDto.getId();
    }

    // 게시글 내용 조회
    @GetMapping(value = "/article/content/{id}")
    public String getArticle(@PathVariable Long id, @AuthenticationPrincipal CustomUserDetails loginUser, Model model) {
        model.addAttribute("articleDto", articleService.findById(id));
        model.addAttribute("loginUser", loginUser);

        return "article/article-content";
    }

    // 게시글 삭제
    @PostMapping(value = "/article/delete/{id}")
    public String deleteArticle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            articleService.deleteArticle(id);
        } catch (NotHaveAuthorityException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/article/content/" + id;
        } catch (IllegalArgumentException e) {
            return "redirect:/article/list";
        }

        return "redirect:/article/list";
    }

    // 게시글 수정 폼 전달
    @GetMapping(value = "/article/update/{id}")
    public String getArticleUpdateForm(Model model, @PathVariable Long id) {
        model.addAttribute("articleDto", articleService.findById(id));

        return "article/article-update";
    }

    // 게시글 수정
    @PostMapping(value = "/article/update")
    public String postArticleUpdate(@Validated @ModelAttribute("articleDto") ArticleDto articleDto, BindingResult bindingResult) {


        if (bindingResult.hasErrors()) {
            return "article/article-update";
        }

        try {
            ArticleDto updatedArticleDto = articleService.update(articleDto);
        } catch (NotHaveAuthorityException e) {
            bindingResult.reject("updateFail", e.getMessage());
            return "article/article-update";
        } catch (IllegalArgumentException e) {
            return "redirect:/article/list";
        }


        return "redirect:/article/content/" + articleDto.getId();
    }
}
