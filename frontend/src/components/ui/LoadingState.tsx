type LoadingStateVariant = "page" | "card" | "table";

type LoadingStateProps = {
    label?: string;
    variant?: LoadingStateVariant;
};

const variantClassNames: Record<LoadingStateVariant, string> = {
    page: "rounded-[28px] border border-slate-200 bg-white px-6 py-12 text-center shadow-sm",
    card: "rounded-2xl border border-slate-200 bg-slate-50 px-4 py-5",
    table: "rounded-[24px] border border-slate-200 bg-white px-5 py-6 shadow-sm",
};

export default function LoadingState({ label = "Loading...", variant = "page" }: LoadingStateProps) {
    return (
        <div className={`${variantClassNames[variant]} text-sm font-medium text-slate-500`}>
            <div className="mx-auto flex max-w-sm items-center justify-center gap-3">
                <span className="h-3 w-3 animate-pulse rounded-full bg-slate-400" />
                <span>{label}</span>
            </div>
        </div>
    );
}
