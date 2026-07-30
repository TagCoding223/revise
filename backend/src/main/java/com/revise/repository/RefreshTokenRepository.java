package com.revise.repository;

import com.revise.entity.RefreshToken;
import com.revise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
    Optional<RefreshToken> findByToken(String token);
    
    // Custom method to clear old tokens when a user logs in again
    int deleteByUser(User user); 
}