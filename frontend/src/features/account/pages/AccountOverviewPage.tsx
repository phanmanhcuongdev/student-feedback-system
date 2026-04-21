import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../../auth/useAuth";
import EmptyState from "../../../components/ui/EmptyState";
import InfoCard from "../../../components/ui/InfoCard";
import SectionCard from "../../../components/ui/SectionCard";
import StatusBadge from "../../../components/ui/StatusBadge";
import { darkActionButtonClass, darkActionButtonStyle } from "../../../components/ui/buttonStyles";

export default function AccountOverviewPage() {
    const { t } = useTranslation(["account"]);
    const { session } = useAuth();

    if (!session) {
        return null;
    }

    return (
        <div className="space-y-6">
            <SectionCard
                title={t("account:account.overview.profile.title")}
                description={t("account:account.overview.profile.description")}
            >
                <div className="grid gap-4 sm:grid-cols-2">
                    <InfoCard label={t("account:account.overview.profile.email")} value={session.email} />
                    <InfoCard label={t("account:account.overview.profile.role")} value={session.role} />
                    <InfoCard label={t("account:account.overview.profile.userId")} value={String(session.userId)} />
                    <InfoCard
                        label={t("account:account.overview.profile.studentStatus")}
                        value={session.role === "STUDENT" && session.studentStatus ? session.studentStatus.replace(/_/g, " ").toLowerCase().replace(/^\w/, (letter) => letter.toUpperCase()) : t("account:account.common.notAvailable")}
                    />
                </div>
            </SectionCard>

            <div className="grid gap-6 lg:grid-cols-[1fr_320px]">
                <SectionCard
                    title={t("account:account.overview.details.title")}
                    description={t("account:account.overview.details.description")}
                >
                    <div className="space-y-4 rounded-2xl border border-slate-200 bg-slate-50 p-5">
                        <div className="flex flex-wrap items-center gap-2">
                            <StatusBadge kind="role" value={session.role} />
                            {session.role === "STUDENT" && session.studentStatus ? (
                                <StatusBadge kind="onboarding" value={session.studentStatus} />
                            ) : null}
                        </div>
                        <p className="text-sm leading-6 text-slate-600">
                            {t("account:account.overview.details.body")}
                        </p>
                    </div>
                </SectionCard>

                <SectionCard title={t("account:account.overview.security.title")} description={t("account:account.overview.security.description")}>
                    <p className="text-sm leading-6 text-slate-500">
                        {t("account:account.overview.security.body")}
                    </p>
                    <Link
                        to="/account/security"
                        className={`mt-5 w-full px-4 py-3 text-sm font-bold ${darkActionButtonClass}`}
                        style={darkActionButtonStyle}
                    >
                        <span className="text-white" style={darkActionButtonStyle}>{t("account:account.overview.security.button")}</span>
                        <span className="material-symbols-outlined text-[18px] text-white" style={darkActionButtonStyle}>shield_lock</span>
                    </Link>
                </SectionCard>
            </div>

            <EmptyState
                title={t("account:account.overview.empty.title")}
                description={t("account:account.overview.empty.description")}
                icon="account_circle"
            />
        </div>
    );
}
