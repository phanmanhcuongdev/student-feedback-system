import { createContext } from "react";
import type { RealtimeNotification } from "../../types/notification";

export type NotificationContextValue = {
    unreadCount: number;
    latestRealtimeNotification: RealtimeNotification | null;
    refreshUnreadCount: () => Promise<void>;
    decrementUnreadCount: () => void;
    clearUnreadCount: () => void;
};

export const NotificationContext = createContext<NotificationContextValue | null>(null);
