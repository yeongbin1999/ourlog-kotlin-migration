package com.back.ourlog.domain.user.repository;

import com.back.ourlog.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    Page<User> findByNicknameContainingIgnoreCase(String keyword, Pageable pageable);
}
