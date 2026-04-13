import axios from "./axios";
import type { StudentNotification } from "../types/notification";

export async function getStudentNotifications(): Promise<StudentNotification[]> {
    const response = await axios.get<StudentNotification[]>("/v1/notifications");
    return response.data;
}
