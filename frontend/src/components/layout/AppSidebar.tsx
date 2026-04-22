import { NavLink } from "react-router-dom";
import { useTranslation } from "react-i18next";
import type { NavigationGroup } from "./appNavigation";

type AppSidebarProps = {
    groups: NavigationGroup[];
    onNavigate?: () => void;
};

export default function AppSidebar({ groups, onNavigate }: AppSidebarProps) {
    const { t } = useTranslation("layout");

    return (
        <aside className="flex h-full flex-col border-r border-slate-200 bg-white">
            <div className="border-b border-slate-200 px-5 py-5">
                <div className="flex items-center gap-3">
                    <div className="flex h-11 w-11 items-center justify-center rounded-2xl bg-slate-900 text-white shadow-sm">
                        <span className="material-symbols-outlined text-[22px]">school</span>
                    </div>
                    <div>
                        <p className="text-sm font-extrabold tracking-tight text-slate-950">
                            {t("layout.brand.name")}
                        </p>
                        <p className="text-[11px] font-semibold uppercase tracking-[0.2em] text-slate-400">
                            {t("layout.brand.subtitle")}
                        </p>
                    </div>
                </div>
            </div>

            <nav className="flex-1 overflow-y-auto px-4 py-5">
                <div className="space-y-6">
                    {groups.map((group) => (
                        <section key={group.titleKey}>
                            <p className="px-3 text-[11px] font-bold uppercase tracking-[0.22em] text-slate-400">
                                {t(group.titleKey)}
                            </p>
                            <div className="mt-3 space-y-1.5">
                                {group.items.map((item) => (
                                    <NavLink
                                        key={item.to}
                                        to={item.to}
                                        end={item.to === "/account"}
                                        onClick={onNavigate}
                                        className={({ isActive }) => [
                                            "group flex items-center gap-3 rounded-2xl px-3 py-3 text-sm font-semibold transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-slate-900 focus-visible:ring-offset-2",
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
                                                    {item.icon}
                                                </span>
                                                <span className={isActive ? "text-white" : "text-slate-700 group-hover:text-slate-950"}>
                                                    {t(item.labelKey)}
                                                </span>
                                            </>
                                        )}
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
