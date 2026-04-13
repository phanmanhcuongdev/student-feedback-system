import { useEffect, useState } from "react";
import { createFeedback, getStudentFeedback } from "../../../api/feedbackApi";
import { getApiErrorMessage } from "../../../api/apiError";
import MainFooter from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
import type { FeedbackResponse, StudentFeedback } from "../../../types/feedback";

function formatDate(date: string) {
    return new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    }).format(new Date(date));
}

export default function FeedbackPage() {
    const [title, setTitle] = useState("");
    const [content, setContent] = useState("");
    const [items, setItems] = useState<StudentFeedback[]>([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    async function loadFeedback() {
        try {
            setLoading(true);
            setError("");
            setItems(await getStudentFeedback());
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to load feedback."));
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        loadFeedback();
    }, []);

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setError("");
        setSuccess("");

        if (!title.trim() || !content.trim()) {
            setError("Title and content are required.");
            return;
        }

        setSubmitting(true);
        try {
            const response = await createFeedback(title.trim(), content.trim());
            if (!response.success) {
                setError(response.message || "Unable to submit feedback.");
                return;
            }

            setSuccess(response.message);
            setTitle("");
            setContent("");
            await loadFeedback();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to submit feedback right now."));
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <>
            <MainHeader />
            <main className="min-h-screen bg-[linear-gradient(180deg,#f4f8ff_0%,#eef3f8_44%,#f7fafc_100%)]">
                <div className="mx-auto max-w-screen-lg px-6 py-10">
                    <div className="grid gap-8 lg:grid-cols-[0.95fr_1.05fr]">
                        <section className="rounded-[28px] border border-slate-200 bg-white p-8 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                            <div className="mb-8">
                                <span className="mb-3 inline-flex rounded-full border border-indigo-200 bg-indigo-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-indigo-700">
                                    Student Feedback
                                </span>
                                <h1 className="text-3xl font-extrabold tracking-tight text-slate-950">
                                    Send feedback
                                </h1>
                                <p className="mt-3 text-sm leading-6 text-slate-500">
                                    Share suggestions or report issues. Your submitted feedback stays visible here for reference.
                                </p>
                            </div>

                            <form className="space-y-5" onSubmit={handleSubmit}>
                                <label className="block space-y-2">
                                    <span className="text-sm font-semibold text-slate-700">Title</span>
                                    <input
                                        type="text"
                                        value={title}
                                        onChange={(event) => setTitle(event.target.value)}
                                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3.5 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                                        required
                                    />
                                </label>

                                <label className="block space-y-2">
                                    <span className="text-sm font-semibold text-slate-700">Content</span>
                                    <textarea
                                        rows={6}
                                        value={content}
                                        onChange={(event) => setContent(event.target.value)}
                                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3.5 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                                        required
                                    />
                                </label>

                                {error ? (
                                    <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                                        {error}
                                    </div>
                                ) : null}

                                {success ? (
                                    <div className="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
                                        {success}
                                    </div>
                                ) : null}

                                <button
                                    type="submit"
                                    disabled={submitting}
                                    className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#0f5bcf_0%,#1d78ec_100%)] px-5 py-3.5 text-sm font-bold text-white shadow-[0_16px_36px_rgba(29,120,236,0.28)] transition hover:translate-y-[-1px] hover:shadow-[0_20px_44px_rgba(29,120,236,0.32)] disabled:cursor-not-allowed disabled:opacity-65 disabled:shadow-none"
                                >
                                    <span>{submitting ? "Submitting..." : "Submit feedback"}</span>
                                    <span className="material-symbols-outlined text-base">send</span>
                                </button>
                            </form>
                        </section>

                        <section className="rounded-[28px] border border-slate-200 bg-white p-8 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                            <div className="mb-8">
                                <span className="mb-3 inline-flex rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-slate-700">
                                    History
                                </span>
                                <h2 className="text-3xl font-extrabold tracking-tight text-slate-950">
                                    Your feedback
                                </h2>
                            </div>

                            {loading ? (
                                <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-5 text-sm font-medium text-slate-500">
                                    Loading feedback...
                                </div>
                            ) : items.length === 0 ? (
                                <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-6 text-sm text-slate-500">
                                    You have not submitted any feedback yet.
                                </div>
                            ) : (
                                <div className="space-y-4">
                                    {items.map((item) => (
                                        <article
                                            key={item.id}
                                            className="rounded-2xl border border-slate-200 bg-slate-50 p-5"
                                        >
                                            <div className="mb-3 flex items-start justify-between gap-4">
                                                <h3 className="text-lg font-bold text-slate-900">{item.title}</h3>
                                                <span className="shrink-0 text-xs font-semibold uppercase tracking-[0.16em] text-slate-400">
                                                    {formatDate(item.createdAt)}
                                                </span>
                                            </div>
                                            <p className="text-sm leading-6 text-slate-600">{item.content}</p>
                                            <div className="mt-5 space-y-3 border-t border-slate-200 pt-4">
                                                <div className="flex items-center justify-between gap-3">
                                                    <h4 className="text-xs font-extrabold uppercase tracking-[0.2em] text-slate-500">
                                                        Responses
                                                    </h4>
                                                    <span className="text-xs font-semibold text-slate-400">
                                                        {item.responses.length} {item.responses.length === 1 ? "reply" : "replies"}
                                                    </span>
                                                </div>
                                                {item.responses.length === 0 ? (
                                                    <p className="text-sm text-slate-400">
                                                        No staff response yet.
                                                    </p>
                                                ) : (
                                                    <div className="space-y-3">
                                                        {item.responses.map((response) => (
                                                            <FeedbackResponseCard key={response.id} response={response} />
                                                        ))}
                                                    </div>
                                                )}
                                            </div>
                                        </article>
                                    ))}
                                </div>
                            )}
                        </section>
                    </div>
                </div>
            </main>
            <MainFooter />
        </>
    );
}

function FeedbackResponseCard({ response }: { response: FeedbackResponse }) {
    return (
        <div className="rounded-2xl border border-blue-200 bg-white p-4">
            <div className="mb-2 flex items-center justify-between gap-3">
                <div>
                    <p className="text-sm font-bold text-slate-900">{response.responderEmail}</p>
                    <p className="text-[11px] font-semibold uppercase tracking-[0.18em] text-blue-600">
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
