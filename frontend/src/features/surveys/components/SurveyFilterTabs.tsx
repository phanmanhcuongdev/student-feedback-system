import { useTranslation } from "react-i18next";

export type SurveyFilter = "ALL" | "OPEN" | "CLOSED" | "NOT_OPEN";

type SurveyFilterTabsProps = {
    value: SurveyFilter;
    onChange: (value: SurveyFilter) => void;
};

const tabs: { labelKey: string; value: SurveyFilter }[] = [
    { labelKey: "surveys:surveys.filters.all", value: "ALL" },
    { labelKey: "surveys:surveys.filters.open", value: "OPEN" },
    { labelKey: "surveys:surveys.filters.upcoming", value: "NOT_OPEN" },
    { labelKey: "surveys:surveys.filters.closed", value: "CLOSED" },
];

export default function SurveyFilterTabs({
    value,
    onChange,
}: SurveyFilterTabsProps) {
    const { t } = useTranslation(["surveys"]);

    return (
        <div className="flex flex-wrap gap-1 rounded-full border border-slate-200 bg-white p-1 shadow-sm">
            {tabs.map((tab) => {
                const isActive = value === tab.value;

                return (
                    <button
                        key={tab.value}
                        type="button"
                        onClick={() => onChange(tab.value)}
                        className={[
                            "cursor-pointer rounded-full px-4 py-2 text-sm font-semibold transition-all active:scale-95 sm:px-5",
                            isActive
                                ? "bg-slate-900 text-white shadow-sm"
                                : "text-slate-500 hover:bg-slate-100 hover:text-slate-700",
                        ].join(" ")}
                    >
                        {t(tab.labelKey)}
                    </button>
                );
            })}
        </div>
    );
}
