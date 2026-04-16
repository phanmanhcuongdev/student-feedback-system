import type { ReactNode } from "react";

type PageToolbarProps = {
    leading?: ReactNode;
    trailing?: ReactNode;
};

export default function PageToolbar({ leading, trailing }: PageToolbarProps) {
    return (
        <div className="flex flex-col gap-3 rounded-[24px] border border-slate-200 bg-white px-4 py-4 shadow-sm sm:px-5 lg:flex-row lg:items-start lg:justify-between">
            <div className="flex min-w-0 flex-1 flex-wrap items-center gap-2.5">
                {leading}
            </div>
            {trailing ? (
                <div className="flex flex-wrap items-center gap-2.5">
                    {trailing}
                </div>
            ) : null}
        </div>
    );
}
