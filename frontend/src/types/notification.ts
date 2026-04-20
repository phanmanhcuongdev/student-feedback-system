export type StudentNotification = {
    id: number;
    type: "SURVEY_PUBLISHED" | "SURVEY_DEADLINE_REMINDER" | "ONBOARDING_APPROVED" | "ONBOARDING_REJECTED" | string;
    title: string;
    message: string;
    surveyId: number | null;
    surveyTitle: string | null;
    actionLabel: string | null;
    eventAt: string;
    read: boolean;
    readAt: string | null;
};

export type StudentNotificationPage = {
    items: StudentNotification[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    unreadCount: number;
};

export type NotificationActionResponse = {
    success: boolean;
    code: string;
    message: string;
};
