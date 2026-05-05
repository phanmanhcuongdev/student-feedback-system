import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getDefaultAppRoute } from "../../features/auth/defaultRoute";
import { useAuth } from "../../features/auth/useAuth";
import NotificationBell from "../../features/notifications/NotificationBell";
import { getRoleLabelKey, getStatusLabelKey } from "./appNavigation";
import LanguageSwitcher from "./LanguageSwitcher";

export default function MainHeader() {
    const { t } = useTranslation(["common", "layout"]);
    const navigate = useNavigate();
    const { session, logout } = useAuth();
    const isStudent = session?.role === "STUDENT";
    const canViewResults = session?.role === "ADMIN" || session?.role === "LECTURER";
    const canManageFeedback = session?.role === "ADMIN" || session?.role === "LECTURER";
    const dashboardRoute = getDefaultAppRoute(session?.role, session?.studentStatus);

    function handleLogout() {
        logout();
        navigate("/login", { replace: true });
    }

    return (
        <header className="sticky top-0 z-50 border-b border-slate-200/80 bg-white/88 shadow-[0_12px_32px_rgba(15,23,42,0.05)] backdrop-blur-xl">
            <nav className="mx-auto flex w-full max-w-screen-xl items-center justify-between gap-4 px-6 py-4">
                <div className="flex items-center gap-8">
                    <div className="flex items-center gap-3">
                        <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-[linear-gradient(135deg,#0b1c30_0%,#1d78ec_100%)] text-white shadow-[0_16px_36px_rgba(14,66,140,0.24)]">
                            <span className="material-symbols-outlined text-[22px]">insights</span>
                        </div>
                        <div>
                            <span className="block text-base font-extrabold tracking-tight text-slate-950">
                                {t("layout:layout.legacyHeader.name")}
                            </span>
                            <span className="block text-xs font-medium uppercase tracking-[0.22em] text-slate-400">
                                {t("layout:layout.legacyHeader.subtitle")}
                            </span>
                        </div>
                    </div>

                    <div className="hidden gap-6 text-sm font-semibold text-slate-500 md:flex">
                        <Link
                            to={dashboardRoute}
                            className="rounded-full border border-slate-200 bg-slate-50 px-4 py-2 text-slate-700 transition hover:border-slate-300 hover:bg-slate-100"
                        >
                            {t("layout:layout.nav.items.dashboard")}
                        </Link>
                        <Link
                            to="/change-password"
                            className="rounded-full border border-slate-200 bg-slate-50 px-4 py-2 text-slate-700 transition hover:border-slate-300 hover:bg-slate-100"
                        >
                            {t("layout:layout.nav.items.changePassword")}
                        </Link>
                        {isStudent ? (
                            <>
                                <Link
                                    to="/surveys"
                                    className="rounded-full border border-blue-200 bg-blue-50 px-4 py-2 text-blue-700 transition hover:border-blue-300 hover:bg-blue-100"
                                >
                                    {t("layout:layout.nav.items.surveys")}
                                </Link>
                                <Link
                                    to="/notifications"
                                    className="rounded-full border border-emerald-200 bg-emerald-50 px-4 py-2 text-emerald-700 transition hover:border-emerald-300 hover:bg-emerald-100"
                                >
                                    {t("layout:layout.nav.items.notifications")}
                                </Link>
                                <Link
                                    to="/feedback"
                                    className="rounded-full border border-indigo-200 bg-indigo-50 px-4 py-2 text-indigo-700 transition hover:border-indigo-300 hover:bg-indigo-100"
                                >
                                    {t("layout:layout.nav.items.feedback")}
                                </Link>
                            </>
                        ) : null}
                        {canViewResults ? (
                            <>
                                <Link
                                    to="/survey-results"
                                    className="rounded-full border border-sky-200 bg-sky-50 px-4 py-2 text-sky-700 transition hover:border-sky-300 hover:bg-sky-100"
                                >
                                    {t("layout:layout.nav.items.surveyResults")}
                                </Link>
                                {canManageFeedback ? (
                                    <Link
                                        to="/feedback/manage"
                                        className="rounded-full border border-cyan-200 bg-cyan-50 px-4 py-2 text-cyan-700 transition hover:border-cyan-300 hover:bg-cyan-100"
                                        >
                                        {t("layout:layout.nav.items.studentFeedback")}
                                    </Link>
                                ) : null}
                            </>
                        ) : null}
                        {session?.role === "ADMIN" ? (
                            <>
                                <Link
                                    to="/admin/users"
                                    className="rounded-full border border-slate-200 bg-slate-50 px-4 py-2 text-slate-700 transition hover:border-slate-300 hover:bg-slate-100"
                                >
                                    {t("layout:layout.nav.items.users")}
                                </Link>
                                <Link
                                    to="/admin/students/pending"
                                    className="rounded-full border border-amber-200 bg-amber-50 px-4 py-2 text-amber-700 transition hover:border-amber-300 hover:bg-amber-100"
                                >
                                    {t("layout:layout.nav.items.pendingStudents")}
                                </Link>
                                <Link
                                    to="/admin/surveys"
                                    className="rounded-full border border-indigo-200 bg-indigo-50 px-4 py-2 text-indigo-700 transition hover:border-indigo-300 hover:bg-indigo-100"
                                >
                                    {t("layout:layout.nav.items.surveys")}
                                </Link>
                            </>
                        ) : null}
                    </div>
                </div>

                <div className="flex items-center gap-3">
                    <LanguageSwitcher />
                    {isStudent ? <NotificationBell /> : null}
                    {session && (
                        <div className="hidden items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-2 md:flex">
                            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-slate-900 text-sm font-bold text-white">
                                {session.email.slice(0, 1).toUpperCase()}
                            </div>
                            <div className="text-right">
                                <p className="max-w-[220px] truncate text-sm font-bold text-slate-900">
                                    {session.email}
                                </p>
                                <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-slate-400">
                                    {getRoleLabelKey(session.role) ? t(`common:${getRoleLabelKey(session.role)}`) : session.role}
                                    {session.role === "STUDENT" && session.studentStatus
                                        ? ` | ${getStatusLabelKey(session.studentStatus) ? t(`common:${getStatusLabelKey(session.studentStatus)}`) : session.studentStatus}`
                                        : ""}
                                </p>
                            </div>
                        </div>
                    )}

                    <button
                        type="button"
                        onClick={handleLogout}
                        className="inline-flex items-center gap-2 rounded-full border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50"
                    >
                        <span className="material-symbols-outlined text-[18px]">logout</span>
                        <span>{t("layout:layout.header.accountMenu.logout")}</span>
                    </button>
                </div>
            </nav>
        </header>
    );
}
