import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useNotifications } from "./useNotifications";

export default function NotificationBell() {
    const { t } = useTranslation("layout");
    const { unreadCount } = useNotifications();
    const badge = unreadCount > 99 ? "99+" : String(unreadCount);

    return (
        <Link
            to="/notifications"
            aria-label={t("layout.nav.items.notifications")}
            className="relative inline-flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl border border-slate-200 bg-white text-slate-700 transition hover:border-slate-300 hover:bg-slate-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 focus-visible:ring-offset-2"
        >
            <span className="material-symbols-outlined text-[21px]">notifications</span>
            {unreadCount > 0 ? (
                <span className="absolute -right-1.5 -top-1.5 flex min-w-5 items-center justify-center rounded-full border-2 border-white bg-red-600 px-1.5 text-[10px] font-extrabold leading-4 text-white">
                    {badge}
                </span>
            ) : null}
        </Link>
    );
}
