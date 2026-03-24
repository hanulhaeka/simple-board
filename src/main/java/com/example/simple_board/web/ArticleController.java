package com.example.simple_board.web;

import com.example.simple_board.model.dto.ArticleDto;
import com.example.simple_board.model.dto.DeletedForm;
import com.example.simple_board.service.ArticleService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;



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
            sort = "time", direction = Sort.Direction.DESC) Pageable pageable, Model model) {
        Page<ArticleDto> page = articleService.findAll(pageable);
        model.addAttribute("page", page);

        return "article/article-list";
    }

    @GetMapping(value = "/article/add")
    public String getArticleForm(Model model) {
        model.addAttribute("articleDto", new ArticleDto());

        return "article/article-add";
    }

    @PostMapping(value = "/article/add")
    public String addArticleForm(@Validated @ModelAttribute("articleDto") ArticleDto articleDto, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "article/article-add";
        }

        ArticleDto savedArticleDto = articleService.create(articleDto);

        return "redirect:/article/content/" + savedArticleDto.getId();
    }

    @GetMapping(value = "/article/content/{id}")
    public String getArticle(@PathVariable Long id, Model model) {
        model.addAttribute("articleDto", articleService.findById(id));
        DeletedForm deletedForm = new DeletedForm();
        deletedForm.setId(id);
        model.addAttribute("deletedForm", deletedForm);

        return "article/article-content";
    }

    @PostMapping(value = "/article/delete")
    public String deleteArticle(@Validated @ModelAttribute DeletedForm deletedForm, BindingResult bindingResult, Model model) {

        if (!deletedForm.getPassword().equals(articleService.findById(deletedForm.getId()).getPassword())) {
            bindingResult.rejectValue("password", "wrongPassword", "비밀번호가 틀렸습니다.");
        }

        if (bindingResult.hasErrors()) {

            model.addAttribute("articleDto", articleService.findById(deletedForm.getId()));

            return "article/article-content";
        }

        articleService.deleteArticle(deletedForm.getId());

        return "redirect:/article/list";
    }

    @GetMapping(value = "/article/update/{id}")
    public String getArticleUpdateForm(Model model, @PathVariable Long id) {
        model.addAttribute("articleDto", articleService.findById(id));

        return "article/article-update";
    }

    @PostMapping(value = "/article/update")
    public String postArticleUpdate(@Validated @ModelAttribute("articleDto") ArticleDto articleDto, BindingResult bindingResult) {


        if (!articleDto.getPassword().equals(articleService.findById(articleDto.getId()).getPassword())) {
            bindingResult.rejectValue("password", "wrongPassword", "비밀번호가 틀렸습니다.");
        }

        if (bindingResult.hasErrors()) {
            return "article/article-update";
        }

        ArticleDto updatedArticleDto = articleService.update(articleDto);

        return "redirect:/article/content/" + updatedArticleDto.getId();
    }
}
