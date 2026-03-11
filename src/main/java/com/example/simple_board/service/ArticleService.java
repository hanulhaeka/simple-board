package com.example.simple_board.service;

import com.example.simple_board.model.Article;
import com.example.simple_board.model.Role;
import com.example.simple_board.model.User;
import com.example.simple_board.model.dto.ArticleDto;
import com.example.simple_board.repository.ArticleRepository;
import com.example.simple_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

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
    public ArticleDto create(ArticleDto articleDto, Long userId) {

        Article article = new Article(
                articleDto.getTitle(),
                articleDto.getContent()
        );
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));


        article.setUser(user);

        Article savedArticle = articleRepository.save(article);
        return mapToArticleDto(savedArticle);
    }

    // 게시글 수정
    @Transactional
    public ArticleDto update(ArticleDto articleDto, Long userId, Role role) {
        Article article = articleRepository.findById(articleDto.getId()).orElseThrow(() -> new IllegalArgumentException("Article not found"));

        boolean isOwner = article.getUser().getId().equals(userId);
        boolean isAdmin = role == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        article.setTitle(articleDto.getTitle());
        article.setContent(articleDto.getContent());

        return mapToArticleDto(article);

    }

    // 게시글 삭제
    @Transactional
    public void deleteArticle(Long id, Long userId, Role role) {
        Article article = articleRepository.findById(id).orElseThrow();

        boolean isOwner = article.getUser().getId().equals(userId);
        boolean isAdmin = role == Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        articleRepository.deleteById(id);
    }



    private ArticleDto mapToArticleDto(Article article) {
        ArticleDto articleDto = new ArticleDto();
        articleDto.setId(article.getId());
        articleDto.setTime(article.getTime());
        articleDto.setTitle(article.getTitle());
        articleDto.setContent(article.getContent());
        articleDto.setUserId(article.getUser().getId());
        return articleDto;
    }

}
