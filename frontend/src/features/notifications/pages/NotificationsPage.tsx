import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../../../api/apiError";
import { getStudentNotifications } from "../../../api/notificationApi";
import MainFooter from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
import type { StudentNotification } from "../../../types/notification";

function formatDate(date: string) {
    return new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    }).format(new Date(date));
}

function getAccent(type: StudentNotification["type"]) {
    switch (type) {
        case "NEW_SURVEY":
            return "border-blue-200 bg-blue-50 text-blue-700";
        case "OPENING_SOON":
            return "border-sky-200 bg-sky-50 text-sky-700";
        case "CLOSING_SOON":
            return "border-amber-200 bg-amber-50 text-amber-700";
        case "CLOSED":
            return "border-slate-200 bg-slate-100 text-slate-700";
        default:
            return "border-slate-200 bg-slate-50 text-slate-700";
    }
}

function getTarget(notification: StudentNotification) {
    if (notification.type === "NEW_SURVEY" || notification.type === "CLOSING_SOON") {
        return notification.surveyId ? `/surveys/${notification.surveyId}` : "/surveys";
    }

    return "/surveys";
}

export default function NotificationsPage() {
    const [notifications, setNotifications] = useState<StudentNotification[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function fetchNotifications() {
            try {
                setLoading(true);
                setError("");
                setNotifications(await getStudentNotifications());
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, "Unable to load notifications."));
            } finally {
                setLoading(false);
            }
        }

        fetchNotifications();
    }, []);

    return (
        <>
            <MainHeader />
            <main className="min-h-screen bg-[linear-gradient(180deg,#f4f8ff_0%,#eef3f8_44%,#f7fafc_100%)]">
                <div className="mx-auto max-w-screen-lg px-6 py-10">
                    <div className="mb-10 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
                        <div className="max-w-2xl">
                            <span className="mb-3 inline-flex rounded-full border border-blue-200 bg-blue-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-blue-700">
                                Student Inbox
                            </span>
                            <h1 className="text-4xl font-extrabold tracking-tight text-slate-950">
                                Notifications
                            </h1>
                            <p className="mt-4 text-base leading-7 text-slate-500">
                                Review survey updates, upcoming openings, and approaching deadlines.
                            </p>
                        </div>
                    </div>

                    {error ? (
                        <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                            {error}
                        </div>
                    ) : null}

                    {loading ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-10 text-center text-sm font-medium text-slate-500 shadow-sm">
                            Loading notifications...
                        </div>
                    ) : notifications.length === 0 ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-12 text-center shadow-sm">
                            <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-slate-100 text-slate-500">
                                <span className="material-symbols-outlined text-[30px]">notifications_none</span>
                            </div>
                            <h2 className="text-2xl font-bold text-slate-900">No notifications</h2>
                            <p className="mt-3 text-sm text-slate-500">
                                Survey-related updates will appear here when they become relevant.
                            </p>
                        </div>
                    ) : (
                        <div className="space-y-5">
                            {notifications.map((notification, index) => (
                                <article
                                    key={`${notification.type}-${notification.surveyId ?? "general"}-${index}`}
                                    className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]"
                                >
                                    <div className="mb-4 flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                                        <div>
                                            <span className={`inline-flex rounded-full border px-3 py-1 text-[11px] font-bold uppercase tracking-[0.22em] ${getAccent(notification.type)}`}>
                                                {notification.type.replace(/_/g, " ")}
                                            </span>
                                            <h2 className="mt-3 text-2xl font-bold text-slate-950">
                                                {notification.title}
                                            </h2>
                                            <p className="mt-3 text-sm leading-6 text-slate-500">
                                                {notification.message}
                                            </p>
                                            {notification.surveyTitle ? (
                                                <p className="mt-3 text-sm font-semibold text-slate-700">
                                                    {notification.surveyTitle}
                                                </p>
                                            ) : null}
                                        </div>
                                        <div className="rounded-2xl bg-slate-100 px-4 py-3 text-sm font-semibold text-slate-600">
                                            {formatDate(notification.eventAt)}
                                        </div>
                                    </div>

                                    <div className="pt-2">
                                        <Link
                                            to={getTarget(notification)}
                                            className="inline-flex items-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-bold text-slate-700 transition hover:border-slate-300 hover:bg-slate-100"
                                        >
                                            <span>{notification.actionLabel ?? "Open"}</span>
                                            <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
                                        </Link>
                                    </div>
                                </article>
                            ))}
                        </div>
                    )}
                </div>
            </main>
            <MainFooter />
        </>
    );
}
