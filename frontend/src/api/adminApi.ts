import axios from "./axios";
import type {
    AdminActionResponse,
    ApproveStudentRequest,
    DepartmentOption,
    ManagedUserDetail,
    ManagedUserPage,
    PendingStudent,
    RejectStudentRequest,
} from "../types/admin";
import type { CreateSurveyData, CreateSurveyResponse, ManagedSurveyDetail, ManagedSurveyPage } from "../types/survey";

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

export async function getStudentDocument(studentId: number, documentType: "student-card" | "national-id"): Promise<Blob> {
    const response = await axios.get<Blob>(`/admin/students/${studentId}/documents/${documentType}`, {
        responseType: "blob",
    });
    return response.data;
}

export async function createSurvey(data: CreateSurveyData): Promise<CreateSurveyResponse> {
    const response = await axios.post<CreateSurveyResponse>("/admin/surveys", data);
    return response.data;
}

export async function getManagedSurveys(params: {
    keyword?: string;
    lifecycleState?: string;
    runtimeStatus?: string;
    hidden?: boolean;
    recipientScope?: string;
    startDateFrom?: string;
    endDateTo?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
}): Promise<ManagedSurveyPage> {
    const response = await axios.get<ManagedSurveyPage>("/admin/surveys", { params });
    return response.data;
}

export async function getSurveyManagementDepartments(): Promise<DepartmentOption[]> {
    const response = await axios.get<DepartmentOption[]>("/admin/surveys/departments");
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

export async function getUsers(params: {
    role?: string;
    keyword?: string;
    active?: boolean;
    studentStatus?: string;
    departmentId?: number;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
}): Promise<ManagedUserPage> {
    const response = await axios.get<ManagedUserPage>("/admin/users", { params });
    return response.data;
}

export async function getUserManagementDepartments(): Promise<DepartmentOption[]> {
    const response = await axios.get<DepartmentOption[]>("/admin/users/departments");
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
