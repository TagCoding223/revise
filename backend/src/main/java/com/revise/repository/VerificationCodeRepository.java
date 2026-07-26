package com.revise.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revise.entity.VerificationCode;


public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long>{
    Optional<VerificationCode> findByEmail(String email);
    void deleteByEmail(String email);
}
