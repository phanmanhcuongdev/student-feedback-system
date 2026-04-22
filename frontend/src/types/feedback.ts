export interface TranslationContent {
    displayContent: string;
    originalContent: string | null;
    contentTranslated: string | null;
    isAutoTranslated: boolean;
    sourceLang: string;
}

export type FeedbackResponse = {
    id: number;
    responderEmail: string;
    responderRole: string;
    content: string;
    createdAt: string;
};

export type StudentFeedback = TranslationContent & {
    id: number;
    title: string;
    createdAt: string;
    responses: FeedbackResponse[];
};

export type StaffFeedback = TranslationContent & {
    id: number;
    studentId: number;
    studentName: string;
    studentEmail: string | null;
    title: string;
    createdAt: string;
    responses: FeedbackResponse[];
};

export type StudentFeedbackPage = {
    items: StudentFeedback[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
};

export type StaffFeedbackPage = {
    items: StaffFeedback[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
};

export type CreateFeedbackResponse = {
    success: boolean;
    code: string;
    message: string;
};

export type RespondToFeedbackResponse = {
    success: boolean;
    code: string;
    message: string;
};
