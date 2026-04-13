import { useEffect, useState } from "react";
import { getApiErrorMessage } from "../../../api/apiError";
import { getAllFeedback, respondToFeedback } from "../../../api/feedbackApi";
import MainFooter from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
import type { FeedbackResponse, StaffFeedback } from "../../../types/feedback";

function formatDate(date: string) {
    return new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    }).format(new Date(date));
}

export default function ManageFeedbackPage() {
    const [items, setItems] = useState<StaffFeedback[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [activeFeedbackId, setActiveFeedbackId] = useState<number | null>(null);
    const [drafts, setDrafts] = useState<Record<number, string>>({});
    const [submittingId, setSubmittingId] = useState<number | null>(null);

    async function loadFeedback() {
        try {
            setLoading(true);
            setError("");
            setItems(await getAllFeedback());
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to load feedback."));
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadFeedback();
    }, []);

    async function handleRespond(feedbackId: number) {
        const content = drafts[feedbackId]?.trim() ?? "";
        if (!content) {
            setError("Response content is required.");
            setActiveFeedbackId(feedbackId);
            return;
        }

        try {
            setSubmittingId(feedbackId);
            setError("");
            const response = await respondToFeedback(feedbackId, content);
            if (!response.success) {
                setError(response.message || "Unable to submit response.");
                setActiveFeedbackId(feedbackId);
                return;
            }

            setDrafts((current) => ({ ...current, [feedbackId]: "" }));
            setActiveFeedbackId(null);
            await loadFeedback();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to submit response right now."));
            setActiveFeedbackId(feedbackId);
        } finally {
            setSubmittingId(null);
        }
    }

    return (
        <>
            <MainHeader />
            <main className="min-h-screen bg-[linear-gradient(180deg,#f4f7fb_0%,#edf3fb_44%,#f9fbfd_100%)]">
                <div className="mx-auto max-w-screen-xl px-6 py-10">
                    <div className="mb-8 max-w-3xl">
                        <span className="mb-3 inline-flex rounded-full border border-cyan-200 bg-cyan-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-cyan-700">
                            Staff Feedback
                        </span>
                        <h1 className="text-4xl font-extrabold tracking-tight text-slate-950">
                            Review student feedback
                        </h1>
                        <p className="mt-4 text-base leading-7 text-slate-500">
                            Admin and lecturers can respond directly to student feedback with one simple reply stream.
                        </p>
                    </div>

                    {error ? (
                        <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                            {error}
                        </div>
                    ) : null}

                    {loading ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-10 text-sm font-medium text-slate-500 shadow-sm">
                            Loading feedback...
                        </div>
                    ) : items.length === 0 ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-12 text-center shadow-sm">
                            <h2 className="text-2xl font-bold text-slate-900">No feedback submitted yet</h2>
                            <p className="mt-3 text-sm text-slate-500">
                                Student feedback will appear here when it is created.
                            </p>
                        </div>
                    ) : (
                        <div className="space-y-6">
                            {items.map((item) => {
                                const isActive = activeFeedbackId === item.id;
                                const draft = drafts[item.id] ?? "";
                                const isSubmitting = submittingId === item.id;

                                return (
                                    <article
                                        key={item.id}
                                        className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]"
                                    >
                                        <div className="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
                                            <div className="max-w-3xl">
                                                <div className="flex flex-wrap items-center gap-3">
                                                    <span className="rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.22em] text-slate-600">
                                                        {item.studentName}
                                                    </span>
                                                    <span className="text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">
                                                        {item.studentEmail || "No email"}
                                                    </span>
                                                </div>
                                                <h2 className="mt-4 text-2xl font-bold text-slate-950">{item.title}</h2>
                                                <p className="mt-3 text-sm leading-6 text-slate-600">{item.content}</p>
                                            </div>
                                            <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-xs font-semibold uppercase tracking-[0.16em] text-slate-500">
                                                {formatDate(item.createdAt)}
                                            </div>
                                        </div>

                                        <div className="mt-6 space-y-3 border-t border-slate-200 pt-5">
                                            <div className="flex items-center justify-between gap-3">
                                                <h3 className="text-xs font-extrabold uppercase tracking-[0.2em] text-slate-500">
                                                    Responses
                                                </h3>
                                                <button
                                                    type="button"
                                                    onClick={() => setActiveFeedbackId(isActive ? null : item.id)}
                                                    className="rounded-full border border-blue-200 bg-blue-50 px-4 py-2 text-xs font-bold uppercase tracking-[0.16em] text-blue-700 transition hover:border-blue-300 hover:bg-blue-100"
                                                >
                                                    {isActive ? "Cancel" : "Respond"}
                                                </button>
                                            </div>

                                            {item.responses.length === 0 ? (
                                                <p className="text-sm text-slate-400">No responses yet.</p>
                                            ) : (
                                                <div className="space-y-3">
                                                    {item.responses.map((response) => (
                                                        <ResponseCard key={response.id} response={response} />
                                                    ))}
                                                </div>
                                            )}

                                            {isActive ? (
                                                <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                                    <label className="block space-y-2">
                                                        <span className="text-sm font-semibold text-slate-700">Response</span>
                                                        <textarea
                                                            rows={4}
                                                            value={draft}
                                                            onChange={(event) =>
                                                                setDrafts((current) => ({
                                                                    ...current,
                                                                    [item.id]: event.target.value,
                                                                }))
                                                            }
                                                            className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:ring-4 focus:ring-blue-100"
                                                        />
                                                    </label>
                                                    <div className="mt-4 flex justify-end">
                                                        <button
                                                            type="button"
                                                            onClick={() => handleRespond(item.id)}
                                                            disabled={isSubmitting}
                                                            className="inline-flex items-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#0f5bcf_0%,#1d78ec_100%)] px-5 py-3 text-sm font-bold text-white shadow-[0_16px_36px_rgba(29,120,236,0.24)] transition hover:translate-y-[-1px] hover:shadow-[0_18px_40px_rgba(29,120,236,0.3)] disabled:cursor-not-allowed disabled:opacity-65 disabled:shadow-none"
                                                        >
                                                            <span>{isSubmitting ? "Sending..." : "Send response"}</span>
                                                            <span className="material-symbols-outlined text-[18px]">reply</span>
                                                        </button>
                                                    </div>
                                                </div>
                                            ) : null}
                                        </div>
                                    </article>
                                );
                            })}
                        </div>
                    )}
                </div>
            </main>
            <MainFooter />
        </>
    );
}

function ResponseCard({ response }: { response: FeedbackResponse }) {
    return (
        <div className="rounded-2xl border border-cyan-200 bg-cyan-50/40 p-4">
            <div className="mb-2 flex items-center justify-between gap-3">
                <div>
                    <p className="text-sm font-bold text-slate-900">{response.responderEmail}</p>
                    <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-cyan-700">
                        {response.responderRole}
                    </p>
                </div>
                <span className="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">
                    {formatDate(response.createdAt)}
                </span>
            </div>
            <p className="text-sm leading-6 text-slate-600">{response.content}</p>
        </div>
    );
}
