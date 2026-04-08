import axios from "./axios";
import type { AdminActionResponse, PendingStudent } from "../types/admin";

export async function getPendingStudents(): Promise<PendingStudent[]> {
    const response = await axios.get<PendingStudent[]>("/admin/students/pending");
    return response.data;
}

export async function approveStudent(studentId: number): Promise<AdminActionResponse> {
    const response = await axios.post<AdminActionResponse>(`/admin/students/${studentId}/approve`);
    return response.data;
}

export async function rejectStudent(studentId: number): Promise<AdminActionResponse> {
    const response = await axios.post<AdminActionResponse>(`/admin/students/${studentId}/reject`);
    return response.data;
}
