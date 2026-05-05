import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getApiErrorMessage } from "../../../api/apiError";
import { getStudentNotifications, markAllNotificationsRead, markNotificationRead } from "../../../api/notificationApi";
import PaginationControls from "../../../components/data-view/PaginationControls";
import type { StudentNotification } from "../../../types/notification";
import { getNotificationTarget } from "../notificationRouting";
import { useNotifications } from "../useNotifications";

function formatDate(date: string, language: string) {
    return new Intl.DateTimeFormat(language === "vi" ? "vi-VN" : "en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    }).format(new Date(date));
}

function getAccent(type: StudentNotification["type"]) {
    switch (type) {
        case "SURVEY_PUBLISHED":
            return "border-blue-200 bg-blue-50 text-blue-700";
        case "SURVEY_DEADLINE_REMINDER":
            return "border-amber-200 bg-amber-50 text-amber-700";
        case "ONBOARDING_APPROVED":
            return "border-emerald-200 bg-emerald-50 text-emerald-700";
        case "ONBOARDING_REJECTED":
            return "border-red-200 bg-red-50 text-red-700";
        default:
            return "border-slate-200 bg-slate-50 text-slate-700";
    }
}

function getTypeLabel(notification: StudentNotification, t: (key: string, options?: Record<string, unknown>) => string) {
    switch (notification.type) {
        case "SURVEY_PUBLISHED":
            return t("notifications:notifications.types.surveyPublished");
        case "SURVEY_DEADLINE_REMINDER":
            return t("notifications:notifications.types.surveyDeadlineReminder");
        case "ONBOARDING_APPROVED":
            return t("notifications:notifications.types.onboardingApproved");
        case "ONBOARDING_REJECTED":
            return t("notifications:notifications.types.onboardingRejected");
        default:
            return notification.type.replace(/_/g, " ");
    }
}

function getLocalizedTitle(notification: StudentNotification, t: (key: string, options?: Record<string, unknown>) => string) {
    switch (notification.type) {
        case "SURVEY_PUBLISHED":
            return t("notifications:notifications.items.surveyPublished.title");
        case "SURVEY_DEADLINE_REMINDER":
            return t("notifications:notifications.items.surveyDeadlineReminder.title");
        case "ONBOARDING_APPROVED":
            return t("notifications:notifications.items.onboardingApproved.title");
        case "ONBOARDING_REJECTED":
            return t("notifications:notifications.items.onboardingRejected.title");
        default:
            return notification.title;
    }
}

function getLocalizedMessage(notification: StudentNotification, t: (key: string, options?: Record<string, unknown>) => string) {
    const surveyTitle = notification.surveyTitle ?? "";

    switch (notification.type) {
        case "SURVEY_PUBLISHED":
            return t("notifications:notifications.items.surveyPublished.message", { surveyTitle });
        case "SURVEY_DEADLINE_REMINDER":
            return t("notifications:notifications.items.surveyDeadlineReminder.message", { surveyTitle });
        case "ONBOARDING_APPROVED":
            return t("notifications:notifications.items.onboardingApproved.message");
        case "ONBOARDING_REJECTED":
            return t("notifications:notifications.items.onboardingRejected.message");
        default:
            return notification.message;
    }
}

function getLocalizedActionLabel(notification: StudentNotification, t: (key: string, options?: Record<string, unknown>) => string) {
    switch (notification.type) {
        case "SURVEY_PUBLISHED":
            return t("notifications:notifications.items.surveyPublished.action");
        case "SURVEY_DEADLINE_REMINDER":
            return t("notifications:notifications.items.surveyDeadlineReminder.action");
        case "ONBOARDING_APPROVED":
            return t("notifications:notifications.items.onboardingApproved.action");
        case "ONBOARDING_REJECTED":
            return t("notifications:notifications.items.onboardingRejected.action");
        default:
            return notification.actionLabel ?? t("notifications:notifications.buttons.open");
    }
}

export default function NotificationsPage() {
    const { i18n, t } = useTranslation(["notifications"]);
    const navigate = useNavigate();
    const {
        unreadCount,
        latestRealtimeNotification,
        refreshUnreadCount,
        decrementUnreadCount,
        clearUnreadCount,
    } = useNotifications();
    const [notifications, setNotifications] = useState<StudentNotification[]>([]);
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [unreadOnly, setUnreadOnly] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    async function fetchNotifications() {
        try {
            setLoading(true);
            setError("");
            const response = await getStudentNotifications({ page, size: 6, unreadOnly });
            if (response.items.length === 0 && response.totalPages > 0 && page >= response.totalPages) {
                setPage(response.totalPages - 1);
                return;
            }
            setNotifications(response.items);
            setTotalPages(response.totalPages);
            await refreshUnreadCount();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("notifications:notifications.errors.load")));
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        void fetchNotifications();
    }, [page, unreadOnly]);

    useEffect(() => {
        if (latestRealtimeNotification) {
            void fetchNotifications();
        }
    }, [latestRealtimeNotification]);

    async function handleMarkRead(notificationId: number) {
        try {
            const response = await markNotificationRead(notificationId);
            if (response.success) {
                decrementUnreadCount();
                setNotifications((current) => current.map((notification) => (
                    notification.id === notificationId
                        ? { ...notification, read: true, readAt: new Date().toISOString() }
                        : notification
                )));
            } else {
                await refreshUnreadCount();
            }
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("notifications:notifications.errors.markRead")));
        }
    }

    async function handleOpenNotification(notification: StudentNotification) {
        if (!notification.read) {
            await handleMarkRead(notification.id);
        }
        navigate(getNotificationTarget(notification));
    }

    async function handleMarkAllRead() {
        try {
            await markAllNotificationsRead();
            clearUnreadCount();
            await fetchNotifications();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("notifications:notifications.errors.markAllRead")));
        }
    }

    return (
        <main className="bg-[linear-gradient(180deg,#f4f8ff_0%,#eef3f8_44%,#f7fafc_100%)]">
            <div className="mx-auto max-w-screen-lg px-6 py-10">
                    <div className="mb-10 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
                        <div className="max-w-2xl">
                            <span className="mb-3 inline-flex rounded-full border border-blue-200 bg-blue-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-blue-700">
                                {t("notifications:notifications.header.eyebrow", { count: unreadCount })}
                            </span>
                            <h1 className="text-4xl font-extrabold tracking-tight text-slate-950">
                                {t("notifications:notifications.header.title")}
                            </h1>
                            <p className="mt-4 text-base leading-7 text-slate-500">
                                {t("notifications:notifications.header.description")}
                            </p>
                        </div>
                        <div className="flex flex-wrap gap-3">
                            <button
                                type="button"
                                onClick={() => {
                                    setUnreadOnly((current) => !current);
                                    setPage(0);
                                }}
                                className={`rounded-2xl border px-4 py-3 text-sm font-bold transition ${unreadOnly ? "border-blue-300 bg-blue-50 text-blue-700" : "border-slate-200 bg-white text-slate-700 hover:border-slate-300"}`}
                            >
                                {unreadOnly ? t("notifications:notifications.buttons.showingUnread") : t("notifications:notifications.buttons.showUnread")}
                            </button>
                            <button
                                type="button"
                                onClick={() => void handleMarkAllRead()}
                                disabled={unreadCount === 0}
                                className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-bold text-slate-700 transition hover:border-slate-300 disabled:cursor-not-allowed disabled:opacity-50"
                            >
                                {t("notifications:notifications.buttons.markAllRead")}
                            </button>
                        </div>
                    </div>

                    {error ? (
                        <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                            {error}
                        </div>
                    ) : null}

                    {loading ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-10 text-center text-sm font-medium text-slate-500 shadow-sm">
                            {t("notifications:notifications.loading")}
                        </div>
                    ) : notifications.length === 0 ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-12 text-center shadow-sm">
                            <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-slate-100 text-slate-500">
                                <span className="material-symbols-outlined text-[30px]">notifications_none</span>
                            </div>
                            <h2 className="text-2xl font-bold text-slate-900">{t("notifications:notifications.empty.title")}</h2>
                            <p className="mt-3 text-sm text-slate-500">
                                {t("notifications:notifications.empty.description")}
                            </p>
                        </div>
                    ) : (
                        <div className="space-y-5">
                            {notifications.map((notification, index) => (
                                <article
                                    key={`${notification.id}-${index}`}
                                    className={`rounded-[28px] border p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)] ${notification.read ? "border-slate-200 bg-white" : "border-blue-200 bg-blue-50/45"}`}
                                >
                                    <div className="mb-4 flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                                        <div>
                                            <span className={`inline-flex rounded-full border px-3 py-1 text-[11px] font-bold uppercase tracking-[0.22em] ${getAccent(notification.type)}`}>
                                                {getTypeLabel(notification, t)}
                                            </span>
                                            {!notification.read ? (
                                                <span className="ml-2 inline-flex rounded-full border border-blue-200 bg-white px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-blue-700">
                                                    {t("notifications:notifications.badges.unread")}
                                                </span>
                                            ) : null}
                                            <h2 className="mt-3 text-2xl font-bold text-slate-950">
                                                {getLocalizedTitle(notification, t)}
                                            </h2>
                                            <p className="mt-3 text-sm leading-6 text-slate-500">
                                                {getLocalizedMessage(notification, t)}
                                            </p>
                                            {notification.surveyTitle ? (
                                                <p className="mt-3 text-sm font-semibold text-slate-700">
                                                    {notification.surveyTitle}
                                                </p>
                                            ) : null}
                                        </div>
                                        <div className="rounded-2xl bg-slate-100 px-4 py-3 text-sm font-semibold text-slate-600">
                                            {formatDate(notification.eventAt, i18n.language)}
                                        </div>
                                    </div>

                                    <div className="pt-2">
                                        <div className="flex flex-wrap gap-3">
                                            <button
                                                type="button"
                                                onClick={() => void handleOpenNotification(notification)}
                                                className="inline-flex items-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-bold text-slate-700 transition hover:border-slate-300 hover:bg-slate-100"
                                            >
                                                <span>{getLocalizedActionLabel(notification, t)}</span>
                                                <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
                                            </button>
                                            {!notification.read ? (
                                                <button
                                                    type="button"
                                                    onClick={() => void handleMarkRead(notification.id)}
                                                    className="inline-flex items-center gap-2 rounded-2xl border border-blue-200 bg-white px-4 py-3 text-sm font-bold text-blue-700 transition hover:border-blue-300"
                                                >
                                                    {t("notifications:notifications.buttons.markRead")}
                                                </button>
                                            ) : null}
                                        </div>
                                    </div>
                                </article>
                            ))}
                            <PaginationControls
                                page={page + 1}
                                pageCount={Math.max(totalPages, 1)}
                                onPageChange={(nextPage) => setPage(nextPage - 1)}
                            />
                        </div>
                    )}
            </div>
        </main>
    );
}
