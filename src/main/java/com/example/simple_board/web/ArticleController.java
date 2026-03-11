package com.example.simple_board.web;

import com.example.simple_board.auth.SessionUser;
import com.example.simple_board.model.dto.ArticleDto;
import com.example.simple_board.service.ArticleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;



@Slf4j
@Controller
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/")
    public String redirectToArticleList() {
        return "redirect:/article/list";
    }

    @GetMapping(value = "/article/list")
    public String getArticleList(@PageableDefault(page = 0, size = 15,
            sort = "time", direction = Sort.Direction.DESC) Pageable pageable, Model model, HttpServletRequest request) {

        SessionUser sessionUser = request.getSession(false) != null
                ? (SessionUser) request.getSession(false).getAttribute("loginUser")
                : null;

        Page<ArticleDto> page = articleService.findAll(pageable);
        model.addAttribute("page", page);
        model.addAttribute("sessionUser", sessionUser);

        return "article/article-list";
    }

    @GetMapping(value = "/article/add")
    public String getArticleForm(Model model) {
        model.addAttribute("articleDto", new ArticleDto());

        return "article/article-add";
    }

    @PostMapping(value = "/article/add")
    public String addArticleForm(@Validated @ModelAttribute("articleDto") ArticleDto articleDto, BindingResult bindingResult, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "article/article-add";
        }

        HttpSession session = request.getSession();
        SessionUser loginUser = (SessionUser) session.getAttribute("loginUser");

        ArticleDto savedArticleDto = articleService.create(articleDto, loginUser.getUserId());

        return "redirect:/article/content/" + savedArticleDto.getId();
    }

    @GetMapping(value = "/article/content/{id}")
    public String getArticle(@PathVariable Long id, Model model, HttpServletRequest request) {

        SessionUser sessionUser = request.getSession(false) != null
                ? (SessionUser) request.getSession(false).getAttribute("loginUser")
                : null;

        model.addAttribute("articleDto", articleService.findById(id));
        model.addAttribute("sessionUser", sessionUser);

        return "article/article-content";
    }

    @PostMapping(value = "/article/delete/{id}")
    public String deleteArticle(@PathVariable Long id, HttpServletRequest request) {

        SessionUser loginUser = (SessionUser) request.getSession().getAttribute("loginUser");

        articleService.deleteArticle(id, loginUser.getUserId(), loginUser.getRole());

        return "redirect:/article/list";
    }

    @GetMapping(value = "/article/update/{id}")
    public String getArticleUpdateForm(Model model, @PathVariable Long id) {
        model.addAttribute("articleDto", articleService.findById(id));

        return "article/article-update";
    }

    @PostMapping(value = "/article/update")
    public String postArticleUpdate(@Validated @ModelAttribute("articleDto") ArticleDto articleDto, BindingResult bindingResult, HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "article/article-update";
        }

        SessionUser loginUser = (SessionUser) request.getSession().getAttribute("loginUser");

        ArticleDto updatedArticleDto = articleService.update(articleDto, loginUser.getUserId(), loginUser.getRole());

        return "redirect:/article/content/" + updatedArticleDto.getId();
    }
}
