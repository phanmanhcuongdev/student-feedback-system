import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";
import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from "react";
import { useNavigate } from "react-router-dom";
import { getUnreadNotificationCount, markNotificationRead } from "../../api/notificationApi";
import { useAuth } from "../auth/useAuth";
import type { RealtimeNotification } from "../../types/notification";
import { getNotificationTarget } from "./notificationRouting";
import { NotificationContext, type NotificationContextValue } from "./notification-context";

const TOAST_VISIBLE_MS = 6000;

function resolveWebSocketUrl() {
    const apiBaseUrl = import.meta.env.VITE_API_BASE_URL as string | undefined;
    if (!apiBaseUrl) {
        return `${window.location.origin}/ws-notifications`;
    }

    const url = new URL(apiBaseUrl, window.location.origin);
    url.pathname = url.pathname.replace(/\/api\/?$/, "/ws-notifications");
    return url.toString();
}

function parseRealtimeNotification(body: string): RealtimeNotification | null {
    try {
        const parsed = JSON.parse(body) as Partial<RealtimeNotification>;
        if (!parsed.type || !parsed.title || !parsed.message) {
            return null;
        }

        return {
            id: parsed.id ?? null,
            type: parsed.type,
            title: parsed.title,
            message: parsed.message,
            surveyId: parsed.surveyId ?? null,
            actionLabel: parsed.actionLabel ?? null,
            eventAt: parsed.eventAt ?? new Date().toISOString(),
        };
    } catch {
        return null;
    }
}

export function NotificationProvider({ children }: { children: ReactNode }) {
    const { session } = useAuth();
    const navigate = useNavigate();
    const clientRef = useRef<Client | null>(null);
    const toastTimerRef = useRef<number | null>(null);
    const [unreadCount, setUnreadCount] = useState(0);
    const [toast, setToast] = useState<RealtimeNotification | null>(null);
    const [latestRealtimeNotification, setLatestRealtimeNotification] = useState<RealtimeNotification | null>(null);

    const refreshUnreadCount = useCallback(async () => {
        if (session?.role !== "STUDENT") {
            setUnreadCount(0);
            return;
        }
        const response = await getUnreadNotificationCount();
        setUnreadCount(response.unreadCount);
    }, [session?.role]);

    const decrementUnreadCount = useCallback(() => {
        setUnreadCount((current) => Math.max(current - 1, 0));
    }, []);

    const clearUnreadCount = useCallback(() => {
        setUnreadCount(0);
    }, []);

    useEffect(() => {
        let ignored = false;

        if (session?.role !== "STUDENT") {
            queueMicrotask(() => {
                if (!ignored) {
                    setUnreadCount(0);
                }
            });
            return () => {
                ignored = true;
            };
        }

        void getUnreadNotificationCount()
            .then((response) => {
                if (!ignored) {
                    setUnreadCount(response.unreadCount);
                }
            })
            .catch(() => {
                if (!ignored) {
                    setUnreadCount(0);
                }
            });

        return () => {
            ignored = true;
        };
    }, [session?.role]);

    useEffect(() => {
        if (toastTimerRef.current !== null) {
            window.clearTimeout(toastTimerRef.current);
            toastTimerRef.current = null;
        }

        if (!toast) {
            return;
        }

        toastTimerRef.current = window.setTimeout(() => {
            setToast(null);
            toastTimerRef.current = null;
        }, TOAST_VISIBLE_MS);

        return () => {
            if (toastTimerRef.current !== null) {
                window.clearTimeout(toastTimerRef.current);
                toastTimerRef.current = null;
            }
        };
    }, [toast]);

    useEffect(() => {
        clientRef.current?.deactivate();
        clientRef.current = null;

        if (session?.role !== "STUDENT" || !session.accessToken) {
            return;
        }

        const client = new Client({
            connectHeaders: {
                Authorization: `Bearer ${session.accessToken}`,
            },
            reconnectDelay: 5000,
            webSocketFactory: () => new SockJS(resolveWebSocketUrl()),
            onConnect: () => {
                client.subscribe("/user/topic/notifications", (message) => {
                    const notification = parseRealtimeNotification(message.body);
                    if (!notification) {
                        return;
                    }
                    setLatestRealtimeNotification(notification);
                    setToast(notification);
                    setUnreadCount((current) => current + 1);
                });
            },
        });

        clientRef.current = client;
        client.activate();

        return () => {
            void client.deactivate();
            if (clientRef.current === client) {
                clientRef.current = null;
            }
        };
    }, [session?.accessToken, session?.role]);

    const value = useMemo<NotificationContextValue>(() => ({
        unreadCount,
        latestRealtimeNotification,
        refreshUnreadCount,
        decrementUnreadCount,
        clearUnreadCount,
    }), [
        unreadCount,
        latestRealtimeNotification,
        refreshUnreadCount,
        decrementUnreadCount,
        clearUnreadCount,
    ]);

    async function handleToastClick() {
        if (!toast) {
            return;
        }
        const notification = toast;
        setToast(null);
        if (notification.id !== null) {
            try {
                const response = await markNotificationRead(notification.id);
                if (response.success) {
                    decrementUnreadCount();
                } else {
                    await refreshUnreadCount();
                }
            } catch {
                await refreshUnreadCount();
            }
        }
        navigate(getNotificationTarget(notification));
    }

    return (
        <NotificationContext.Provider value={value}>
            {children}
            {toast ? (
                <button
                    type="button"
                    onClick={() => void handleToastClick()}
                    className="fixed right-4 top-20 z-[80] w-[min(360px,calc(100vw-2rem))] rounded-2xl border border-slate-200 bg-white p-4 text-left shadow-[0_20px_52px_rgba(15,23,42,0.18)] transition hover:-translate-y-0.5 hover:border-slate-300 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 focus-visible:ring-offset-2"
                >
                    <div className="flex items-start gap-3">
                        <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-slate-900 text-white">
                            <span className="material-symbols-outlined text-[20px]">notifications</span>
                        </div>
                        <div className="min-w-0">
                            <p className="line-clamp-1 text-sm font-extrabold text-slate-950">{toast.title}</p>
                            <p className="mt-1 line-clamp-2 text-sm leading-5 text-slate-600">{toast.message}</p>
                        </div>
                    </div>
                </button>
            ) : null}
        </NotificationContext.Provider>
    );
}
