import axios from "./axios";
import type { CreateFeedbackResponse, RespondToFeedbackResponse, StaffFeedback, StudentFeedback } from "../types/feedback";

export async function getStudentFeedback(): Promise<StudentFeedback[]> {
    const response = await axios.get<StudentFeedback[]>("/v1/feedback");
    return response.data;
}

export async function createFeedback(title: string, content: string): Promise<CreateFeedbackResponse> {
    const response = await axios.post<CreateFeedbackResponse>("/v1/feedback", { title, content });
    return response.data;
}

export async function getAllFeedback(): Promise<StaffFeedback[]> {
    const response = await axios.get<StaffFeedback[]>("/v1/feedback/staff");
    return response.data;
}

export async function respondToFeedback(feedbackId: number, content: string): Promise<RespondToFeedbackResponse> {
    const response = await axios.post<RespondToFeedbackResponse>(`/v1/feedback/${feedbackId}/responses`, { content });
    return response.data;
}
