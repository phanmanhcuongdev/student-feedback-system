import type { ReactNode } from "react";

type EmptyStateProps = {
    title: string;
    description: string;
    icon?: string;
    action?: ReactNode;
};

export default function EmptyState({
    title,
    description,
    icon = "inbox",
    action,
}: EmptyStateProps) {
    return (
        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-10 text-center shadow-sm">
            <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-slate-100 text-slate-500">
                <span className="material-symbols-outlined text-[26px]">{icon}</span>
            </div>
            <h2 className="text-xl font-bold text-slate-900 sm:text-2xl">{title}</h2>
            <p className="mx-auto mt-3 max-w-xl text-sm leading-6 text-slate-500">
                {description}
            </p>
            {action ? (
                <div className="mt-6 flex justify-center">
                    {action}
                </div>
            ) : null}
        </div>
    );
}
