export default function MainFooter() {
    return (
        <footer className="w-full border-t border-slate-200 bg-slate-950 py-8 text-slate-300">
            <div className="mx-auto flex max-w-screen-xl flex-col items-center justify-between gap-6 px-6 lg:flex-row">
                <div className="flex flex-col items-center gap-2 lg:items-start">
                    <span className="text-lg font-bold tracking-tight text-white">
                        University Operations
                    </span>
                    <p className="text-sm text-slate-400">
                        Internal platform for student onboarding and survey feedback.
                    </p>
                </div>

                <div className="flex items-center gap-8">
                    <span className="rounded-full border border-white/10 bg-white/5 px-3 py-1 text-xs font-semibold uppercase tracking-[0.18em] text-slate-300">
                        Authenticated Access
                    </span>
                    <span className="text-sm text-slate-500">
                        Copyright 2026
                    </span>
                </div>
            </div>
        </footer>
    );
}
