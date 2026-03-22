package com.ttcs.backend.adapter.out.persistence;

import lombok.*;
import org.hibernate.annotations.Nationalized;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Teacher")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TeacherEntity {
    @Id
    @Column(name = "user_id")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserEntity user;

    @Column(nullable = false)
    @Nationalized
    private String name;

    @Column( name = "teacher_code", nullable = false, unique = true)
    private String teacherCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dept_id", nullable = false)
    private DepartmentEntity department;
}
