package com.example.simple_board.repository;

import com.example.simple_board.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    
}
