type SelectFilterOption = {
    label: string;
    value: string;
};

type SelectFilterProps = {
    label: string;
    value: string;
    options: SelectFilterOption[];
    onChange: (value: string) => void;
};

export default function SelectFilter({ label, value, options, onChange }: SelectFilterProps) {
    return (
        <label className="flex min-w-[180px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm transition focus-within:border-slate-500 focus-within:ring-4 focus-within:ring-slate-900/5">
            <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">
                {label}
            </span>
            <select
                value={value}
                onChange={(event) => onChange(event.target.value)}
                className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none"
            >
                {options.map((option) => (
                    <option key={option.value} value={option.value}>
                        {option.label}
                    </option>
                ))}
            </select>
        </label>
    );
}
