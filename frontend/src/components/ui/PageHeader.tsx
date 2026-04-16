import type { ReactNode } from "react";

type PageHeaderProps = {
    eyebrow?: string;
    title: string;
    description?: string;
    actions?: ReactNode;
};

export default function PageHeader({ eyebrow, title, description, actions }: PageHeaderProps) {
    return (
        <div className="flex flex-col gap-4 rounded-[28px] border border-slate-200 bg-white px-6 py-7 shadow-sm sm:px-8 lg:flex-row lg:items-start lg:justify-between">
            <div className="max-w-3xl">
                {eyebrow ? (
                    <span className="inline-flex rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.22em] text-slate-600">
                        {eyebrow}
                    </span>
                ) : null}
                <h1 className="mt-4 text-3xl font-extrabold tracking-tight text-slate-950 sm:text-[2rem]">
                    {title}
                </h1>
                {description ? (
                    <p className="mt-3 text-sm leading-6 text-slate-500 sm:text-[15px]">
                        {description}
                    </p>
                ) : null}
            </div>

            {actions ? (
                <div className="flex shrink-0 flex-wrap items-center gap-3">
                    {actions}
                </div>
            ) : null}
        </div>
    );
}
