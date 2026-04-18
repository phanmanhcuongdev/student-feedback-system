import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../../../api/apiError";
import { getPendingStudents } from "../../../api/adminApi";
import { getSurveyResults } from "../../../api/surveyResultApi";
import EmptyState from "../../../components/ui/EmptyState";
import ErrorState from "../../../components/ui/ErrorState";
import LoadingState from "../../../components/ui/LoadingState";
import PageHeader from "../../../components/ui/PageHeader";
import SectionCard from "../../../components/ui/SectionCard";
import StatCard from "../../../components/ui/StatCard";
import StatusBadge from "../../../components/ui/StatusBadge";
import type { PendingStudent } from "../../../types/admin";
import type { SurveyResultMetrics, SurveyResultSummary } from "../../../types/surveyResult";

export default function AdminDashboardPage() {
    const [pendingStudents, setPendingStudents] = useState<PendingStudent[]>([]);
    const [surveyResults, setSurveyResults] = useState<SurveyResultSummary[]>([]);
    const [surveyMetrics, setSurveyMetrics] = useState<SurveyResultMetrics>({
        total: 0,
        open: 0,
        closed: 0,
        averageResponseRate: 0,
        totalSubmitted: 0,
        totalResponses: 0,
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    async function load() {
        try {
            setLoading(true);
            setError("");
            const [students, results] = await Promise.all([
                getPendingStudents({ page: 0, size: 4 }),
                getSurveyResults({ page: 0, size: 3, sortBy: "responseCount", sortDir: "desc" }),
            ]);
            setPendingStudents(students.items);
            setSurveyResults(results.items);
            setSurveyMetrics(results.metrics);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to load dashboard data."));
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        void load();
    }, []);

    const recentlyActive = surveyResults.slice(0, 3);

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-xl px-6 py-10">
                <PageHeader
                    eyebrow="Admin Dashboard"
                    title="Pending users and survey stats"
                    description="Review onboarding workload and keep a simple view of survey activity across the system."
                />

                <div className="mt-6">
                    {error ? (
                        <ErrorState description={error} onRetry={() => void load()} />
                    ) : null}

                    {loading ? (
                        <LoadingState label="Loading dashboard..." />
                    ) : (
                        <>
                            <div className="grid gap-5 md:grid-cols-3">
                                <StatCard label="Pending students" value={pendingStudents.length} tone="amber" />
                                <StatCard label="Tracked surveys" value={surveyMetrics.total} tone="sky" />
                                <StatCard label="Total responses" value={surveyMetrics.totalResponses} tone="emerald" />
                            </div>

                            <div className="mt-8 grid gap-6 lg:grid-cols-[0.95fr_1.05fr]">
                                <SectionCard
                                    title="Pending approvals"
                                    description="Latest student accounts waiting for review."
                                    actions={(
                                        <Link
                                            to="/admin/students/pending"
                                            className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-2 text-sm font-bold text-amber-700 transition hover:border-amber-300 hover:bg-amber-100"
                                        >
                                            Review all
                                        </Link>
                                    )}
                                >
                                    {pendingStudents.length === 0 ? (
                                        <EmptyState
                                            title="No pending students"
                                            description="All currently onboarded students have already been reviewed."
                                            icon="task_alt"
                                        />
                                    ) : (
                                        <div className="space-y-4">
                                            {pendingStudents.slice(0, 4).map((student) => (
                                                <article key={student.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
                                                    <div className="flex items-start justify-between gap-4">
                                                        <div>
                                                            <h3 className="text-lg font-bold text-slate-900">{student.name}</h3>
                                                            <p className="mt-1 text-sm text-slate-500">{student.email}</p>
                                                        </div>
                                                        <StatusBadge kind="onboarding" value={student.status} />
                                                    </div>
                                                    <div className="mt-4 grid gap-2 text-sm text-slate-600">
                                                        <p><span className="font-semibold text-slate-500">Student code:</span> {student.studentCode}</p>
                                                        <p><span className="font-semibold text-slate-500">Department:</span> {student.departmentName ?? "Unassigned"}</p>
                                                    </div>
                                                </article>
                                            ))}
                                        </div>
                                    )}
                                </SectionCard>

                                <div className="space-y-6">
                                    <SectionCard title="Survey status" description="Simple activity counters from live result data.">
                                        <div className="grid gap-4 sm:grid-cols-2">
                                            <StatCard label="Open surveys" value={surveyMetrics.open} tone="sky" />
                                            <StatCard
                                                label="Average responses"
                                                value={surveyMetrics.total === 0 ? 0 : Math.round(surveyMetrics.totalResponses / surveyMetrics.total)}
                                                tone="slate"
                                            />
                                        </div>
                                    </SectionCard>

                                    <SectionCard title="Top response volume" description="Surveys with the highest response count.">
                                        {recentlyActive.length === 0 ? (
                                            <EmptyState
                                                title="No survey activity yet"
                                                description="Result activity will appear here after surveys start collecting responses."
                                                icon="bar_chart"
                                            />
                                        ) : (
                                            <div className="space-y-3">
                                                {recentlyActive.map((survey) => (
                                                    <div key={survey.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                                        <div className="flex items-center justify-between gap-4">
                                                            <div>
                                                                <p className="text-sm font-bold text-slate-900">{survey.title}</p>
                                                                <div className="mt-2">
                                                                    <StatusBadge kind="surveyRuntime" value={survey.status} />
                                                                </div>
                                                            </div>
                                                            <span className="text-lg font-extrabold text-slate-950">
                                                                {survey.responseCount}
                                                            </span>
                                                        </div>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </SectionCard>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </main>
    );
}
