export default function SurveyEmptyState() {
    return (
        <div className="mx-auto mt-12 flex max-w-md flex-col items-center rounded-[28px] border border-slate-200 bg-white px-8 py-10 text-center shadow-sm">
            <div className="mb-6 flex h-20 w-20 items-center justify-center rounded-full bg-slate-100">
                <span className="material-symbols-outlined text-[34px] text-slate-400">
                    content_paste_search
                </span>
            </div>

            <h2 className="mb-3 text-2xl font-bold text-slate-900">
                No surveys found
            </h2>

            <p className="mb-1 text-sm leading-6 text-slate-500">
                There are no surveys available right now. Check back later for new requests.
            </p>
        </div>
    );
}
