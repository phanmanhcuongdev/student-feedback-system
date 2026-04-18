import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getApiErrorMessage } from "../../../api/apiError";
import { getSurveyResults } from "../../../api/surveyResultApi";
import DataTable, { type DataTableColumn } from "../../../components/data-view/DataTable";
import DataToolbar from "../../../components/data-view/DataToolbar";
import PaginationControls from "../../../components/data-view/PaginationControls";
import ResponsiveDataList from "../../../components/data-view/ResponsiveDataList";
import SearchInput from "../../../components/data-view/SearchInput";
import SelectFilter from "../../../components/data-view/SelectFilter";
import EmptyState from "../../../components/ui/EmptyState";
import ErrorState from "../../../components/ui/ErrorState";
import LoadingState from "../../../components/ui/LoadingState";
import PageHeader from "../../../components/ui/PageHeader";
import StatCard from "../../../components/ui/StatCard";
import StatusBadge from "../../../components/ui/StatusBadge";
import { useAuth } from "../../auth/useAuth";
import type { SurveyResultMetrics, SurveyResultSummary } from "../../../types/surveyResult";

function formatDate(date: string) {
    return new Intl.DateTimeFormat("en-GB", { day: "2-digit", month: "short", year: "numeric" }).format(new Date(date));
}

function formatDateRange(startDate: string, endDate: string) {
    return `${formatDate(startDate)} - ${formatDate(endDate)}`;
}

function formatRate(value: number) {
    return `${value.toFixed(1)}%`;
}

function getAudienceLabel(survey: SurveyResultSummary) {
    if (survey.recipientScope === "DEPARTMENT") {
        return survey.recipientDepartmentName || "Department";
    }
    return "All students";
}

export default function SurveyResultsPage() {
    const { session } = useAuth();
    const [surveys, setSurveys] = useState<SurveyResultSummary[]>([]);
    const [metrics, setMetrics] = useState<SurveyResultMetrics>({
        total: 0,
        open: 0,
        closed: 0,
        averageResponseRate: 0,
        totalSubmitted: 0,
        totalResponses: 0,
    });
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [query, setQuery] = useState("");
    const [debouncedQuery, setDebouncedQuery] = useState("");
    const [lifecycleFilter, setLifecycleFilter] = useState("ALL");
    const [runtimeFilter, setRuntimeFilter] = useState("ALL");
    const [audienceFilter, setAudienceFilter] = useState("ALL");
    const [startDateFrom, setStartDateFrom] = useState("");
    const [endDateTo, setEndDateTo] = useState("");
    const [sortBy, setSortBy] = useState("responseRate");
    const [sortDir, setSortDir] = useState("desc");
    const [page, setPage] = useState(0);

    const pageSize = 12;

    useEffect(() => {
        const timeout = window.setTimeout(() => {
            setDebouncedQuery(query.trim());
        }, 300);

        return () => window.clearTimeout(timeout);
    }, [query]);

    const loadSurveyResults = useCallback(async () => {
        try {
            setLoading(true);
            setError("");
            const response = await getSurveyResults({
                keyword: debouncedQuery || undefined,
                lifecycleState: lifecycleFilter === "ALL" ? undefined : lifecycleFilter,
                runtimeStatus: runtimeFilter === "ALL" ? undefined : runtimeFilter,
                recipientScope: audienceFilter === "ALL" ? undefined : audienceFilter,
                startDateFrom: startDateFrom || undefined,
                endDateTo: endDateTo || undefined,
                page,
                size: pageSize,
                sortBy,
                sortDir,
            });
            if (response.items.length === 0 && response.totalPages > 0 && page >= response.totalPages) {
                setPage(response.totalPages - 1);
                return;
            }
            setSurveys(response.items);
            setMetrics(response.metrics);
            setTotalElements(response.totalElements);
            setTotalPages(response.totalPages);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to load survey results."));
        } finally {
            setLoading(false);
        }
    }, [audienceFilter, debouncedQuery, endDateTo, lifecycleFilter, page, runtimeFilter, sortBy, sortDir, startDateFrom]);

    useEffect(() => {
        void loadSurveyResults();
    }, [loadSurveyResults]);

    useEffect(() => {
        setPage(0);
    }, [audienceFilter, debouncedQuery, endDateTo, lifecycleFilter, runtimeFilter, sortBy, sortDir, startDateFrom]);

    const columns: DataTableColumn<SurveyResultSummary>[] = [
        {
            key: "survey",
            header: "Survey",
            render: (survey) => (
                <div>
                    <p className="font-bold text-slate-950">{survey.title}</p>
                    <p className="mt-1 line-clamp-2 max-w-md text-sm text-slate-500">{survey.description || "No survey description provided."}</p>
                </div>
            ),
        },
        {
            key: "status",
            header: "Status",
            render: (survey) => (
                <div className="flex flex-wrap gap-2">
                    <StatusBadge kind="surveyLifecycle" value={survey.lifecycleState} />
                    <StatusBadge kind="surveyRuntime" value={survey.runtimeStatus} />
                </div>
            ),
        },
        {
            key: "audience",
            header: "Audience",
            render: (survey) => getAudienceLabel(survey),
        },
        {
            key: "targeted",
            header: "Targeted",
            render: (survey) => survey.targetedCount,
        },
        {
            key: "submitted",
            header: "Submitted",
            render: (survey) => survey.submittedCount,
        },
        {
            key: "rate",
            header: "Response rate",
            render: (survey) => formatRate(survey.responseRate),
        },
        {
            key: "window",
            header: "Window",
            render: (survey) => formatDateRange(survey.startDate, survey.endDate),
        },
        {
            key: "actions",
            header: "Actions",
            className: "text-right",
            render: (survey) => (
                <div className="flex justify-end">
                    <Link to={`/survey-results/${survey.id}`} className="inline-flex items-center rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                        View details
                    </Link>
                </div>
            ),
        },
    ];

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-2xl px-6 py-10">
                <PageHeader
                    eyebrow="Result Review"
                    title="Survey statistics"
                    description="Review participation and question-level outcomes through an operational results list instead of a visual card wall."
                    actions={<div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm">{totalElements} matching survey result{totalElements === 1 ? "" : "s"}</div>}
                />

                <div className="mt-6 space-y-6">
                    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                        <StatCard label="Visible results" value={metrics.total} />
                        <StatCard label="Open now" value={metrics.open} tone="blue" />
                        <StatCard label="Average response rate" value={formatRate(metrics.averageResponseRate)} tone="emerald" />
                        <StatCard label="Submitted responses" value={metrics.totalSubmitted} tone="slate" />
                    </div>

                    <DataToolbar
                        filters={(
                            <>
                                <SearchInput value={query} onChange={setQuery} placeholder="Search by survey title" />
                                <SelectFilter label="Lifecycle" value={lifecycleFilter} onChange={setLifecycleFilter} options={[{ label: "All lifecycle states", value: "ALL" }, { label: "Draft", value: "DRAFT" }, { label: "Published", value: "PUBLISHED" }, { label: "Closed", value: "CLOSED" }, { label: "Archived", value: "ARCHIVED" }]} />
                                <SelectFilter label="Runtime" value={runtimeFilter} onChange={setRuntimeFilter} options={[{ label: "All runtime states", value: "ALL" }, { label: "Not open", value: "NOT_OPEN" }, { label: "Open", value: "OPEN" }, { label: "Closed", value: "CLOSED" }]} />
                                <SelectFilter label="Audience" value={audienceFilter} onChange={setAudienceFilter} options={[{ label: "All audiences", value: "ALL" }, { label: "All students", value: "ALL_STUDENTS" }, { label: "Department", value: "DEPARTMENT" }]} />
                                <label className="flex min-w-[170px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                    <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">Start from</span>
                                    <input type="date" value={startDateFrom} onChange={(event) => setStartDateFrom(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                </label>
                                <label className="flex min-w-[170px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                    <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">End by</span>
                                    <input type="date" value={endDateTo} onChange={(event) => setEndDateTo(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                </label>
                                <SelectFilter
                                    label="Sort"
                                    value={`${sortBy}:${sortDir}`}
                                    onChange={(value) => {
                                        const [nextSortBy, nextSortDir] = value.split(":");
                                        setSortBy(nextSortBy);
                                        setSortDir(nextSortDir);
                                    }}
                                    options={[
                                        { label: "Response rate high-low", value: "responseRate:desc" },
                                        { label: "Response rate low-high", value: "responseRate:asc" },
                                        { label: "Submitted high-low", value: "submittedCount:desc" },
                                        { label: "Targeted high-low", value: "targetedCount:desc" },
                                        { label: "Start date newest", value: "startDate:desc" },
                                        { label: "End date latest", value: "endDate:desc" },
                                    ]}
                                />
                            </>
                        )}
                        actions={<div className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-semibold text-slate-600">{surveys.length} row{surveys.length === 1 ? "" : "s"} on page</div>}
                    />

                    {error ? (
                        <ErrorState description={error} onRetry={() => void loadSurveyResults()} />
                    ) : loading ? (
                        <LoadingState label="Loading survey results..." />
                    ) : surveys.length === 0 ? (
                        <EmptyState
                            title="No survey results found"
                            description={session?.role === "TEACHER" ? "No results match your department scope and current filters." : "Adjust the filters to find the survey analytics you need."}
                            icon="bar_chart"
                        />
                    ) : (
                        <>
                            <div className="hidden lg:block">
                                <DataTable columns={columns} items={surveys} getRowKey={(survey) => survey.id} />
                            </div>

                            <ResponsiveDataList
                                items={surveys}
                                getKey={(survey) => survey.id}
                                renderItem={(survey) => (
                                    <article className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
                                        <div className="flex flex-wrap gap-2">
                                            <StatusBadge kind="surveyLifecycle" value={survey.lifecycleState} />
                                            <StatusBadge kind="surveyRuntime" value={survey.runtimeStatus} />
                                        </div>
                                        <h2 className="mt-3 text-xl font-bold text-slate-950">{survey.title}</h2>
                                        <p className="mt-2 text-sm leading-6 text-slate-500">{survey.description || "No survey description provided."}</p>
                                        <div className="mt-4 grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Audience</span><span className="font-medium text-slate-900">{getAudienceLabel(survey)}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Targeted</span><span className="font-medium text-slate-900">{survey.targetedCount}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Submitted</span><span className="font-medium text-slate-900">{survey.submittedCount}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Response rate</span><span className="font-medium text-slate-900">{formatRate(survey.responseRate)}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Window</span><span className="text-right font-medium text-slate-900">{formatDateRange(survey.startDate, survey.endDate)}</span></div>
                                        </div>
                                        <Link to={`/survey-results/${survey.id}`} className="mt-5 inline-flex w-full items-center justify-center rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                                            View details
                                        </Link>
                                    </article>
                                )}
                            />

                            <PaginationControls page={page + 1} pageCount={Math.max(totalPages, 1)} onPageChange={(nextPage) => setPage(nextPage - 1)} />
                        </>
                    )}
                </div>
            </div>
        </main>
    );
}
