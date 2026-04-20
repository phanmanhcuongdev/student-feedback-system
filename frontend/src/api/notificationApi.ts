import axios from "./axios";
import type { NotificationActionResponse, StudentNotificationPage } from "../types/notification";

export async function getStudentNotifications(params: {
    page?: number;
    size?: number;
    unreadOnly?: boolean;
}): Promise<StudentNotificationPage> {
    const response = await axios.get<StudentNotificationPage>("/v1/notifications", { params });
    return response.data;
}

export async function markNotificationRead(notificationId: number): Promise<NotificationActionResponse> {
    const response = await axios.post<NotificationActionResponse>(`/v1/notifications/${notificationId}/read`);
    return response.data;
}

export async function markAllNotificationsRead(): Promise<NotificationActionResponse> {
    const response = await axios.post<NotificationActionResponse>("/v1/notifications/read-all");
    return response.data;
}
