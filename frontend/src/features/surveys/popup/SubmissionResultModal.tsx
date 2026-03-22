type SubmissionResultModalProps = {
    isOpen: boolean;
    success: boolean;
    message: string;
    onOk: () => void;
};

export default function SubmissionResultModal({
                                                  isOpen,
                                                  success,
                                                  message,
                                                  onOk,
                                              }: SubmissionResultModalProps) {
    if (!isOpen) return null;

    const title = success ? "Submission Successful" : "Submission Failed";

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-[#0b1c30]/60 backdrop-blur-sm p-4">
            <div className="w-full max-w-md overflow-hidden rounded-xl bg-white shadow-[0_24px_48px_rgba(11,28,48,0.16)]">
                <div className="flex flex-col items-center p-8 text-center">
                    {success ? (
                        <div className="relative mb-6">
                            <div className="absolute inset-0 scale-150 rounded-full bg-emerald-600/10 blur-xl" />
                            <div className="relative flex h-20 w-20 items-center justify-center rounded-full bg-emerald-600 shadow-lg shadow-emerald-600/20">
                                <svg
                                    className="h-10 w-10 text-white"
                                    viewBox="0 0 24 24"
                                    fill="none"
                                    stroke="currentColor"
                                    strokeWidth="2.5"
                                    strokeLinecap="round"
                                    strokeLinejoin="round"
                                >
                                    <path d="M20 6 9 17l-5-5" />
                                </svg>
                            </div>
                        </div>
                    ) : (
                        <div className="mb-6 flex h-20 w-20 items-center justify-center rounded-full bg-red-100">
                            <svg
                                className="h-10 w-10 text-red-600"
                                viewBox="0 0 24 24"
                                fill="currentColor"
                            >
                                <path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2Zm1 15h-2v-2h2Zm0-4h-2V7h2Z" />
                            </svg>
                        </div>
                    )}

                    <h2 className="mb-3 text-2xl font-extrabold tracking-tight text-slate-900">
                        {title}
                    </h2>

                    <p className="mb-8 max-w-[280px] text-base leading-relaxed text-slate-500">
                        {message}
                    </p>

                    <button
                        type="button"
                        onClick={onOk}
                        className={`w-full rounded-lg py-3.5 px-6 text-base font-bold text-white shadow-md transition active:scale-[0.98] ${
                            success
                                ? "bg-gradient-to-br from-blue-600 to-blue-500 hover:opacity-90"
                                : "bg-blue-600 hover:bg-blue-500"
                        }`}
                    >
                        OK
                    </button>
                </div>

                {!success && <div className="h-1.5 w-full bg-red-600/10" />}
            </div>
        </div>
    );
}