import { NavLink } from "react-router-dom";
import type { NavigationGroup } from "./appNavigation";

type AppSidebarProps = {
    groups: NavigationGroup[];
    onNavigate?: () => void;
};

export default function AppSidebar({ groups, onNavigate }: AppSidebarProps) {
    return (
        <aside className="flex h-full flex-col border-r border-slate-200 bg-white">
            <div className="border-b border-slate-200 px-5 py-5">
                <div className="flex items-center gap-3">
                    <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-900 text-white shadow-sm">
                        <span className="material-symbols-outlined text-[22px]">school</span>
                    </div>
                    <div>
                        <p className="text-sm font-extrabold tracking-tight text-slate-950">
                            University Operations
                        </p>
                        <p className="text-[11px] font-semibold uppercase tracking-[0.2em] text-slate-400">
                            Onboarding and Feedback
                        </p>
                    </div>
                </div>
            </div>

            <nav className="flex-1 overflow-y-auto px-4 py-5">
                <div className="space-y-6">
                    {groups.map((group) => (
                        <section key={group.title}>
                            <p className="px-3 text-[11px] font-bold uppercase tracking-[0.22em] text-slate-400">
                                {group.title}
                            </p>
                            <div className="mt-3 space-y-1.5">
                                {group.items.map((item) => (
                                    <NavLink
                                        key={item.to}
                                        to={item.to}
                                        onClick={onNavigate}
                                        className={({ isActive }) =>
                                            [
                                                "flex items-center gap-3 rounded-2xl px-3 py-3 text-sm font-semibold transition",
                                                isActive
                                                    ? "bg-slate-900 text-white shadow-sm"
                                                    : "text-slate-600 hover:bg-slate-100 hover:text-slate-950",
                                            ].join(" ")
                                        }
                                    >
                                        <span className="material-symbols-outlined text-[20px]">{item.icon}</span>
                                        <span>{item.label}</span>
                                    </NavLink>
                                ))}
                            </div>
                        </section>
                    ))}
                </div>
            </nav>
        </aside>
    );
}
