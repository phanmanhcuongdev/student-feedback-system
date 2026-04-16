import { useState } from "react";
import { Outlet } from "react-router-dom";
import { useAuth } from "../../features/auth/useAuth";
import AppHeader from "./AppHeader";
import AppSidebar from "./AppSidebar";
import { getNavigationGroups } from "./appNavigation";

export default function AppShell() {
    const { session } = useAuth();
    const [mobileNavigationOpen, setMobileNavigationOpen] = useState(false);

    if (!session) {
        return null;
    }

    const groups = getNavigationGroups(session.role);

    return (
        <div className="min-h-screen bg-slate-100 text-slate-900">
            <div className="flex min-h-screen">
                <div className="hidden w-[292px] shrink-0 lg:block">
                    <AppSidebar groups={groups} />
                </div>

                {mobileNavigationOpen ? (
                    <div className="fixed inset-0 z-50 lg:hidden">
                        <button
                            type="button"
                            aria-label="Close navigation"
                            className="absolute inset-0 bg-slate-950/40"
                            onClick={() => setMobileNavigationOpen(false)}
                        />
                        <div className="relative h-full w-[292px] max-w-[85vw]">
                            <AppSidebar groups={groups} onNavigate={() => setMobileNavigationOpen(false)} />
                        </div>
                    </div>
                ) : null}

                <div className="flex min-h-screen min-w-0 flex-1 flex-col">
                    <AppHeader onOpenNavigation={() => setMobileNavigationOpen(true)} />
                    <div className="flex-1 px-4 py-5 sm:px-6 sm:py-6 lg:px-8">
                        <Outlet />
                    </div>
                </div>
            </div>
        </div>
    );
}
