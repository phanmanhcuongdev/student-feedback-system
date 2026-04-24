import { useTranslation } from "react-i18next";

export default function SurveyHero() {
    const { t } = useTranslation(["surveys"]);

    return (
        <div className="max-w-2xl">
            <span className="mb-3 inline-flex rounded-full border border-sky-200 bg-sky-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-sky-700">
                {t("surveys:surveys.hero.workspace")}
            </span>
            <h1 className="text-4xl font-extrabold tracking-tight text-slate-950 md:text-[2.8rem]">
                {t("surveys:surveys.hero.title")}
            </h1>
            <p className="mt-4 max-w-xl text-base leading-7 text-slate-500">
                {t("surveys:surveys.hero.description")}
            </p>
        </div>
    );
}
