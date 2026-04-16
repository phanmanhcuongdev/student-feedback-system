import { type ReactNode, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../../../api/apiError";
import { getSurveyResults } from "../../../api/surveyResultApi";
import type { SurveyResultSummary } from "../../../types/surveyResult";

export default function LecturerDashboardPage() {
    const [results, setResults] = useState<SurveyResultSummary[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function load() {
            try {
                setLoading(true);
                setError("");
                setResults(await getSurveyResults());
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, "Unable to load dashboard data."));
            } finally {
                setLoading(false);
            }
        }

        load();
    }, []);

    const totalResponses = results.reduce((sum, item) => sum + item.responseCount, 0);
    const openResults = results.filter((item) => item.status === "OPEN");
    const closedResults = results.filter((item) => item.status === "CLOSED");
    const topResponseSurveys = results.slice().sort((a, b) => b.responseCount - a.responseCount).slice(0, 4);

    return (
        <main className="bg-[linear-gradient(180deg,#f5fbff_0%,#eef6fb_45%,#f8fbfd_100%)]">
            <div className="mx-auto max-w-screen-xl px-6 py-10">
                    <div className="mb-10 max-w-3xl">
                        <span className="mb-3 inline-flex rounded-full border border-sky-200 bg-sky-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-sky-700">
                            Lecturer Dashboard
                        </span>
                        <h1 className="text-4xl font-extrabold tracking-tight text-slate-950">
                            Results overview
                        </h1>
                        <p className="mt-4 text-base leading-7 text-slate-500">
                            Review survey activity that is visible within your department scope and jump directly into the most active result sets.
                        </p>
                    </div>

                    {error ? (
                        <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                            {error}
                        </div>
                    ) : null}

                    {loading ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-10 text-sm font-medium text-slate-500 shadow-sm">
                            Loading dashboard...
                        </div>
                    ) : (
                        <>
                            <div className="grid gap-5 md:grid-cols-3">
                                <Metric label="Tracked surveys" value={results.length} tone="sky" />
                                <Metric label="Total responses" value={totalResponses} tone="emerald" />
                                <Metric label="Closed surveys" value={closedResults.length} tone="slate" />
                            </div>

                            <div className="mt-8 grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
                                <section className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                                    <div className="mb-5 flex items-center justify-between gap-4">
                                        <div>
                                            <h2 className="text-2xl font-bold text-slate-950">Most active surveys</h2>
                                            <p className="mt-2 text-sm text-slate-500">
                                                Surveys ordered by current response volume.
                                            </p>
                                        </div>
                                        <Link
                                            to="/survey-results"
                                            className="rounded-full border border-sky-200 bg-sky-50 px-4 py-2 text-sm font-bold text-sky-700 transition hover:border-sky-300 hover:bg-sky-100"
                                        >
                                            Open result list
                                        </Link>
                                    </div>

                                    {topResponseSurveys.length === 0 ? (
                                        <p className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-5 text-sm text-slate-500">
                                            No survey results are available yet.
                                        </p>
                                    ) : (
                                        <div className="space-y-4">
                                            {topResponseSurveys.map((survey) => (
                                                <article key={survey.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
                                                    <div className="flex items-start justify-between gap-4">
                                                        <div>
                                                            <h3 className="text-lg font-bold text-slate-900">{survey.title}</h3>
                                                            <p className="mt-2 text-sm leading-6 text-slate-600">
                                                                {survey.description || "No survey description provided."}
                                                            </p>
                                                        </div>
                                                        <span className="rounded-full border border-slate-200 bg-white px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-slate-700">
                                                            {survey.status}
                                                        </span>
                                                    </div>
                                                    <div className="mt-4 flex items-center justify-between gap-4 text-sm text-slate-500">
                                                        <span>{survey.responseCount} response{survey.responseCount === 1 ? "" : "s"}</span>
                                                        <Link to={`/survey-results/${survey.id}`} className="font-bold text-sky-700">
                                                            Open statistics
                                                        </Link>
                                                    </div>
                                                </article>
                                            ))}
                                        </div>
                                    )}
                                </section>

                                <section className="space-y-6">
                                    <Panel title="Open surveys" subtitle="Result entries that are still collecting responses.">
                                        <StatText value={openResults.length} label="survey runs still open" />
                                    </Panel>
                                    <Panel title="Coverage" subtitle="Average response count across all visible surveys.">
                                        <StatText
                                            value={results.length === 0 ? 0 : Math.round(totalResponses / results.length)}
                                            label="responses per survey"
                                        />
                                    </Panel>
                                </section>
                            </div>
                        </>
                    )}
            </div>
        </main>
    );
}

function Metric({ label, value, tone }: { label: string; value: number; tone: "sky" | "emerald" | "slate" }) {
    const toneClassName = {
        sky: "border-sky-200 bg-sky-50 text-sky-700",
        emerald: "border-emerald-200 bg-emerald-50 text-emerald-700",
        slate: "border-slate-200 bg-slate-50 text-slate-700",
    }[tone];

    return (
        <div className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
            <span className={`inline-flex rounded-full border px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] ${toneClassName}`}>
                {label}
            </span>
            <p className="mt-5 text-4xl font-extrabold tracking-tight text-slate-950">{value}</p>
        </div>
    );
}

function Panel({ title, subtitle, children }: { title: string; subtitle: string; children: ReactNode }) {
    return (
        <section className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
            <h2 className="text-2xl font-bold text-slate-950">{title}</h2>
            <p className="mt-2 text-sm text-slate-500">{subtitle}</p>
            <div className="mt-5">{children}</div>
        </section>
    );
}

function StatText({ value, label }: { value: number; label: string }) {
    return (
        <div className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
            <p className="text-4xl font-extrabold tracking-tight text-slate-950">{value}</p>
            <p className="mt-2 text-sm font-medium text-slate-500">{label}</p>
        </div>
    );
}
