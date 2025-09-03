package com.back.ourlog.global.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@Configuration
@EnableJpaAuditing // JPA Auditing 기능 활성화
class JpaAuditingConfig {
    // 필요 시 AuditorAware 인터페이스 구현체를 Bean으로 등록 가능
    // 예: @CreatedBy, @LastModifiedBy 필드에 사용자 ID 자동 주입
}