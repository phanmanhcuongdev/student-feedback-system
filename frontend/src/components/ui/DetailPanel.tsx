import type { ReactNode } from "react";

type DetailPanelItem = {
    label: string;
    value: ReactNode;
};

type DetailPanelProps = {
    title?: string;
    items: DetailPanelItem[];
};

export default function DetailPanel({ title, items }: DetailPanelProps) {
    return (
        <div className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
            {title ? (
                <h2 className="text-xl font-bold text-slate-950">{title}</h2>
            ) : null}
            <div className={`grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600 ${title ? "mt-5" : ""}`}>
                {items.map((item) => (
                    <div key={item.label} className="flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between sm:gap-4">
                        <span className="font-semibold text-slate-500">{item.label}</span>
                        <span className="text-right font-medium text-slate-900">{item.value}</span>
                    </div>
                ))}
            </div>
        </div>
    );
}
