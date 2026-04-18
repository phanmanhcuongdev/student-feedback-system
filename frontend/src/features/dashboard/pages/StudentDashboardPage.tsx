import { type ReactNode, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../../../api/apiError";
import { getAllSurveys } from "../../../api/surveyApi";
import ErrorState from "../../../components/ui/ErrorState";
import LoadingState from "../../../components/ui/LoadingState";
import type { Survey } from "../../../types/survey";

function formatDate(date: string) {
    return new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
    }).format(new Date(date));
}

export default function StudentDashboardPage() {
    const [surveys, setSurveys] = useState<Survey[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function load() {
            try {
                setLoading(true);
                setError("");
                const response = await getAllSurveys({ page: 0, size: 100, sortBy: "endDate", sortDir: "asc" });
                setSurveys(response.items);
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, "Unable to load dashboard data."));
            } finally {
                setLoading(false);
            }
        }

        void load();
    }, []);

    const openSurveys = surveys.filter((survey) => survey.status === "OPEN");
    const upcomingSurveys = surveys.filter((survey) => survey.status === "NOT_OPEN");
    const closedSurveys = surveys.filter((survey) => survey.status === "CLOSED");
    const closingSoon = openSurveys
        .slice()
        .sort((a, b) => new Date(a.endDate).getTime() - new Date(b.endDate).getTime())
        .slice(0, 3);

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-xl px-6 py-10">
                    <div className="mb-10 max-w-3xl">
                        <span className="mb-3 inline-flex rounded-full border border-blue-200 bg-blue-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-blue-700">
                            Student Dashboard
                        </span>
                        <h1 className="text-4xl font-extrabold tracking-tight text-slate-950">
                            Survey overview
                        </h1>
                        <p className="mt-4 text-base leading-7 text-slate-500">
                            Check what is open now, what is coming next, and which surveys are close to their deadline.
                        </p>
                    </div>

                    {error ? <ErrorState description={error} /> : null}

                    {loading ? (
                        <LoadingState label="Loading dashboard..." />
                    ) : (
                        <>
                            <div className="grid gap-5 md:grid-cols-3">
                                <SummaryCard label="Open surveys" value={openSurveys.length} tone="blue" />
                                <SummaryCard label="Upcoming surveys" value={upcomingSurveys.length} tone="amber" />
                                <SummaryCard label="Closed surveys" value={closedSurveys.length} tone="slate" />
                            </div>

                            <div className="mt-8 grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
                                <section className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                                    <div className="mb-5 flex items-center justify-between gap-4">
                                        <div>
                                            <h2 className="text-2xl font-bold text-slate-950">Available now</h2>
                                            <p className="mt-2 text-sm text-slate-500">
                                                Open surveys you can access immediately.
                                            </p>
                                        </div>
                                        <Link
                                            to="/surveys"
                                            className="rounded-full border border-blue-200 bg-blue-50 px-4 py-2 text-sm font-bold text-blue-700 transition hover:border-blue-300 hover:bg-blue-100"
                                        >
                                            View all surveys
                                        </Link>
                                    </div>

                                    {openSurveys.length === 0 ? (
                                        <p className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-5 text-sm text-slate-500">
                                            No surveys are open right now.
                                        </p>
                                    ) : (
                                        <div className="space-y-4">
                                            {openSurveys.slice(0, 4).map((survey) => (
                                                <article
                                                    key={survey.id}
                                                    className="rounded-2xl border border-slate-200 bg-slate-50 p-5"
                                                >
                                                    <div className="flex items-start justify-between gap-4">
                                                        <div>
                                                            <h3 className="text-lg font-bold text-slate-900">{survey.title}</h3>
                                                            <p className="mt-2 text-sm leading-6 text-slate-600">
                                                                {survey.description || "No survey description provided."}
                                                            </p>
                                                        </div>
                                                        <span className="rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-emerald-700">
                                                            Open
                                                        </span>
                                                    </div>
                                                    <div className="mt-4 flex items-center justify-between gap-4 text-sm text-slate-500">
                                                        <span>Closes {formatDate(survey.endDate)}</span>
                                                        <Link to={`/surveys/${survey.id}`} className="font-bold text-blue-700">
                                                            Open survey
                                                        </Link>
                                                    </div>
                                                </article>
                                            ))}
                                        </div>
                                    )}
                                </section>

                                <section className="space-y-6">
                                    <Panel title="Closing soon" subtitle="Open surveys ordered by nearest deadline.">
                                        {closingSoon.length === 0 ? (
                                            <p className="text-sm text-slate-500">No urgent survey deadlines right now.</p>
                                        ) : (
                                            <div className="space-y-3">
                                                {closingSoon.map((survey) => (
                                                    <div key={survey.id} className="rounded-2xl border border-orange-200 bg-orange-50 p-4">
                                                        <p className="text-sm font-bold text-slate-900">{survey.title}</p>
                                                        <p className="mt-1 text-xs font-semibold uppercase tracking-[0.16em] text-orange-700">
                                                            Ends {formatDate(survey.endDate)}
                                                        </p>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </Panel>

                                    <Panel title="Upcoming surveys" subtitle="Surveys scheduled but not open yet.">
                                        {upcomingSurveys.length === 0 ? (
                                            <p className="text-sm text-slate-500">No upcoming surveys scheduled.</p>
                                        ) : (
                                            <div className="space-y-3">
                                                {upcomingSurveys.slice(0, 3).map((survey) => (
                                                    <div key={survey.id} className="rounded-2xl border border-amber-200 bg-amber-50 p-4">
                                                        <p className="text-sm font-bold text-slate-900">{survey.title}</p>
                                                        <p className="mt-1 text-xs font-semibold uppercase tracking-[0.16em] text-amber-700">
                                                            Starts {formatDate(survey.startDate)}
                                                        </p>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </Panel>
                                </section>
                            </div>
                        </>
                    )}
            </div>
        </main>
    );
}

function SummaryCard({ label, value, tone }: { label: string; value: number; tone: "blue" | "amber" | "slate" }) {
    const toneClassName = {
        blue: "border-blue-200 bg-blue-50 text-blue-700",
        amber: "border-amber-200 bg-amber-50 text-amber-700",
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
