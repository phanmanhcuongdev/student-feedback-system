export type StudentNotification = {
    type: "NEW_SURVEY" | "OPENING_SOON" | "CLOSING_SOON" | "CLOSED" | string;
    title: string;
    message: string;
    surveyId: number | null;
    surveyTitle: string | null;
    actionLabel: string | null;
    eventAt: string;
};

export type StudentNotificationPage = {
    items: StudentNotification[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
};
