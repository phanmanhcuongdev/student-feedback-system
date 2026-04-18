import axios from "./axios";
import type { CreateFeedbackResponse, RespondToFeedbackResponse, StaffFeedbackPage, StudentFeedbackPage } from "../types/feedback";

export async function getStudentFeedback(params: {
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
}): Promise<StudentFeedbackPage> {
    const response = await axios.get<StudentFeedbackPage>("/v1/feedback", { params });
    return response.data;
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
    return response.data;
}

export async function respondToFeedback(feedbackId: number, content: string): Promise<RespondToFeedbackResponse> {
    const response = await axios.post<RespondToFeedbackResponse>(`/v1/feedback/${feedbackId}/responses`, { content });
    return response.data;
}
