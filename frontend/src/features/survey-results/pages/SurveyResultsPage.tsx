import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../../../api/apiError";
import { getSurveyResults } from "../../../api/surveyResultApi";
import MainFooter from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
import type { SurveyResultSummary } from "../../../types/surveyResult";

function formatDateRange(startDate: string, endDate: string) {
    const formatter = new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
    });

    return `${formatter.format(new Date(startDate))} - ${formatter.format(new Date(endDate))}`;
}

export default function SurveyResultsPage() {
    const [surveys, setSurveys] = useState<SurveyResultSummary[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function fetchSurveyResults() {
            try {
                setLoading(true);
                setError("");
                setSurveys(await getSurveyResults());
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, "Unable to load survey results."));
            } finally {
                setLoading(false);
            }
        }

        fetchSurveyResults();
    }, []);

    return (
        <>
            <MainHeader />

            <main className="min-h-screen bg-[linear-gradient(180deg,#f3f6ff_0%,#edf3fb_46%,#f8fbff_100%)]">
                <div className="mx-auto max-w-screen-xl px-6 py-10">
                    <div className="mb-10 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
                        <div className="max-w-3xl">
                            <span className="mb-3 inline-flex rounded-full border border-blue-200 bg-blue-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-blue-700">
                                Result Review
                            </span>
                            <h1 className="text-4xl font-extrabold tracking-tight text-slate-950">
                                Survey statistics
                            </h1>
                            <p className="mt-4 text-base leading-7 text-slate-500">
                                Review response volume, rating distributions, and qualitative comments for completed survey runs.
                            </p>
                        </div>

                        <div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm">
                            {surveys.length} survey result{surveys.length === 1 ? "" : "s"}
                        </div>
                    </div>

                    {error ? (
                        <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                            {error}
                        </div>
                    ) : null}

                    {loading ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-10 text-center text-sm font-medium text-slate-500 shadow-sm">
                            Loading survey results...
                        </div>
                    ) : surveys.length === 0 ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-12 text-center shadow-sm">
                            <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-slate-100 text-slate-500">
                                <span className="material-symbols-outlined text-[30px]">bar_chart</span>
                            </div>
                            <h2 className="text-2xl font-bold text-slate-900">No survey results yet</h2>
                            <p className="mt-3 text-sm text-slate-500">
                                Results will appear here after surveys start receiving student responses.
                            </p>
                        </div>
                    ) : (
                        <div className="grid gap-6 lg:grid-cols-2">
                            {surveys.map((survey) => (
                                <article
                                    key={survey.id}
                                    className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]"
                                >
                                    <div className="mb-5 flex items-start justify-between gap-4">
                                        <div>
                                            <span className="inline-flex rounded-full border border-blue-200 bg-blue-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.22em] text-blue-700">
                                                {survey.status}
                                            </span>
                                            <h2 className="mt-3 text-2xl font-bold text-slate-950">{survey.title}</h2>
                                            <p className="mt-3 text-sm leading-6 text-slate-500">
                                                {survey.description || "No survey description provided."}
                                            </p>
                                        </div>
                                        <div className="rounded-2xl bg-slate-100 px-3 py-2 text-xs font-semibold uppercase tracking-[0.16em] text-slate-500">
                                            ID {survey.id}
                                        </div>
                                    </div>

                                    <div className="grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                        <div className="flex items-center justify-between gap-4">
                                            <span className="font-semibold text-slate-500">Response count</span>
                                            <span className="text-right text-lg font-bold text-slate-900">{survey.responseCount}</span>
                                        </div>
                                        <div className="flex items-center justify-between gap-4">
                                            <span className="font-semibold text-slate-500">Window</span>
                                            <span className="text-right font-medium text-slate-900">
                                                {formatDateRange(survey.startDate, survey.endDate)}
                                            </span>
                                        </div>
                                    </div>

                                    <div className="mt-6">
                                        <Link
                                            to={`/survey-results/${survey.id}`}
                                            className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#0f5bcf_0%,#1d78ec_100%)] px-4 py-3 text-sm font-bold text-white shadow-[0_16px_36px_rgba(29,120,236,0.24)] transition hover:translate-y-[-1px] hover:shadow-[0_18px_40px_rgba(29,120,236,0.3)]"
                                        >
                                            <span>Open statistics</span>
                                            <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
                                        </Link>
                                    </div>
                                </article>
                            ))}
                        </div>
                    )}
                </div>
            </main>

            <MainFooter />
        </>
    );
}
