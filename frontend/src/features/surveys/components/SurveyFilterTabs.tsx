export default function SurveyFilterTabs() {
    return (
        <div className="bg-slate-200/60 p-1 rounded-full flex gap-1">
            <button
                className="px-6 py-2 rounded-full text-sm font-semibold bg-white text-primary shadow-sm transition-all cursor-pointer hover:shadow active:scale-95">All
            </button>
            <button
                className="px-6 py-2 rounded-full text-sm font-semibold text-on-surface-variant hover:bg-white/50 cursor-pointer hover:text-slate-700 active:scale-95 transition-all">Open
            </button>
            <button
                className="px-6 py-2 rounded-full text-sm font-semibold text-on-surface-variant hover:bg-white/50 cursor-pointer hover:text-slate-700 active:scale-95 transition-all">Closed
            </button>
        </div>
    );
}