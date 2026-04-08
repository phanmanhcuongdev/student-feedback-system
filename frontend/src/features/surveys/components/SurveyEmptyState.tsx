export default function SurveyEmptyState() {
    return (
        <div className="mx-auto mt-20 flex max-w-md flex-col items-center rounded-[28px] border border-slate-200 bg-white px-8 py-10 text-center shadow-sm">
            <div className="relative mb-8 flex h-40 w-40 items-center justify-center rounded-full bg-blue-50">
                <span className="material-symbols-outlined text-7xl text-blue-200">
                    content_paste_search
                </span>

                <div className="absolute -bottom-2 -right-2 rounded-xl bg-white p-3 shadow-md">
                    <span className="material-symbols-outlined text-blue-600">
                        priority_high
                    </span>
                </div>
            </div>

            <h2 className="text-2xl font-bold text-slate-900 mb-3">
                No surveys found
            </h2>

            <p className="mb-1 text-slate-500">
                It looks like there are not any surveys available right now.
                Check back later.
            </p>
        </div>
    );
}
