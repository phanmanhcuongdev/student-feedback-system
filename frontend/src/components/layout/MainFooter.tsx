export default function MainFooter() {
    return (
        <footer className="w-full border-t border-slate-100 bg-slate-50 py-12">
            <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-6 px-6 lg:flex-row lg:px-12">
                <div className="flex flex-col items-center gap-2 lg:items-start">
                    <span className="font-manrope text-lg font-bold tracking-tighter text-slate-400">
                        Insight Observatory
                    </span>
                    <p className="text-sm text-slate-400">
                        Copyright 2024 Insight Observatory. All rights reserved.
                    </p>
                </div>

                <div className="flex items-center gap-8">
                    <a href="#" className="text-sm font-medium text-slate-400 transition-colors hover:text-blue-500">
                        Privacy Policy
                    </a>
                    <a href="#" className="text-sm font-medium text-slate-400 transition-colors hover:text-blue-500">
                        Terms of Service
                    </a>
                    <a href="#" className="text-sm font-medium text-slate-400 transition-colors hover:text-blue-500">
                        Help Center
                    </a>
                </div>
            </div>
        </footer>
    );
}
