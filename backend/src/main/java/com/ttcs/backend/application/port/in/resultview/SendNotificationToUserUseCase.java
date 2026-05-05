package com.ttcs.backend.application.port.in.resultview;

public interface SendNotificationToUserUseCase {
    void sendNotificationToUser(Integer userId, String message, String type);

    void sendSurveyNotificationToUser(
            Integer userId,
            String message,
            String type,
            Integer surveyId,
            String title,
            String actionLabel,
            String metadata
    );
}
