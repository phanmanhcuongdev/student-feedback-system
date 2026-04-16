type PaginationControlsProps = {
    page: number;
    pageCount: number;
    onPageChange: (page: number) => void;
};

export default function PaginationControls({
    page,
    pageCount,
    onPageChange,
}: PaginationControlsProps) {
    if (pageCount <= 1) {
        return null;
    }

    return (
        <div className="flex flex-col gap-3 rounded-2xl border border-slate-200 bg-white px-4 py-3 shadow-sm sm:flex-row sm:items-center sm:justify-between">
            <p className="text-sm font-medium text-slate-500">
                Page {page} of {pageCount}
            </p>
            <div className="flex items-center gap-2">
                <button
                    type="button"
                    onClick={() => onPageChange(page - 1)}
                    disabled={page <= 1}
                    className="rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                >
                    Previous
                </button>
                <button
                    type="button"
                    onClick={() => onPageChange(page + 1)}
                    disabled={page >= pageCount}
                    className="rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-50"
                >
                    Next
                </button>
            </div>
        </div>
    );
}
