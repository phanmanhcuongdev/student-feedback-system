type StatCardTone = "default" | "blue" | "sky" | "emerald" | "amber" | "slate";

type StatCardProps = {
    label: string;
    value: string | number;
    tone?: StatCardTone;
    hint?: string;
};

const toneClassNames: Record<StatCardTone, string> = {
    default: "border-slate-200 bg-slate-50 text-slate-700",
    blue: "border-blue-200 bg-blue-50 text-blue-700",
    sky: "border-sky-200 bg-sky-50 text-sky-700",
    emerald: "border-emerald-200 bg-emerald-50 text-emerald-700",
    amber: "border-amber-200 bg-amber-50 text-amber-700",
    slate: "border-slate-200 bg-slate-50 text-slate-700",
};

export default function StatCard({ label, value, tone = "default", hint }: StatCardProps) {
    return (
        <div className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
            <span className={`inline-flex rounded-full border px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] ${toneClassNames[tone]}`}>
                {label}
            </span>
            <p className="mt-5 text-4xl font-extrabold tracking-tight text-slate-950">
                {value}
            </p>
            {hint ? (
                <p className="mt-2 text-sm text-slate-500">{hint}</p>
            ) : null}
        </div>
    );
}
