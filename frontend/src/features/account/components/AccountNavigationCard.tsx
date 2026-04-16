import { NavLink } from "react-router-dom";
import { useAuth } from "../../auth/useAuth";
import RoleBadge from "../../../components/ui/RoleBadge";
import StatusBadge from "../../../components/ui/StatusBadge";

export default function AccountNavigationCard() {
    const { session } = useAuth();

    if (!session) {
        return null;
    }

    return (
        <div className="space-y-5 rounded-[28px] border border-slate-200 bg-white p-5 shadow-sm">
            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                <div className="flex items-center gap-3">
                    <div className="flex h-12 w-12 items-center justify-center rounded-full bg-slate-900 text-sm font-bold text-white">
                        {session.email.slice(0, 1).toUpperCase()}
                    </div>
                    <div className="min-w-0">
                        <p className="truncate text-sm font-bold text-slate-950">{session.email}</p>
                        <p className="mt-1 text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">
                            Account settings
                        </p>
                    </div>
                </div>

                <div className="mt-4 flex flex-wrap items-center gap-2">
                    <RoleBadge value={session.role} />
                    {session.role === "STUDENT" && session.studentStatus ? (
                        <StatusBadge kind="onboarding" value={session.studentStatus} />
                    ) : null}
                </div>
            </div>

            <nav className="space-y-1.5">
                <AccountNavLink to="/account" end icon="account_circle" label="Account overview" />
                <AccountNavLink to="/account/security" icon="shield_lock" label="Security" />
            </nav>
        </div>
    );
}

function AccountNavLink({
    to,
    label,
    icon,
    end = false,
}: {
    to: string;
    label: string;
    icon: string;
    end?: boolean;
}) {
    return (
        <NavLink
            to={to}
            end={end}
            className={({ isActive }) => [
                "group flex items-center gap-3 rounded-2xl px-4 py-3 text-sm font-semibold transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 focus-visible:ring-offset-2",
                isActive
                    ? "bg-slate-900 text-white shadow-sm"
                    : "text-slate-600 hover:bg-slate-100 hover:text-slate-950",
            ].join(" ")}
        >
            {({ isActive }) => (
                <>
                    <span
                        className={[
                            "material-symbols-outlined text-[20px] transition-colors",
                            isActive ? "text-white" : "text-slate-500 group-hover:text-slate-900",
                        ].join(" ")}
                    >
                        {icon}
                    </span>
                    <span className={isActive ? "text-white" : "text-slate-700 group-hover:text-slate-950"}>
                        {label}
                    </span>
                </>
            )}
        </NavLink>
    );
}
