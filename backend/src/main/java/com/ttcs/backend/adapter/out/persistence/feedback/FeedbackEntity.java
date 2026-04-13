package com.ttcs.backend.adapter.out.persistence.feedback;

import com.ttcs.backend.adapter.out.persistence.student.StudentEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Nationalized;

import java.time.LocalDateTime;

@Entity
@Table(name = "[Feedback]")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeedbackEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "feedback_id", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentEntity student;

    @Column(name = "title", nullable = false)
    @Nationalized
    private String title;

    @Column(name = "content", nullable = false)
    @Nationalized
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
