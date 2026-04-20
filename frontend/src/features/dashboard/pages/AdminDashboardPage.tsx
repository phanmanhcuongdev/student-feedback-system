import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../../../api/apiError";
import { getAdminAnalyticsOverview, getPendingStudents, getSurveyManagementDepartments } from "../../../api/adminApi";
import { getSurveyResults } from "../../../api/surveyResultApi";
import EmptyState from "../../../components/ui/EmptyState";
import ErrorState from "../../../components/ui/ErrorState";
import LoadingState from "../../../components/ui/LoadingState";
import PageHeader from "../../../components/ui/PageHeader";
import SectionCard from "../../../components/ui/SectionCard";
import StatCard from "../../../components/ui/StatCard";
import StatusBadge from "../../../components/ui/StatusBadge";
import type { AdminAnalyticsOverview, DepartmentOption, PendingStudent } from "../../../types/admin";
import type { SurveyResultSummary } from "../../../types/surveyResult";

const emptyAnalytics: AdminAnalyticsOverview = {
    metrics: {
        totalSurveys: 0,
        totalDrafts: 0,
        totalPublished: 0,
        totalClosed: 0,
        totalArchived: 0,
        totalHidden: 0,
        totalOpenRuntime: 0,
        totalTargeted: 0,
        totalOpened: 0,
        totalSubmitted: 0,
        averageResponseRate: 0,
    },
    lifecycleCounts: [],
    runtimeCounts: [],
    departmentBreakdown: [],
    attentionSurveys: [],
};

function formatRate(value: number) {
    return `${value.toFixed(1)}%`;
}

export default function AdminDashboardPage() {
    const [pendingStudents, setPendingStudents] = useState<PendingStudent[]>([]);
    const [surveyResults, setSurveyResults] = useState<SurveyResultSummary[]>([]);
    const [analytics, setAnalytics] = useState<AdminAnalyticsOverview>(emptyAnalytics);
    const [departments, setDepartments] = useState<DepartmentOption[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [startDateFrom, setStartDateFrom] = useState("");
    const [endDateTo, setEndDateTo] = useState("");
    const [departmentId, setDepartmentId] = useState("ALL");

    async function load() {
        try {
            setLoading(true);
            setError("");
            const selectedDepartmentId = departmentId === "ALL" ? undefined : Number(departmentId);
            const [students, results, overview, departmentOptions] = await Promise.all([
                getPendingStudents({ page: 0, size: 4 }),
                getSurveyResults({
                    page: 0,
                    size: 3,
                    startDateFrom: startDateFrom || undefined,
                    endDateTo: endDateTo || undefined,
                    sortBy: "responseCount",
                    sortDir: "desc",
                }),
                getAdminAnalyticsOverview({
                    startDateFrom: startDateFrom || undefined,
                    endDateTo: endDateTo || undefined,
                    departmentId: selectedDepartmentId,
                }),
                getSurveyManagementDepartments(),
            ]);
            setPendingStudents(students.items);
            setSurveyResults(results.items);
            setAnalytics(overview);
            setDepartments(departmentOptions);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to load dashboard data."));
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        void load();
    }, [departmentId, endDateTo, startDateFrom]);

    const recentlyActive = surveyResults.slice(0, 3);
    const metrics = analytics.metrics;

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-xl px-6 py-10">
                <PageHeader
                    eyebrow="Admin Dashboard"
                    title="Survey health and operations"
                    description="Track onboarding workload, survey lifecycle volume, participation health, and privileged activity entry points."
                />

                <div className="mt-6">
                    <div className="mb-6 flex flex-wrap gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
                        <label className="flex min-w-[170px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700">
                            <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">Start</span>
                            <input type="date" value={startDateFrom} onChange={(event) => setStartDateFrom(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                        </label>
                        <label className="flex min-w-[170px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700">
                            <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">End</span>
                            <input type="date" value={endDateTo} onChange={(event) => setEndDateTo(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                        </label>
                        <label className="flex min-w-[220px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700">
                            <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">Dept</span>
                            <select value={departmentId} onChange={(event) => setDepartmentId(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none">
                                <option value="ALL">All departments</option>
                                {departments.map((department) => (
                                    <option key={department.id} value={department.id}>{department.name}</option>
                                ))}
                            </select>
                        </label>
                    </div>

                    {error ? (
                        <ErrorState description={error} onRetry={() => void load()} />
                    ) : null}

                    {loading ? (
                        <LoadingState label="Loading dashboard..." />
                    ) : (
                        <>
                            <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
                                <StatCard label="Pending students" value={pendingStudents.length} tone="amber" />
                                <StatCard label="Total surveys" value={metrics.totalSurveys} tone="sky" />
                                <StatCard label="Open surveys" value={metrics.totalOpenRuntime} tone="emerald" />
                                <StatCard label="Response rate" value={formatRate(metrics.averageResponseRate)} tone="slate" />
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
                                    <SectionCard title="Lifecycle overview" description="Counts from active survey lifecycle and visibility state.">
                                        <div className="grid gap-4 sm:grid-cols-2">
                                            <StatCard label="Draft" value={metrics.totalDrafts} tone="slate" />
                                            <StatCard label="Published" value={metrics.totalPublished} tone="sky" />
                                            <StatCard label="Closed" value={metrics.totalClosed} tone="amber" />
                                            <StatCard label="Archived" value={metrics.totalArchived} tone="slate" />
                                        </div>
                                    </SectionCard>

                                    <SectionCard title="Top response volume" description={departmentId === "ALL" ? "Surveys with the highest response count in the selected date window." : "System-wide response volume for the selected date window; department-specific participation appears below."}>
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

                            <div className="mt-8 grid gap-6 lg:grid-cols-2">
                                <SectionCard title="Participation health" description="Opened and submitted counts from recipient tracking.">
                                    <div className="grid gap-4 sm:grid-cols-3">
                                        <StatCard label="Targeted" value={metrics.totalTargeted} tone="slate" />
                                        <StatCard label="Opened" value={metrics.totalOpened} tone="sky" />
                                        <StatCard label="Submitted" value={metrics.totalSubmitted} tone="emerald" />
                                    </div>
                                    <div className="mt-5 space-y-3">
                                        {analytics.departmentBreakdown.slice(0, 5).map((department) => (
                                            <div key={department.departmentId ?? "all"} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                                <div className="flex items-center justify-between gap-4">
                                                    <div>
                                                        <p className="font-bold text-slate-900">{department.departmentName}</p>
                                                        <p className="mt-1 text-sm text-slate-500">{department.submittedCount} of {department.targetedCount} submitted</p>
                                                    </div>
                                                    <span className="text-sm font-extrabold text-slate-950">{formatRate(department.responseRate)}</span>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </SectionCard>

                                <SectionCard
                                    title="Surveys needing attention"
                                    description="Open surveys with remaining recipients, sorted by lowest response rate."
                                    actions={(
                                        <Link
                                            to="/survey-results"
                                            className="rounded-xl border border-slate-300 bg-white px-4 py-2 text-sm font-bold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50"
                                        >
                                            Open results
                                        </Link>
                                    )}
                                >
                                    {analytics.attentionSurveys.length === 0 ? (
                                        <EmptyState
                                            title="No open participation gaps"
                                            description="Open surveys with incomplete participation will appear here."
                                            icon="task_alt"
                                        />
                                    ) : (
                                        <div className="space-y-3">
                                            {analytics.attentionSurveys.map((survey) => (
                                                <div key={survey.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                                    <div className="flex items-start justify-between gap-4">
                                                        <div>
                                                            <p className="font-bold text-slate-900">{survey.title}</p>
                                                            <p className="mt-1 text-sm text-slate-500">{survey.departmentName ?? "All students"} - {survey.submittedCount} of {survey.targetedCount} submitted</p>
                                                        </div>
                                                        <span className="text-sm font-extrabold text-amber-700">{formatRate(survey.responseRate)}</span>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </SectionCard>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </main>
    );
}
