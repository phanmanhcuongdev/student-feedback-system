package com.ttcs.backend.adapter.out.persistence.notificationuser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface NotificationUserRepository extends JpaRepository<NotificationUserEntity, Integer> {
    @Query(
            value = """
            SELECT notificationUser
            FROM NotificationUserEntity notificationUser
            JOIN FETCH notificationUser.notification notification
            LEFT JOIN FETCH notification.survey
            WHERE notificationUser.user.id = :userId
                AND (:unreadOnly = false OR notificationUser.readAt IS NULL)
            ORDER BY notification.createdAt DESC, notificationUser.id DESC
            """,
            countQuery = """
            SELECT COUNT(notificationUser)
            FROM NotificationUserEntity notificationUser
            WHERE notificationUser.user.id = :userId
                AND (:unreadOnly = false OR notificationUser.readAt IS NULL)
            """
    )
    Page<NotificationUserEntity> findPageForUser(
            @Param("userId") Integer userId,
            @Param("unreadOnly") boolean unreadOnly,
            Pageable pageable
    );

    @Query("""
            SELECT COUNT(notificationUser)
            FROM NotificationUserEntity notificationUser
            WHERE notificationUser.user.id = :userId
                AND notificationUser.readAt IS NULL
            """)
    long countUnreadByUserId(@Param("userId") Integer userId);

    Optional<NotificationUserEntity> findByIdAndUser_Id(Integer id, Integer userId);

    boolean existsByUser_IdAndNotification_TypeAndNotification_Survey_Id(Integer userId, String type, Integer surveyId);

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query("""
            UPDATE NotificationUserEntity notificationUser
            SET notificationUser.readAt = :readAt
            WHERE notificationUser.user.id = :userId
                AND notificationUser.readAt IS NULL
            """)
    int markAllUnreadAsRead(@Param("userId") Integer userId, @Param("readAt") LocalDateTime readAt);
}
