import axios from "./axios";
import type { StudentNotificationPage } from "../types/notification";

export async function getStudentNotifications(params: {
    page?: number;
    size?: number;
}): Promise<StudentNotificationPage> {
    const response = await axios.get<StudentNotificationPage>("/v1/notifications", { params });
    return response.data;
}
