package com.ttcs.backend.adapter.out.persistence.notification;

import com.ttcs.backend.adapter.out.persistence.UserRepository;
import com.ttcs.backend.adapter.out.persistence.notificationuser.NotificationUserEntity;
import com.ttcs.backend.adapter.out.persistence.notificationuser.NotificationUserRepository;
import com.ttcs.backend.adapter.out.persistence.survey.SurveyRepository;
import com.ttcs.backend.adapter.out.persistence.user.UserEntity;
import com.ttcs.backend.application.port.out.LoadStudentNotificationPort;
import com.ttcs.backend.application.port.out.LoadedStudentNotification;
import com.ttcs.backend.application.port.out.LoadedStudentNotificationPage;
import com.ttcs.backend.application.port.out.NotificationCreateCommand;
import com.ttcs.backend.application.port.out.SaveNotificationPort;
import com.ttcs.backend.common.PersistenceAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@PersistenceAdapter
@RequiredArgsConstructor
public class NotificationPersistenceAdapter implements SaveNotificationPort, LoadStudentNotificationPort {

    private final NotificationRepository notificationRepository;
    private final NotificationUserRepository notificationUserRepository;
    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;

    @Override
    public LoadedStudentNotificationPage loadPage(Integer studentUserId, int page, int size, boolean unreadOnly) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Page<NotificationUserEntity> results = notificationUserRepository.findPageForUser(
                studentUserId,
                unreadOnly,
                PageRequest.of(safePage, safeSize)
        );
        return new LoadedStudentNotificationPage(
                results.getContent().stream().map(this::toLoadedNotification).toList(),
                results.getNumber(),
                results.getSize(),
                results.getTotalElements(),
                results.getTotalPages(),
                notificationUserRepository.countUnreadByUserId(studentUserId)
        );
    }

    @Override
    public boolean existsForUserAndSurvey(Integer studentUserId, String type, Integer surveyId) {
        if (studentUserId == null || type == null || surveyId == null) {
            return false;
        }
        return notificationUserRepository.existsByUser_IdAndNotification_TypeAndNotification_Survey_Id(studentUserId, type, surveyId);
    }

    @Override
    public boolean markAsRead(Integer notificationUserId, Integer studentUserId, LocalDateTime readAt) {
        NotificationUserEntity notificationUser = notificationUserRepository.findByIdAndUser_Id(notificationUserId, studentUserId)
                .orElse(null);
        if (notificationUser == null) {
            return false;
        }
        if (notificationUser.getReadAt() == null) {
            notificationUser.setReadAt(readAt);
            notificationUserRepository.save(notificationUser);
        }
        return true;
    }

    @Override
    public int markAllAsRead(Integer studentUserId, LocalDateTime readAt) {
        return notificationUserRepository.markAllUnreadAsRead(studentUserId, readAt);
    }

    @Override
    public void create(NotificationCreateCommand command) {
        if (command == null || command.recipientUserIds() == null || command.recipientUserIds().isEmpty()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        NotificationEntity notification = new NotificationEntity(
                null,
                command.content(),
                command.type(),
                command.title(),
                command.surveyId() == null ? null : surveyRepository.findById(command.surveyId()).orElse(null),
                command.actionLabel(),
                command.createdByUserId() == null ? null : userRepository.findById(command.createdByUserId()).orElse(null),
                command.metadata(),
                now
        );
        NotificationEntity savedNotification = notificationRepository.save(notification);

        List<Integer> recipientUserIds = new LinkedHashSet<>(command.recipientUserIds()).stream()
                .filter(Objects::nonNull)
                .toList();
        Map<Integer, UserEntity> usersById = userRepository.findAllById(recipientUserIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        List<NotificationUserEntity> recipients = recipientUserIds.stream()
                .map(usersById::get)
                .filter(Objects::nonNull)
                .map(user -> toRecipient(savedNotification, user, now))
                .toList();
        notificationUserRepository.saveAll(recipients);
    }

    private NotificationUserEntity toRecipient(NotificationEntity notification, UserEntity user, LocalDateTime deliveredAt) {
        return new NotificationUserEntity(
                null,
                notification,
                user,
                deliveredAt,
                null
        );
    }

    private LoadedStudentNotification toLoadedNotification(NotificationUserEntity entity) {
        NotificationEntity notification = entity.getNotification();
        return new LoadedStudentNotification(
                entity.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getSurvey() == null ? null : notification.getSurvey().getId(),
                notification.getSurvey() == null ? null : notification.getSurvey().getTitle(),
                notification.getActionLabel(),
                notification.getCreatedAt(),
                entity.getReadAt()
        );
    }
}
