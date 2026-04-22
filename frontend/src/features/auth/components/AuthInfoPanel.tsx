import { useTranslation } from "react-i18next";

const highlightKeys = [
    "auth.onboarding.highlights.createAccount",
    "auth.onboarding.highlights.verifyDocuments",
    "auth.onboarding.highlights.accessSurveys",
];

export default function AuthInfoPanel() {
    const { t } = useTranslation("auth");

    return (
        <section className="relative overflow-hidden rounded-[28px] border border-white/60 bg-[linear-gradient(160deg,#0b1c30_0%,#123e6d_52%,#1d6ed6_100%)] p-8 text-white shadow-[0_30px_80px_rgba(8,23,44,0.28)] lg:p-10">
            <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(255,255,255,0.22),transparent_34%),radial-gradient(circle_at_bottom_left,rgba(255,255,255,0.14),transparent_28%)]" />

            <div className="relative space-y-8">
                <div className="space-y-4">
                    <span className="inline-flex rounded-full border border-white/20 bg-white/10 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.28em] text-blue-100">
                        {t("auth.onboarding.badge")}
                    </span>

                    <div className="space-y-3">
                        <h1 className="max-w-md text-4xl font-extrabold tracking-tight text-white md:text-5xl">
                            {t("auth.onboarding.title")}
                        </h1>
                        <p className="max-w-xl text-base leading-7 text-blue-50/88">
                            {t("auth.onboarding.description")}
                        </p>
                    </div>
                </div>

                <div className="grid gap-3">
                    {highlightKeys.map((itemKey) => (
                        <div
                            key={itemKey}
                            className="flex items-center gap-3 rounded-2xl border border-white/12 bg-white/8 px-4 py-3 backdrop-blur-sm"
                        >
                            <span className="material-symbols-outlined text-xl text-blue-100">check_circle</span>
                            <span className="text-sm font-medium text-white/92">{t(itemKey)}</span>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
}
