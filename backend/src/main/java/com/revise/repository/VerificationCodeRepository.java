package com.revise.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revise.entity.VerificationCode;


public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long>{
    VerificationCode findByEmail(String email);
    void deleteByEmail(String email);
}
