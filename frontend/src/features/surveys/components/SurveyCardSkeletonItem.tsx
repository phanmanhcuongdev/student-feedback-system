export default function SurveyCardSkeletonItem() {
    return (
        <div className="bg-white rounded-xl p-6 flex flex-col h-full border border-slate-200 animate-pulse">

            {/* HEADER */}
            <div className="flex justify-between items-start mb-4">
                <div className="bg-slate-200 h-6 w-16 rounded-full"></div>
                <div className="bg-slate-200 h-4 w-12 rounded"></div>
            </div>

            {/* TITLE */}
            <div className="bg-slate-200 h-7 w-3/4 rounded mb-2"></div>

            {/* DESCRIPTION */}
            <div className="bg-slate-200 h-4 w-full rounded mb-2"></div>
            <div className="bg-slate-200 h-4 w-2/3 rounded mb-6"></div>

            {/* FOOTER */}
            <div className="space-y-4 pt-6 border-t border-slate-200">

                <div className="flex items-center justify-between">
                    <div className="bg-slate-200 h-4 w-24 rounded"></div>
                    <div className="bg-slate-200 h-4 w-12 rounded"></div>
                </div>

                <div className="h-1.5 w-full bg-slate-300 rounded-full"></div>

                <div className="bg-slate-200 h-11 w-full rounded-lg"></div>

            </div>
        </div>
    );
}