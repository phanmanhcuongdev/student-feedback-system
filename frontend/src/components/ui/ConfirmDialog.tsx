import { darkActionButtonClass, darkActionButtonStyle } from "./buttonStyles";

type ConfirmDialogProps = {
    open: boolean;
    title: string;
    description: string;
    confirmLabel: string;
    tone?: "default" | "danger";
    busy?: boolean;
    onConfirm: () => void;
    onCancel: () => void;
};

export default function ConfirmDialog({
    open,
    title,
    description,
    confirmLabel,
    tone = "default",
    busy = false,
    onConfirm,
    onCancel,
}: ConfirmDialogProps) {
    if (!open) {
        return null;
    }

    const confirmClassName = tone === "danger"
        ? "border-red-200 bg-red-600 text-white hover:bg-red-700 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-red-600 focus-visible:ring-offset-2"
        : `${darkActionButtonClass} border border-slate-900`;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-950/40 px-4 py-6">
            <div className="w-full max-w-md rounded-[28px] border border-slate-200 bg-white p-6 shadow-2xl">
                <h2 className="text-xl font-bold text-slate-950">{title}</h2>
                <p className="mt-3 text-sm leading-6 text-slate-500">{description}</p>

                <div className="mt-6 flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
                    <button
                        type="button"
                        onClick={onCancel}
                        disabled={busy}
                        className="inline-flex items-center justify-center rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
                    >
                        Cancel
                    </button>
                    <button
                        type="button"
                        onClick={onConfirm}
                        disabled={busy}
                        className={`inline-flex items-center justify-center rounded-2xl border px-4 py-3 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-60 ${confirmClassName}`}
                        style={tone === "danger" ? undefined : darkActionButtonStyle}
                    >
                        <span className={tone === "danger" ? "" : "text-white"} style={tone === "danger" ? undefined : darkActionButtonStyle}>
                            {busy ? "Working..." : confirmLabel}
                        </span>
                    </button>
                </div>
            </div>
        </div>
    );
}
