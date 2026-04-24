import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
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
    const { i18n, t } = useTranslation("admin");
    const [pendingStudents, setPendingStudents] = useState<PendingStudent[]>([]);
    const [surveyResults, setSurveyResults] = useState<SurveyResultSummary[]>([]);
    const [analytics, setAnalytics] = useState<AdminAnalyticsOverview>(emptyAnalytics);
    const [departments, setDepartments] = useState<DepartmentOption[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [startDateFrom, setStartDateFrom] = useState("");
    const [endDateTo, setEndDateTo] = useState("");
    const [startDateInputType, setStartDateInputType] = useState<"text" | "date">("text");
    const [endDateInputType, setEndDateInputType] = useState<"text" | "date">("text");
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
            setError(getApiErrorMessage(requestError, t("admin.dashboard.errors.loadFailed")));
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {
        void load();
    }, [departmentId, endDateTo, i18n.resolvedLanguage, startDateFrom]);

    const recentlyActive = surveyResults.slice(0, 3);
    const metrics = analytics.metrics;

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-xl px-6 py-10">
                <PageHeader
                    eyebrow={t("admin.dashboard.header.eyebrow")}
                    title={t("admin.dashboard.header.title")}
                    description={t("admin.dashboard.header.description")}
                />

                <div className="mt-6">
                    <div className="mb-6 flex flex-wrap gap-3 rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
                        <label className="flex min-w-[170px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700">
                            <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">{t("admin.dashboard.filters.start")}</span>
                            <input
                                type={startDateFrom ? "date" : startDateInputType}
                                value={startDateFrom}
                                onChange={(event) => setStartDateFrom(event.target.value)}
                                onFocus={() => setStartDateInputType("date")}
                                onBlur={() => {
                                    if (!startDateFrom) {
                                        setStartDateInputType("text");
                                    }
                                }}
                                placeholder={t("admin.dashboard.filters.datePlaceholder")}
                                aria-label={t("admin.dashboard.filters.startDate")}
                                className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none placeholder:text-slate-400"
                            />
                        </label>
                        <label className="flex min-w-[170px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700">
                            <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">{t("admin.dashboard.filters.end")}</span>
                            <input
                                type={endDateTo ? "date" : endDateInputType}
                                value={endDateTo}
                                onChange={(event) => setEndDateTo(event.target.value)}
                                onFocus={() => setEndDateInputType("date")}
                                onBlur={() => {
                                    if (!endDateTo) {
                                        setEndDateInputType("text");
                                    }
                                }}
                                placeholder={t("admin.dashboard.filters.datePlaceholder")}
                                aria-label={t("admin.dashboard.filters.endDate")}
                                className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none placeholder:text-slate-400"
                            />
                        </label>
                        <label className="flex min-w-[220px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700">
                            <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">{t("admin.dashboard.filters.department")}</span>
                            <select value={departmentId} onChange={(event) => setDepartmentId(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none">
                                <option value="ALL">{t("admin.dashboard.filters.allDepartments")}</option>
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
                        <LoadingState label={t("admin.dashboard.loading")} />
                    ) : (
                        <>
                            <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-4">
                                <StatCard label={t("admin.dashboard.stats.pendingStudents")} value={pendingStudents.length} tone="amber" />
                                <StatCard label={t("admin.dashboard.stats.totalSurveys")} value={metrics.totalSurveys} tone="sky" />
                                <StatCard label={t("admin.dashboard.stats.openSurveys")} value={metrics.totalOpenRuntime} tone="emerald" />
                                <StatCard label={t("admin.dashboard.stats.responseRate")} value={formatRate(metrics.averageResponseRate)} tone="slate" />
                            </div>

                            <div className="mt-8 grid gap-6 lg:grid-cols-[0.95fr_1.05fr]">
                                <SectionCard
                                    title={t("admin.dashboard.pendingApprovals.title")}
                                    description={t("admin.dashboard.pendingApprovals.description")}
                                    actions={(
                                        <Link
                                            to="/admin/students/pending"
                                            className="rounded-xl border border-amber-200 bg-amber-50 px-4 py-2 text-sm font-bold text-amber-700 transition hover:border-amber-300 hover:bg-amber-100"
                                        >
                                            {t("admin.dashboard.pendingApprovals.reviewAll")}
                                        </Link>
                                    )}
                                >
                                    {pendingStudents.length === 0 ? (
                                        <EmptyState
                                            title={t("admin.dashboard.pendingApprovals.emptyTitle")}
                                            description={t("admin.dashboard.pendingApprovals.emptyDescription")}
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
                                                        <p><span className="font-semibold text-slate-500">{t("admin.dashboard.pendingApprovals.studentCode")}:</span> {student.studentCode}</p>
                                                        <p><span className="font-semibold text-slate-500">{t("admin.dashboard.pendingApprovals.department")}:</span> {student.departmentName ?? t("admin.dashboard.common.unassigned")}</p>
                                                    </div>
                                                </article>
                                            ))}
                                        </div>
                                    )}
                                </SectionCard>

                                <div className="space-y-6">
                                    <SectionCard title={t("admin.dashboard.lifecycle.title")} description={t("admin.dashboard.lifecycle.description")}>
                                        <div className="grid gap-4 sm:grid-cols-2">
                                            <StatCard label={t("admin.dashboard.lifecycle.draft")} value={metrics.totalDrafts} tone="slate" />
                                            <StatCard label={t("admin.dashboard.lifecycle.published")} value={metrics.totalPublished} tone="sky" />
                                            <StatCard label={t("admin.dashboard.lifecycle.closed")} value={metrics.totalClosed} tone="amber" />
                                            <StatCard label={t("admin.dashboard.lifecycle.archived")} value={metrics.totalArchived} tone="slate" />
                                        </div>
                                    </SectionCard>

                                    <SectionCard
                                        title={t("admin.dashboard.topResponses.title")}
                                        description={departmentId === "ALL"
                                            ? t("admin.dashboard.topResponses.descriptionAll")
                                            : t("admin.dashboard.topResponses.descriptionDepartment")}
                                    >
                                        {recentlyActive.length === 0 ? (
                                            <EmptyState
                                                title={t("admin.dashboard.topResponses.emptyTitle")}
                                                description={t("admin.dashboard.topResponses.emptyDescription")}
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
                                <SectionCard title={t("admin.dashboard.participation.title")} description={t("admin.dashboard.participation.description")}>
                                    <div className="grid gap-4 sm:grid-cols-3">
                                        <StatCard label={t("admin.dashboard.participation.targeted")} value={metrics.totalTargeted} tone="slate" />
                                        <StatCard label={t("admin.dashboard.participation.opened")} value={metrics.totalOpened} tone="sky" />
                                        <StatCard label={t("admin.dashboard.participation.submitted")} value={metrics.totalSubmitted} tone="emerald" />
                                    </div>
                                    <div className="mt-5 space-y-3">
                                        {analytics.departmentBreakdown.slice(0, 5).map((department) => (
                                            <div key={department.departmentId ?? "all"} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                                <div className="flex items-center justify-between gap-4">
                                                    <div>
                                                        <p className="font-bold text-slate-900">{department.departmentName}</p>
                                                        <p className="mt-1 text-sm text-slate-500">
                                                            {t("admin.dashboard.participation.submittedCount", {
                                                                submitted: department.submittedCount,
                                                                targeted: department.targetedCount,
                                                            })}
                                                        </p>
                                                    </div>
                                                    <span className="text-sm font-extrabold text-slate-950">{formatRate(department.responseRate)}</span>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </SectionCard>

                                <SectionCard
                                    title={t("admin.dashboard.attention.title")}
                                    description={t("admin.dashboard.attention.description")}
                                    actions={(
                                        <Link
                                            to="/survey-results"
                                            className="rounded-xl border border-slate-300 bg-white px-4 py-2 text-sm font-bold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50"
                                        >
                                            {t("admin.dashboard.attention.openResults")}
                                        </Link>
                                    )}
                                >
                                    {analytics.attentionSurveys.length === 0 ? (
                                        <EmptyState
                                            title={t("admin.dashboard.attention.emptyTitle")}
                                            description={t("admin.dashboard.attention.emptyDescription")}
                                            icon="task_alt"
                                        />
                                    ) : (
                                        <div className="space-y-3">
                                            {analytics.attentionSurveys.map((survey) => (
                                                <div key={survey.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                                    <div className="flex items-start justify-between gap-4">
                                                        <div>
                                                            <p className="font-bold text-slate-900">{survey.title}</p>
                                                            <p className="mt-1 text-sm text-slate-500">
                                                                {t("admin.dashboard.attention.surveyProgress", {
                                                                    department: survey.departmentName ?? t("admin.dashboard.common.allStudents"),
                                                                    submitted: survey.submittedCount,
                                                                    targeted: survey.targetedCount,
                                                                })}
                                                            </p>
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
