type SearchInputProps = {
    value: string;
    onChange: (value: string) => void;
    placeholder?: string;
};

export default function SearchInput({
    value,
    onChange,
    placeholder = "Search",
}: SearchInputProps) {
    return (
        <label className="flex min-w-[220px] flex-1 items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm transition focus-within:border-slate-500 focus-within:ring-4 focus-within:ring-slate-900/5">
            <span className="material-symbols-outlined text-[18px] text-slate-400">search</span>
            <input
                type="search"
                value={value}
                onChange={(event) => onChange(event.target.value)}
                placeholder={placeholder}
                className="w-full border-0 bg-transparent p-0 text-sm text-slate-900 outline-none placeholder:text-slate-400"
            />
        </label>
    );
}
