import axios from "./axios";
import type {
    AdminActionResponse,
    ApproveStudentRequest,
    ManagedUserDetail,
    ManagedUserSummary,
    PendingStudent,
    RejectStudentRequest,
} from "../types/admin";
import type { CreateSurveyData, CreateSurveyResponse, ManagedSurveyDetail, ManagedSurveySummary } from "../types/survey";

export async function getPendingStudents(): Promise<PendingStudent[]> {
    const response = await axios.get<PendingStudent[]>("/admin/students/pending");
    return response.data;
}

export async function approveStudent(
    studentId: number,
    payload: ApproveStudentRequest
): Promise<AdminActionResponse> {
    const response = await axios.post<AdminActionResponse>(`/admin/students/${studentId}/approve`, payload);
    return response.data;
}

export async function rejectStudent(
    studentId: number,
    payload: RejectStudentRequest
): Promise<AdminActionResponse> {
    const response = await axios.post<AdminActionResponse>(`/admin/students/${studentId}/reject`, payload);
    return response.data;
}

export async function createSurvey(data: CreateSurveyData): Promise<CreateSurveyResponse> {
    const response = await axios.post<CreateSurveyResponse>("/admin/surveys", data);
    return response.data;
}

export async function getManagedSurveys(): Promise<ManagedSurveySummary[]> {
    const response = await axios.get<ManagedSurveySummary[]>("/admin/surveys");
    return response.data;
}

export async function getManagedSurvey(surveyId: number): Promise<ManagedSurveyDetail> {
    const response = await axios.get<ManagedSurveyDetail>(`/admin/surveys/${surveyId}`);
    return response.data;
}

export async function updateSurvey(surveyId: number, data: CreateSurveyData): Promise<AdminActionResponse> {
    const response = await axios.put<AdminActionResponse>(`/admin/surveys/${surveyId}`, data);
    return response.data;
}

export async function closeSurvey(surveyId: number): Promise<AdminActionResponse> {
    const response = await axios.post<AdminActionResponse>(`/admin/surveys/${surveyId}/close`);
    return response.data;
}

export async function publishSurvey(surveyId: number): Promise<AdminActionResponse> {
    const response = await axios.post<AdminActionResponse>(`/admin/surveys/${surveyId}/publish`);
    return response.data;
}

export async function archiveSurvey(surveyId: number): Promise<AdminActionResponse> {
    const response = await axios.post<AdminActionResponse>(`/admin/surveys/${surveyId}/archive`);
    return response.data;
}

export async function setSurveyVisibility(surveyId: number, hidden: boolean): Promise<AdminActionResponse> {
    const response = await axios.post<AdminActionResponse>(`/admin/surveys/${surveyId}/visibility`, { hidden });
    return response.data;
}

export async function getUsers(): Promise<ManagedUserSummary[]> {
    const response = await axios.get<ManagedUserSummary[]>("/admin/users");
    return response.data;
}

export async function getUserDetail(userId: number): Promise<ManagedUserDetail> {
    const response = await axios.get<ManagedUserDetail>(`/admin/users/${userId}`);
    return response.data;
}

export async function updateUser(userId: number, payload: {
    email: string;
    name: string;
    departmentId?: number | null;
    studentCode?: string | null;
    teacherCode?: string | null;
}): Promise<AdminActionResponse> {
    const response = await axios.put<AdminActionResponse>(`/admin/users/${userId}`, payload);
    return response.data;
}

export async function deactivateUser(userId: number): Promise<AdminActionResponse> {
    const response = await axios.post<AdminActionResponse>(`/admin/users/${userId}/deactivate`);
    return response.data;
}

export async function activateUser(userId: number): Promise<AdminActionResponse> {
    const response = await axios.post<AdminActionResponse>(`/admin/users/${userId}/activate`);
    return response.data;
}
