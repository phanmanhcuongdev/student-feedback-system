import { useTranslation } from "react-i18next";

export default function MainFooter() {
    const { t } = useTranslation("layout");

    return (
        <footer className="w-full border-t border-slate-200 bg-slate-950 py-8 text-slate-300">
            <div className="mx-auto flex max-w-screen-xl flex-col items-center justify-between gap-6 px-6 lg:flex-row">
                <div className="flex flex-col items-center gap-2 lg:items-start">
                    <span className="text-lg font-bold tracking-tight text-white">
                        {t("layout.footer.name")}
                    </span>
                    <p className="text-sm text-slate-400">
                        {t("layout.footer.description")}
                    </p>
                </div>

                <div className="flex items-center gap-8">
                    <span className="rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] text-slate-300">
                        {t("layout.footer.access")}
                    </span>
                    <span className="text-sm text-slate-500">
                        {t("layout.footer.copyright")}
                    </span>
                </div>
            </div>
        </footer>
    );
}
