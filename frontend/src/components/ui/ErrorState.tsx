import type { ReactNode } from "react";

type ErrorStateProps = {
    title?: string;
    description: string;
    retryLabel?: string;
    onRetry?: () => void;
    action?: ReactNode;
};

export default function ErrorState({
    title = "Something went wrong",
    description,
    retryLabel = "Retry",
    onRetry,
    action,
}: ErrorStateProps) {
    return (
        <div className="rounded-[28px] border border-red-200 bg-red-50 px-5 py-5 text-red-900 shadow-sm">
            <div className="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
                <div>
                    <h2 className="text-base font-bold">{title}</h2>
                    <p className="mt-2 text-sm leading-6 text-red-800">{description}</p>
                </div>
                {onRetry || action ? (
                    <div className="flex shrink-0 flex-wrap items-center gap-3">
                        {onRetry ? (
                            <button
                                type="button"
                                onClick={onRetry}
                                className="rounded-xl border border-red-300 bg-white px-4 py-2 text-sm font-semibold text-red-700 transition hover:border-red-400 hover:bg-red-100"
                            >
                                {retryLabel}
                            </button>
                        ) : null}
                        {action}
                    </div>
                ) : null}
            </div>
        </div>
    );
}
