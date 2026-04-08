import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../features/auth/useAuth";

function toStatusLabel(status: string | null) {
    if (!status) {
        return "";
    }

    return status.replace(/_/g, " ").toLowerCase().replace(/^\w/, (letter) => letter.toUpperCase());
}

export default function MainHeader() {
    const navigate = useNavigate();
    const { session, logout } = useAuth();
    const isStudent = session?.role === "STUDENT";
    const canViewResults = session?.role === "ADMIN" || session?.role === "TEACHER";

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
                                Insight Observatory
                            </span>
                            <span className="block text-xs font-medium uppercase tracking-[0.22em] text-slate-400">
                                Student Feedback Portal
                            </span>
                        </div>
                    </div>

                    <div className="hidden gap-6 text-sm font-semibold text-slate-500 md:flex">
                        {isStudent ? (
                            <Link
                                to="/surveys"
                                className="rounded-full border border-blue-200 bg-blue-50 px-4 py-2 text-blue-700 transition hover:border-blue-300 hover:bg-blue-100"
                            >
                                Surveys
                            </Link>
                        ) : null}
                        {canViewResults ? (
                            <Link
                                to="/survey-results"
                                className="rounded-full border border-sky-200 bg-sky-50 px-4 py-2 text-sky-700 transition hover:border-sky-300 hover:bg-sky-100"
                            >
                                Survey Results
                            </Link>
                        ) : null}
                        {session?.role === "ADMIN" ? (
                            <Link
                                to="/admin/students/pending"
                                className="rounded-full border border-amber-200 bg-amber-50 px-4 py-2 text-amber-700 transition hover:border-amber-300 hover:bg-amber-100"
                            >
                                Pending Students
                            </Link>
                        ) : null}
                    </div>
                </div>

                <div className="flex items-center gap-3">
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
                                    {session.role}
                                    {session.role === "STUDENT" && session.studentStatus ? ` | ${toStatusLabel(session.studentStatus)}` : ""}
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
                        <span>Logout</span>
                    </button>
                </div>
            </nav>
        </header>
    );
}
