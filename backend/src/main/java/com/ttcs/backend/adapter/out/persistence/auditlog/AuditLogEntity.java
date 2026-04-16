package com.ttcs.backend.adapter.out.persistence.auditlog;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "Audit_Log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Integer id;

    @Column(name = "actor_user_id", nullable = false)
    private Integer actorUserId;

    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @Column(name = "target_type", nullable = false, length = 30)
    private String targetType;

    @Column(name = "target_id", nullable = false)
    private Integer targetId;

    @Nationalized
    @Column(name = "summary", nullable = false, length = 255)
    private String summary;

    @Nationalized
    @Column(name = "details", columnDefinition = "NVARCHAR(MAX)")
    private String details;

    @Nationalized
    @Column(name = "old_state", length = 255)
    private String oldState;

    @Nationalized
    @Column(name = "new_state", length = 255)
    private String newState;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
