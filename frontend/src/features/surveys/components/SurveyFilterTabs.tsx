export type SurveyFilter = "ALL" | "OPEN" | "CLOSED" | "NOT_OPEN";

type SurveyFilterTabsProps = {
    value: SurveyFilter;
    onChange: (value: SurveyFilter) => void;
};

const tabs: { label: string; value: SurveyFilter }[] = [
    { label: "All", value: "ALL" },
    { label: "Open", value: "OPEN" },
    { label: "Upcoming", value: "NOT_OPEN" },
    { label: "Closed", value: "CLOSED" },
];

export default function SurveyFilterTabs({
    value,
    onChange,
}: SurveyFilterTabsProps) {
    return (
        <div className="flex gap-1 rounded-full border border-slate-200 bg-white p-1 shadow-sm">
            {tabs.map((tab) => {
                const isActive = value === tab.value;

                return (
                    <button
                        key={tab.value}
                        type="button"
                        onClick={() => onChange(tab.value)}
                        className={[
                            "cursor-pointer rounded-full px-5 py-2 text-sm font-semibold transition-all active:scale-95",
                            isActive
                                ? "bg-slate-900 text-white shadow-sm"
                                : "text-slate-500 hover:bg-slate-100 hover:text-slate-700",
                        ].join(" ")}
                    >
                        {tab.label}
                    </button>
                );
            })}
        </div>
    );
}
