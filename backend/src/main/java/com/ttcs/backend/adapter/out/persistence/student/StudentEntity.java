package com.ttcs.backend.adapter.out.persistence.student;

import com.ttcs.backend.adapter.out.persistence.StatusEntity;
import com.ttcs.backend.adapter.out.persistence.department.DepartmentEntity;
import com.ttcs.backend.adapter.out.persistence.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "Student")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StudentEntity {
    @Id
    @Column(name = "user_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @Column(nullable = false)
    @Nationalized
    private String name;

    @Column( name = "student_code", nullable = false, unique = true)
    private String studentCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", nullable = false)
    private DepartmentEntity department;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private StatusEntity status;

    @Column(name = "student_card_img")
    @Nationalized
    private String studentCardImageUrl;

    @Column(name = "national_id_img")
    @Nationalized
    private String nationalIdImageUrl;

    @Column(name = "review_reason")
    @Nationalized
    private String reviewReason;

    @Column(name = "review_notes")
    @Nationalized
    private String reviewNotes;

    @Column(name = "reviewed_by_user_id")
    private Integer reviewedByUserId;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "resubmission_count", nullable = false)
    private Integer resubmissionCount;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = StatusEntity.EMAIL_UNVERIFIED;
        }
        if (resubmissionCount == null) {
            resubmissionCount = 0;
        }
    }
}
