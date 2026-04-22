import axios from "./axios";
import type {
    CreateFeedbackResponse,
    RespondToFeedbackResponse,
    StaffFeedback,
    StaffFeedbackPage,
    StudentFeedback,
    StudentFeedbackPage,
    TranslationContent,
} from "../types/feedback";

type FeedbackQueryParams = {
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
};

type FeedbackResponsePayload = {
    id: number;
    title: string;
    displayContent?: string | null;
    originalContent?: string | null;
    contentTranslated?: string | null;
    isAutoTranslated?: boolean;
    sourceLang?: string | null;
    content?: string | null;
    createdAt: string;
    responses: StudentFeedback["responses"];
};

function toTranslationContent(payload: FeedbackResponsePayload): TranslationContent {
    return {
        displayContent: payload.displayContent ?? payload.content ?? "",
        originalContent: payload.originalContent ?? null,
        contentTranslated: payload.contentTranslated ?? null,
        isAutoTranslated: payload.isAutoTranslated ?? false,
        sourceLang: payload.sourceLang ?? "",
    };
}

function mapStudentFeedback(payload: FeedbackResponsePayload): StudentFeedback {
    return {
        id: payload.id,
        title: payload.title,
        ...toTranslationContent(payload),
        createdAt: payload.createdAt,
        responses: payload.responses,
    };
}

export async function fetchFeedbacks(params: FeedbackQueryParams = {}): Promise<StudentFeedbackPage> {
    const response = await axios.get<StudentFeedbackPage>("/v1/feedback", { params });
    return {
        ...response.data,
        items: response.data.items.map((item) => mapStudentFeedback(item as FeedbackResponsePayload)),
    };
}

export async function getStudentFeedback(params: FeedbackQueryParams): Promise<StudentFeedbackPage> {
    return fetchFeedbacks(params);
}

export async function createFeedback(title: string, content: string): Promise<CreateFeedbackResponse> {
    const response = await axios.post<CreateFeedbackResponse>("/v1/feedback", { title, content });
    return response.data;
}

export async function getAllFeedback(params: {
    keyword?: string;
    status?: string;
    createdDate?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
}): Promise<StaffFeedbackPage> {
    const response = await axios.get<StaffFeedbackPage>("/v1/feedback/staff", { params });
    return {
        ...response.data,
        items: response.data.items.map((item) => ({
            ...item,
            ...toTranslationContent(item as FeedbackResponsePayload),
        } as StaffFeedback)),
    };
}

export async function respondToFeedback(feedbackId: number, content: string): Promise<RespondToFeedbackResponse> {
    const response = await axios.post<RespondToFeedbackResponse>(`/v1/feedback/${feedbackId}/responses`, { content });
    return response.data;
}
