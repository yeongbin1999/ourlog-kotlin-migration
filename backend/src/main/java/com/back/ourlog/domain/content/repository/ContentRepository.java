package com.back.ourlog.domain.content.repository;

import com.back.ourlog.domain.content.entity.Content;
import com.back.ourlog.domain.content.entity.ContentType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ContentRepository extends JpaRepository<Content, Integer> {
    Optional<Content> findByExternalIdAndType(String externalId, ContentType type);
}