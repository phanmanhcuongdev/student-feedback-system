export default function SurveyEmptyState() {
    return (
        <div className="mt-20 flex flex-col items-center text-center max-w-md mx-auto">

            {/* ICON */}
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

            {/* TITLE */}
            <h2 className="text-2xl font-bold text-slate-900 mb-3">
                No surveys found
            </h2>

            {/* DESCRIPTION */}
            <p className="text-slate-500 mb-8">
                It looks like there aren't any active surveys for your department right now.
                Check back later or contact your advisor.
            </p>

            {/* BUTTON */}
            <button
                type="button"
                className="bg-slate-100 text-blue-600 px-8 py-3 rounded-full font-bold text-sm hover:bg-blue-100 transition-all active:scale-95"
            >
                Refresh Dashboard
            </button>

        </div>
    );
}