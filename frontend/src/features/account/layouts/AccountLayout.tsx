import { Outlet } from "react-router-dom";
import { useTranslation } from "react-i18next";
import PageHeader from "../../../components/ui/PageHeader";
import AccountNavigationCard from "../components/AccountNavigationCard";

export default function AccountLayout() {
    const { t } = useTranslation(["account"]);

    return (
        <div className="mx-auto max-w-6xl space-y-8">
            <PageHeader
                eyebrow={t("account:account.layout.eyebrow")}
                title={t("account:account.layout.title")}
                description={t("account:account.layout.description")}
            />

            <div className="grid gap-8 xl:grid-cols-[18.75rem_minmax(0,1fr)]">
                <div className="min-w-0">
                    <AccountNavigationCard />
                </div>
                <div className="min-w-0">
                    <Outlet />
                </div>
            </div>
        </div>
    );
}
