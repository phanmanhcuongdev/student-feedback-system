import type { RealtimeNotification, StudentNotification } from "../../types/notification";

type RoutableNotification = Pick<StudentNotification, "type" | "surveyId"> | Pick<RealtimeNotification, "type" | "surveyId">;

export function getNotificationTarget(notification: RoutableNotification) {
    if (notification.type === "SURVEY_PUBLISHED" || notification.type === "SURVEY_DEADLINE_REMINDER") {
        return notification.surveyId ? `/surveys/${notification.surveyId}` : "/surveys";
    }
    if (notification.type === "ONBOARDING_REJECTED") {
        return "/upload-documents";
    }
    if (notification.type === "ONBOARDING_APPROVED") {
        return "/dashboard/student";
    }

    return "/notifications";
}
