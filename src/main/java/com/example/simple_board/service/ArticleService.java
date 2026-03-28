package com.example.simple_board.service;

import com.example.simple_board.exception.NotHaveAuthorityException;
import com.example.simple_board.model.Article;
import com.example.simple_board.model.Role;
import com.example.simple_board.model.User;
import com.example.simple_board.model.dto.ArticleDto;
import com.example.simple_board.repository.ArticleRepository;
import com.example.simple_board.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public ArticleDto create(ArticleDto articleDto) {

        User user = getCurrentUser();

        Article article = new Article(
                articleDto.getTitle(),
                articleDto.getContent()
        );

        article.setUser(user);

        Article savedArticle = articleRepository.save(article);
        return mapToArticleDto(savedArticle);
    }

    // 게시글 수정
    @Transactional
    public ArticleDto update(ArticleDto articleDto) {

        User user = getCurrentUser();

        Article article = articleRepository.findById(articleDto.getId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (article.getUser().getId().equals(user.getId()) || user.getRole() == Role.ADMIN) {
            article.setTitle(articleDto.getTitle());
            article.setContent(articleDto.getContent());
        } else {
            throw new NotHaveAuthorityException("게시글을 수정할 권한이 없습니다.");
        }

        return mapToArticleDto(article);

    }

    // 게시글 삭제
    @Transactional
    public void deleteArticle(Long id) {

        User user = getCurrentUser();
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시글입니다."));

        if (article.getUser().getId().equals(user.getId()) || user.getRole() == Role.ADMIN) {
            articleRepository.delete(article);
        } else {
            throw new NotHaveAuthorityException("게시글을 삭제할 권한이 없습니다.");
        }

    }


    // article -> ArticleDto
    private ArticleDto mapToArticleDto(Article article) {
        ArticleDto articleDto = new ArticleDto();
        articleDto.setId(article.getId());
        articleDto.setTime(article.getTime());
        articleDto.setTitle(article.getTitle());
        articleDto.setContent(article.getContent());
        articleDto.setUserId(article.getUser().getId());
        return articleDto;
    }


    // SecurityContextHolder 에서 User 객체 꺼내기
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("로그인 사용자를 찾을 수 없습니다"));
    }

}
