package com.example.simple_board.service;

import com.example.simple_board.model.Article;
import com.example.simple_board.model.dto.ArticleDto;
import com.example.simple_board.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;

    // 전체 리스트 페이징과 정렬해서 조회
    @Transactional(readOnly = true)
    public Page<ArticleDto> findAll(Pageable pageable) {
        return articleRepository.findAll(pageable).map(this::mapToArticleDto);
    }

    // 게시글 하나 조회
    @Transactional(readOnly = true)
    public ArticleDto findById(Long id) {
        return articleRepository.findById(id).map(this::mapToArticleDto).orElseThrow();
    }

    // 게시글 저장
    @Transactional
    public ArticleDto create(ArticleDto articleDto) {

        Article article = new Article(
                articleDto.getPassword(),
                articleDto.getTitle(),
                articleDto.getContent()
        );
        Article savedArticle = articleRepository.save(article);
        return mapToArticleDto(savedArticle);
    }

    // 게시글 수정
    @Transactional
    public ArticleDto update(ArticleDto articleDto) {
        Article article = articleRepository.findById(articleDto.getId()).orElseThrow();

        article.setTitle(articleDto.getTitle());
        article.setContent(articleDto.getContent());

        articleRepository.save(article);

        return mapToArticleDto(article);

    }

    // 게시글 삭제
    @Transactional
    public void deleteArticle(Long id) {
        Article article = articleRepository.findById(id).orElseThrow();

        articleRepository.deleteById(id);
    }



    private ArticleDto mapToArticleDto(Article article) {
        ArticleDto articleDto = new ArticleDto();
        articleDto.setId(article.getId());
        articleDto.setTime(article.getTime());
        articleDto.setTitle(article.getTitle());
        articleDto.setContent(article.getContent());
        articleDto.setPassword(article.getPassword());
        return articleDto;
    }

}
