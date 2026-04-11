package com.blog.service;

public interface UserActionService {
    
    boolean toggleLike(Long articleId);
    
    boolean toggleFavorite(Long articleId);
    
    boolean checkLiked(Long articleId, Long userId);
    
    boolean checkFavorited(Long articleId, Long userId);
}
