export type FeedbackResponse = {
    id: number;
    responderEmail: string;
    responderRole: string;
    content: string;
    createdAt: string;
};

export type StudentFeedback = {
    id: number;
    title: string;
    content: string;
    createdAt: string;
    responses: FeedbackResponse[];
};

export type StaffFeedback = {
    id: number;
    studentId: number;
    studentName: string;
    studentEmail: string | null;
    title: string;
    content: string;
    createdAt: string;
    responses: FeedbackResponse[];
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
