type SurveyFilter = "ALL" | "OPEN" | "CLOSED";

type SurveyFilterTabsProps = {
    value: SurveyFilter;
    onChange: (value: SurveyFilter) => void;
};

const tabs: { label: string; value: SurveyFilter }[] = [
    { label: "All", value: "ALL" },
    { label: "Open", value: "OPEN" },
    { label: "Closed", value: "CLOSED" },
];

export default function SurveyFilterTabs({
                                             value,
                                             onChange,
                                         }: SurveyFilterTabsProps) {
    return (
        <div className="bg-slate-200/60 p-1 rounded-full flex gap-1">
            {tabs.map((tab) => {
                const isActive = value === tab.value;

                return (
                    <button
                        key={tab.value}
                        type="button"
                        onClick={() => onChange(tab.value)}
                        className={[
                            "px-6 py-2 rounded-full text-sm font-semibold transition-all active:scale-95 cursor-pointer",
                            isActive
                                ? "bg-white text-primary shadow-sm"
                                : "text-on-surface-variant hover:bg-white/50 hover:text-slate-700",
                        ].join(" ")}
                    >
                        {tab.label}
                    </button>
                );
            })}
        </div>
    );
}