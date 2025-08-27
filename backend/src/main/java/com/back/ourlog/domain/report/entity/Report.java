package com.back.ourlog.domain.report.entity;

import com.back.ourlog.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"reporter_id", "target_id", "type"})
        }
)
public class Report {
    @Id
    @GeneratedValue
    private Integer id;

    @Enumerated(EnumType.STRING)
    private ReportReason type;

    @Column(length = 255)
    private String description;

    @ManyToOne(fetch = LAZY)
    private User reporter;

    @ManyToOne(fetch = LAZY)
    private User target;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime reportedAt;
}

