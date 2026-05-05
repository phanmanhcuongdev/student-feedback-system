package com.ttcs.backend.adapter.out.persistence.notificationuser;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
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
            SELECT notificationUser
            FROM NotificationUserEntity notificationUser
            JOIN FETCH notificationUser.notification notification
            LEFT JOIN FETCH notification.survey
            WHERE notificationUser.user.id = :userId
                AND notificationUser.readAt IS NULL
            ORDER BY notification.createdAt DESC, notificationUser.id DESC
            """)
    List<NotificationUserEntity> findUnreadForUser(@Param("userId") Integer userId);

    @Query("""
            SELECT COUNT(notificationUser)
            FROM NotificationUserEntity notificationUser
            WHERE notificationUser.user.id = :userId
                AND notificationUser.readAt IS NULL
            """)
    long countUnreadByUserId(@Param("userId") Integer userId);

    Optional<NotificationUserEntity> findByIdAndUser_Id(Integer id, Integer userId);

    boolean existsByUser_IdAndNotification_TypeAndNotification_Survey_Id(Integer userId, String type, Integer surveyId);

    @Query("""
            SELECT COUNT(notificationUser) > 0
            FROM NotificationUserEntity notificationUser
            JOIN notificationUser.notification notification
            WHERE notificationUser.user.id = :userId
                AND notification.type = :type
                AND notification.survey.id = :surveyId
                AND notification.createdAt >= :startOfDay
                AND notification.createdAt < :startOfNextDay
            """)
    boolean existsForUserAndSurveyOnDate(
            @Param("userId") Integer userId,
            @Param("type") String type,
            @Param("surveyId") Integer surveyId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("startOfNextDay") LocalDateTime startOfNextDay
    );

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
