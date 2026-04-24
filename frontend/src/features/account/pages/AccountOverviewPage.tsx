import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { useAuth } from "../../auth/useAuth";
import SectionCard from "../../../components/ui/SectionCard";
import StatusBadge from "../../../components/ui/StatusBadge";
import { darkActionButtonClass } from "../../../components/ui/buttonStyles";

export default function AccountOverviewPage() {
    const { t } = useTranslation(["account"]);
    const { session } = useAuth();

    if (!session) {
        return null;
    }

    const studentStatus =
        session.role === "STUDENT" && session.studentStatus
            ? session.studentStatus.replace(/_/g, " ").toLowerCase().replace(/^\w/, (letter) => letter.toUpperCase())
            : t("account:account.common.notAvailable");

    return (
        <div className="space-y-8">
            <SectionCard
                title={t("account:account.overview.profile.title")}
                description={t("account:account.overview.profile.description")}
                className="rounded-2xl border-slate-100 p-6 shadow-[0_10px_30px_rgba(15,23,42,0.06)] sm:p-8"
            >
                <div className="overflow-hidden rounded-2xl border border-slate-100 bg-slate-50">
                    <dl className="grid sm:grid-cols-2">
                        <div className="border-b border-slate-100 p-6 sm:border-r">
                            <dt className="text-sm font-semibold uppercase tracking-wider text-slate-500">
                                {t("account:account.overview.profile.email")}
                            </dt>
                            <dd className="mt-3 text-base font-semibold leading-7 text-slate-950">
                                {session.email}
                            </dd>
                        </div>
                        <div className="border-b border-slate-100 p-6">
                            <dt className="text-sm font-semibold uppercase tracking-wider text-slate-500">
                                {t("account:account.overview.profile.role")}
                            </dt>
                            <dd className="mt-3 text-base font-semibold leading-7 text-slate-950">
                                {session.role}
                            </dd>
                        </div>
                        <div className="border-b border-slate-100 p-6 sm:border-b-0 sm:border-r">
                            <dt className="text-sm font-semibold uppercase tracking-wider text-slate-500">
                                {t("account:account.overview.profile.userId")}
                            </dt>
                            <dd className="mt-3 text-base font-semibold leading-7 text-slate-950">
                                {session.userId}
                            </dd>
                        </div>
                        <div className="p-6">
                            <dt className="text-sm font-semibold uppercase tracking-wider text-slate-500">
                                {t("account:account.overview.profile.studentStatus")}
                            </dt>
                            <dd className="mt-3 text-base font-semibold leading-7 text-slate-950">
                                {studentStatus}
                            </dd>
                        </div>
                    </dl>
                </div>
            </SectionCard>

            <div className="grid gap-8 xl:grid-cols-[minmax(0,1fr)_18.75rem]">
                <SectionCard
                    title={t("account:account.overview.details.title")}
                    description={t("account:account.overview.details.description")}
                    className="rounded-2xl border-slate-100 p-6 shadow-[0_10px_30px_rgba(15,23,42,0.06)] sm:p-8"
                >
                    <div className="space-y-6">
                        <div className="flex flex-wrap items-center gap-3">
                            <StatusBadge kind="role" value={session.role} />
                            {session.role === "STUDENT" && session.studentStatus ? (
                                <StatusBadge kind="onboarding" value={session.studentStatus} />
                            ) : null}
                        </div>
                        <p className="max-w-2xl text-base leading-7 text-slate-600">
                            {t("account:account.overview.details.body")}
                        </p>
                    </div>
                </SectionCard>

                <SectionCard
                    title={t("account:account.overview.security.title")}
                    description={t("account:account.overview.security.description")}
                    className="rounded-2xl border-slate-100 p-6 shadow-[0_10px_30px_rgba(15,23,42,0.06)] sm:p-8"
                >
                    <div className="space-y-6">
                        <p className="text-base leading-7 text-slate-600">
                        {t("account:account.overview.security.body")}
                        </p>
                        <Link
                            to="/account/security"
                            className={`w-full px-4 py-3 text-sm font-semibold ${darkActionButtonClass}`}
                        >
                            <span>{t("account:account.overview.security.button")}</span>
                            <span className="material-symbols-outlined text-[18px]">shield_lock</span>
                        </Link>
                    </div>
                </SectionCard>
            </div>
        </div>
    );
}
