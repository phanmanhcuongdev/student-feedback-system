import type { ReactNode } from "react";

type SectionCardProps = {
    title?: string;
    description?: string;
    actions?: ReactNode;
    children: ReactNode;
    className?: string;
};

export default function SectionCard({
    title,
    description,
    actions,
    children,
    className = "",
}: SectionCardProps) {
    return (
        <section className={`rounded-[28px] border border-slate-200 bg-white p-5 shadow-sm sm:p-6 ${className}`.trim()}>
            {title || description || actions ? (
                <div className="mb-6 flex flex-col gap-4 border-b border-slate-100 pb-4 lg:flex-row lg:items-start lg:justify-between">
                    <div className="max-w-3xl">
                        {title ? (
                            <h2 className="text-xl font-bold text-slate-950 sm:text-[1.35rem]">
                                {title}
                            </h2>
                        ) : null}
                        {description ? (
                            <p className="mt-2 text-sm leading-6 text-slate-500">
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
            ) : null}

            {children}
        </section>
    );
}
