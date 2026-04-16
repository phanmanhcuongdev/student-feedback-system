import { Outlet } from "react-router-dom";
import PageHeader from "../../../components/ui/PageHeader";
import AccountNavigationCard from "../components/AccountNavigationCard";

export default function AccountLayout() {
    return (
        <div className="mx-auto max-w-6xl space-y-6">
            <PageHeader
                eyebrow="Account"
                title="Account settings"
                description="Manage your own authenticated account separately from operational work areas. Use this area for profile context and password security."
            />

            <div className="grid gap-6 xl:grid-cols-[300px_minmax(0,1fr)]">
                <div>
                    <AccountNavigationCard />
                </div>
                <div className="min-w-0">
                    <Outlet />
                </div>
            </div>
        </div>
    );
}
