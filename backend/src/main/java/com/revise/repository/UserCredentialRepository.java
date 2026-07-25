package com.revise.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.revise.entity.UserCredential;

public interface UserCredentialRepository extends JpaRepository<UserCredential, String>{
    // Basic CRUD operations are auto-implemented
}
