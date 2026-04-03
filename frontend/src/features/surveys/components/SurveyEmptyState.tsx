export default function SurveyEmptyState() {
    return (
        <div className="mt-20 flex flex-col items-center text-center max-w-md mx-auto">
            <div className="w-48 h-48 bg-blue-50 rounded-full flex items-center justify-center mb-8 relative">
                <span className="material-symbols-outlined text-7xl text-blue-200">
                    content_paste_search
                </span>

                <div className="absolute -bottom-2 -right-2 bg-white p-3 rounded-xl shadow-md">
                    <span className="material-symbols-outlined text-blue-600">
                        priority_high
                    </span>
                </div>
            </div>

            <h2 className="text-2xl font-bold text-slate-900 mb-3">
                No surveys found
            </h2>

            <p className="text-slate-500 mb-8">
                It looks like there are not any surveys available right now.
                Check back later.
            </p>
        </div>
    );
}
