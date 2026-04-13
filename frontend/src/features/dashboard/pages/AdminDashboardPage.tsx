import { type ReactNode, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../../../api/apiError";
import { getPendingStudents } from "../../../api/adminApi";
import { getSurveyResults } from "../../../api/surveyResultApi";
import MainFooter from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
import type { PendingStudent } from "../../../types/admin";
import type { SurveyResultSummary } from "../../../types/surveyResult";

export default function AdminDashboardPage() {
    const [pendingStudents, setPendingStudents] = useState<PendingStudent[]>([]);
    const [surveyResults, setSurveyResults] = useState<SurveyResultSummary[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function load() {
            try {
                setLoading(true);
                setError("");
                const [students, results] = await Promise.all([getPendingStudents(), getSurveyResults()]);
                setPendingStudents(students);
                setSurveyResults(results);
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, "Unable to load dashboard data."));
            } finally {
                setLoading(false);
            }
        }

        load();
    }, []);

    const totalResponses = surveyResults.reduce((sum, survey) => sum + survey.responseCount, 0);
    const openSurveys = surveyResults.filter((survey) => survey.status === "OPEN");
    const recentlyActive = surveyResults.slice().sort((a, b) => b.responseCount - a.responseCount).slice(0, 3);

    return (
        <>
            <MainHeader />
            <main className="min-h-screen bg-[linear-gradient(180deg,#fbfaf4_0%,#f3f2eb_44%,#faf8f1_100%)]">
                <div className="mx-auto max-w-screen-xl px-6 py-10">
                    <div className="mb-10 max-w-3xl">
                        <span className="mb-3 inline-flex rounded-full border border-amber-200 bg-amber-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-amber-700">
                            Admin Dashboard
                        </span>
                        <h1 className="text-4xl font-extrabold tracking-tight text-slate-950">
                            Pending users and survey stats
                        </h1>
                        <p className="mt-4 text-base leading-7 text-slate-500">
                            Review onboarding workload and keep a simple view of survey activity across the system.
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
                                <Metric label="Pending students" value={pendingStudents.length} tone="amber" />
                                <Metric label="Tracked surveys" value={surveyResults.length} tone="sky" />
                                <Metric label="Total responses" value={totalResponses} tone="emerald" />
                            </div>

                            <div className="mt-8 grid gap-6 lg:grid-cols-[0.95fr_1.05fr]">
                                <section className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                                    <div className="mb-5 flex items-center justify-between gap-4">
                                        <div>
                                            <h2 className="text-2xl font-bold text-slate-950">Pending approvals</h2>
                                            <p className="mt-2 text-sm text-slate-500">
                                                Latest student accounts waiting for review.
                                            </p>
                                        </div>
                                        <Link
                                            to="/admin/students/pending"
                                            className="rounded-full border border-amber-200 bg-amber-50 px-4 py-2 text-sm font-bold text-amber-700 transition hover:border-amber-300 hover:bg-amber-100"
                                        >
                                            Review all
                                        </Link>
                                    </div>

                                    {pendingStudents.length === 0 ? (
                                        <p className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-5 text-sm text-slate-500">
                                            No student approvals are waiting right now.
                                        </p>
                                    ) : (
                                        <div className="space-y-4">
                                            {pendingStudents.slice(0, 4).map((student) => (
                                                <article key={student.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
                                                    <div className="flex items-start justify-between gap-4">
                                                        <div>
                                                            <h3 className="text-lg font-bold text-slate-900">{student.name}</h3>
                                                            <p className="mt-1 text-sm text-slate-500">{student.email}</p>
                                                        </div>
                                                        <span className="rounded-full border border-amber-200 bg-amber-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-amber-700">
                                                            {student.status}
                                                        </span>
                                                    </div>
                                                    <div className="mt-4 grid gap-2 text-sm text-slate-600">
                                                        <p><span className="font-semibold text-slate-500">Student code:</span> {student.studentCode}</p>
                                                        <p><span className="font-semibold text-slate-500">Department:</span> {student.departmentName ?? "Unassigned"}</p>
                                                    </div>
                                                </article>
                                            ))}
                                        </div>
                                    )}
                                </section>

                                <section className="space-y-6">
                                    <Panel title="Survey status" subtitle="Simple activity counters from live result data.">
                                        <div className="grid gap-4 sm:grid-cols-2">
                                            <MiniStat label="Open surveys" value={openSurveys.length} />
                                            <MiniStat label="Average responses" value={surveyResults.length === 0 ? 0 : Math.round(totalResponses / surveyResults.length)} />
                                        </div>
                                    </Panel>

                                    <Panel title="Top response volume" subtitle="Surveys with the highest response count.">
                                        {recentlyActive.length === 0 ? (
                                            <p className="text-sm text-slate-500">No survey activity yet.</p>
                                        ) : (
                                            <div className="space-y-3">
                                                {recentlyActive.map((survey) => (
                                                    <div key={survey.id} className="rounded-2xl border border-sky-200 bg-sky-50 p-4">
                                                        <div className="flex items-center justify-between gap-4">
                                                            <div>
                                                                <p className="text-sm font-bold text-slate-900">{survey.title}</p>
                                                                <p className="mt-1 text-xs font-semibold uppercase tracking-[0.16em] text-sky-700">
                                                                    {survey.status}
                                                                </p>
                                                            </div>
                                                            <span className="text-lg font-extrabold text-slate-950">
                                                                {survey.responseCount}
                                                            </span>
                                                        </div>
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
            <MainFooter />
        </>
    );
}

function Metric({ label, value, tone }: { label: string; value: number; tone: "amber" | "sky" | "emerald" }) {
    const toneClassName = {
        amber: "border-amber-200 bg-amber-50 text-amber-700",
        sky: "border-sky-200 bg-sky-50 text-sky-700",
        emerald: "border-emerald-200 bg-emerald-50 text-emerald-700",
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

function MiniStat({ label, value }: { label: string; value: number }) {
    return (
        <div className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
            <p className="text-3xl font-extrabold tracking-tight text-slate-950">{value}</p>
            <p className="mt-2 text-sm font-medium text-slate-500">{label}</p>
        </div>
    );
}
