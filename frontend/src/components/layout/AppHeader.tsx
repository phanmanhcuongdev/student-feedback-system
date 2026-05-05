import { useEffect, useRef, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../../features/auth/useAuth";
import NotificationBell from "../../features/notifications/NotificationBell";
import { getRoleLabelKey, getStatusLabelKey } from "./appNavigation";
import LanguageSwitcher from "./LanguageSwitcher";

type AppHeaderProps = {
    onOpenNavigation: () => void;
};

export default function AppHeader({ onOpenNavigation }: AppHeaderProps) {
    const { t } = useTranslation(["common", "layout"]);
    const navigate = useNavigate();
    const { session, logout } = useAuth();
    const [menuOpen, setMenuOpen] = useState(false);
    const menuRef = useRef<HTMLDivElement | null>(null);

    useEffect(() => {
        function handlePointerDown(event: MouseEvent) {
            if (!menuRef.current?.contains(event.target as Node)) {
                setMenuOpen(false);
            }
        }

        function handleEscape(event: KeyboardEvent) {
            if (event.key === "Escape") {
                setMenuOpen(false);
            }
        }

        document.addEventListener("mousedown", handlePointerDown);
        document.addEventListener("keydown", handleEscape);

        return () => {
            document.removeEventListener("mousedown", handlePointerDown);
            document.removeEventListener("keydown", handleEscape);
        };
    }, []);

    function handleLogout() {
        setMenuOpen(false);
        logout();
        navigate("/login", { replace: true });
    }

    if (!session) {
        return null;
    }

    return (
        <header className="sticky top-0 z-30 border-b border-slate-200 bg-white/95 backdrop-blur">
            <div className="flex items-center justify-between gap-4 px-4 py-3.5 sm:px-6 lg:px-8">
                <div className="flex items-center gap-3">
                    <button
                        type="button"
                        onClick={onOpenNavigation}
                        className="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-slate-200 bg-white text-slate-700 transition hover:border-slate-300 hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 focus-visible:ring-offset-2 lg:hidden"
                        aria-label={t("layout:layout.nav.open")}
                    >
                        <span className="material-symbols-outlined text-[20px]">menu</span>
                    </button>

                    <div>
                        <p className="text-xs font-bold uppercase tracking-[0.22em] text-slate-400">
                            {t("layout:layout.brand.name")}
                        </p>
                        <h1 className="text-lg font-bold tracking-tight text-slate-950">
                            {t("layout:layout.header.title")}
                        </h1>
                    </div>
                </div>

                <div className="flex items-center gap-3">
                    <LanguageSwitcher />
                    {session.role === "STUDENT" ? <NotificationBell /> : null}
                    <div className="relative" ref={menuRef}>
                        <button
                            type="button"
                            onClick={() => setMenuOpen((current) => !current)}
                            className="flex items-center gap-3 rounded-2xl border border-slate-200 bg-slate-50 px-3 py-2 text-left transition hover:border-slate-300 hover:bg-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 focus-visible:ring-offset-2 sm:px-4"
                            aria-haspopup="menu"
                            aria-expanded={menuOpen}
                        >
                            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-slate-900 text-sm font-bold text-white">
                                {session.email.slice(0, 1).toUpperCase()}
                            </div>
                            <div className="hidden text-left sm:block">
                                <p className="max-w-[220px] truncate text-sm font-bold text-slate-950">
                                    {session.email}
                                </p>
                                <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-slate-400">
                                    {getRoleLabelKey(session.role) ? t(`common:${getRoleLabelKey(session.role)}`) : session.role}
                                    {session.role === "STUDENT" && session.studentStatus
                                        ? ` | ${getStatusLabelKey(session.studentStatus) ? t(`common:${getStatusLabelKey(session.studentStatus)}`) : session.studentStatus}`
                                        : ""}
                                </p>
                            </div>
                            <span className="material-symbols-outlined text-[18px] text-slate-500">
                                expand_more
                            </span>
                        </button>

                        {menuOpen ? (
                            <div className="absolute right-0 top-[calc(100%+0.75rem)] z-40 w-[260px] rounded-[24px] border border-slate-200 bg-white p-2 shadow-[0_18px_40px_rgba(15,23,42,0.12)]">
                                <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3">
                                    <p className="truncate text-sm font-bold text-slate-950">{session.email}</p>
                                    <p className="mt-1 text-[11px] font-semibold uppercase tracking-[0.16em] text-slate-400">
                                        {getRoleLabelKey(session.role) ? t(`common:${getRoleLabelKey(session.role)}`) : session.role}
                                    </p>
                                </div>
                                <div className="mt-2 grid gap-1">
                                    <HeaderMenuLink to="/account" icon="account_circle" label={t("layout:layout.header.accountMenu.viewAccount")} onClick={() => setMenuOpen(false)} />
                                    <HeaderMenuLink to="/account/security" icon="shield_lock" label={t("layout:layout.nav.items.security")} onClick={() => setMenuOpen(false)} />
                                    <button
                                        type="button"
                                        onClick={handleLogout}
                                        className="flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-100 hover:text-slate-950 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 focus-visible:ring-offset-2"
                                    >
                                        <span className="material-symbols-outlined text-[20px]">logout</span>
                                        <span>{t("layout:layout.header.accountMenu.logout")}</span>
                                    </button>
                                </div>
                            </div>
                        ) : null}
                    </div>
                </div>
            </div>
        </header>
    );
}

function HeaderMenuLink({
    to,
    icon,
    label,
    onClick,
}: {
    to: string;
    icon: string;
    label: string;
    onClick: () => void;
}) {
    return (
        <Link
            to={to}
            onClick={onClick}
            className="flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-100 hover:text-slate-950 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 focus-visible:ring-offset-2"
        >
            <span className="material-symbols-outlined text-[20px]">{icon}</span>
            <span>{label}</span>
        </Link>
    );
}
