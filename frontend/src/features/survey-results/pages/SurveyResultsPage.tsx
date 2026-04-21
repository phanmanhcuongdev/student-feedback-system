import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
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

function formatDate(date: string, language: string) {
    return new Intl.DateTimeFormat(language === "vi" ? "vi-VN" : "en-GB", { day: "2-digit", month: "short", year: "numeric" }).format(new Date(date));
}

function formatDateRange(startDate: string, endDate: string, language: string) {
    return `${formatDate(startDate, language)} - ${formatDate(endDate, language)}`;
}

function formatRate(value: number) {
    return `${value.toFixed(1)}%`;
}

function getAudienceLabel(survey: SurveyResultSummary, t: (key: string) => string) {
    if (survey.recipientScope === "DEPARTMENT") {
        return survey.recipientDepartmentName || t("surveyResults:surveyResults.common.department");
    }
    return t("surveyResults:surveyResults.common.allStudents");
}

export default function SurveyResultsPage() {
    const { i18n, t } = useTranslation(["surveyResults"]);
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
            setError(getApiErrorMessage(requestError, t("surveyResults:surveyResults.errors.loadList")));
        } finally {
            setLoading(false);
        }
    }, [audienceFilter, debouncedQuery, endDateTo, lifecycleFilter, page, runtimeFilter, sortBy, sortDir, startDateFrom, t]);

    useEffect(() => {
        void loadSurveyResults();
    }, [loadSurveyResults]);

    useEffect(() => {
        setPage(0);
    }, [audienceFilter, debouncedQuery, endDateTo, lifecycleFilter, runtimeFilter, sortBy, sortDir, startDateFrom]);

    const columns: DataTableColumn<SurveyResultSummary>[] = [
        {
            key: "survey",
            header: t("surveyResults:surveyResults.list.table.survey"),
            render: (survey) => (
                <div>
                    <p className="font-bold text-slate-950">{survey.title}</p>
                    <p className="mt-1 line-clamp-2 max-w-md text-sm text-slate-500">{survey.description || t("surveyResults:surveyResults.common.noDescription")}</p>
                </div>
            ),
        },
        {
            key: "status",
            header: t("surveyResults:surveyResults.list.table.status"),
            render: (survey) => (
                <div className="flex flex-wrap gap-2">
                    <StatusBadge kind="surveyLifecycle" value={survey.lifecycleState} />
                    <StatusBadge kind="surveyRuntime" value={survey.runtimeStatus} />
                </div>
            ),
        },
        {
            key: "audience",
            header: t("surveyResults:surveyResults.list.table.audience"),
            render: (survey) => getAudienceLabel(survey, t),
        },
        {
            key: "targeted",
            header: t("surveyResults:surveyResults.list.table.targeted"),
            render: (survey) => survey.targetedCount,
        },
        {
            key: "submitted",
            header: t("surveyResults:surveyResults.list.table.submitted"),
            render: (survey) => survey.submittedCount,
        },
        {
            key: "opened",
            header: t("surveyResults:surveyResults.list.table.opened"),
            render: (survey) => survey.openedCount,
        },
        {
            key: "rate",
            header: t("surveyResults:surveyResults.list.table.responseRate"),
            render: (survey) => formatRate(survey.responseRate),
        },
        {
            key: "window",
            header: t("surveyResults:surveyResults.list.table.window"),
            render: (survey) => formatDateRange(survey.startDate, survey.endDate, i18n.language),
        },
        {
            key: "actions",
            header: t("surveyResults:surveyResults.list.table.actions"),
            className: "text-right",
            render: (survey) => (
                <div className="flex justify-end">
                    <Link to={`/survey-results/${survey.id}`} className="inline-flex items-center rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                        {t("surveyResults:surveyResults.list.buttons.viewDetails")}
                    </Link>
                </div>
            ),
        },
    ];

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-2xl px-6 py-10">
                <PageHeader
                    eyebrow={t("surveyResults:surveyResults.list.header.eyebrow")}
                    title={t("surveyResults:surveyResults.list.header.title")}
                    description={t("surveyResults:surveyResults.list.header.description")}
                    actions={<div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm">{t("surveyResults:surveyResults.list.header.matchingCount", { count: totalElements })}</div>}
                />

                <div className="mt-6 space-y-6">
                    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                        <StatCard label={t("surveyResults:surveyResults.list.stats.visibleResults")} value={metrics.total} />
                        <StatCard label={t("surveyResults:surveyResults.list.stats.openNow")} value={metrics.open} tone="blue" />
                        <StatCard label={t("surveyResults:surveyResults.list.stats.averageResponseRate")} value={formatRate(metrics.averageResponseRate)} tone="emerald" />
                        <StatCard label={t("surveyResults:surveyResults.list.stats.submittedResponses")} value={metrics.totalSubmitted} tone="slate" />
                    </div>

                    <DataToolbar
                        filters={(
                            <>
                                <SearchInput value={query} onChange={setQuery} placeholder={t("surveyResults:surveyResults.list.filters.search")} />
                                <SelectFilter label={t("surveyResults:surveyResults.list.filters.lifecycle")} value={lifecycleFilter} onChange={setLifecycleFilter} options={[{ label: t("surveyResults:surveyResults.list.filters.allLifecycle"), value: "ALL" }, { label: t("surveyResults:surveyResults.list.filters.draft"), value: "DRAFT" }, { label: t("surveyResults:surveyResults.list.filters.published"), value: "PUBLISHED" }, { label: t("surveyResults:surveyResults.list.filters.closed"), value: "CLOSED" }, { label: t("surveyResults:surveyResults.list.filters.archived"), value: "ARCHIVED" }]} />
                                <SelectFilter label={t("surveyResults:surveyResults.list.filters.runtime")} value={runtimeFilter} onChange={setRuntimeFilter} options={[{ label: t("surveyResults:surveyResults.list.filters.allRuntime"), value: "ALL" }, { label: t("surveyResults:surveyResults.list.filters.notOpen"), value: "NOT_OPEN" }, { label: t("surveyResults:surveyResults.list.filters.open"), value: "OPEN" }, { label: t("surveyResults:surveyResults.list.filters.closed"), value: "CLOSED" }]} />
                                <SelectFilter label={t("surveyResults:surveyResults.list.filters.audience")} value={audienceFilter} onChange={setAudienceFilter} options={[{ label: t("surveyResults:surveyResults.list.filters.allAudiences"), value: "ALL" }, { label: t("surveyResults:surveyResults.common.allStudents"), value: "ALL_STUDENTS" }, { label: t("surveyResults:surveyResults.common.department"), value: "DEPARTMENT" }]} />
                                <label className="flex min-w-[170px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                    <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">{t("surveyResults:surveyResults.list.filters.startFrom")}</span>
                                    <input type="date" value={startDateFrom} onChange={(event) => setStartDateFrom(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                </label>
                                <label className="flex min-w-[170px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                    <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">{t("surveyResults:surveyResults.list.filters.endBy")}</span>
                                    <input type="date" value={endDateTo} onChange={(event) => setEndDateTo(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                </label>
                                <SelectFilter
                                    label={t("surveyResults:surveyResults.list.filters.sort")}
                                    value={`${sortBy}:${sortDir}`}
                                    onChange={(value) => {
                                        const [nextSortBy, nextSortDir] = value.split(":");
                                        setSortBy(nextSortBy);
                                        setSortDir(nextSortDir);
                                    }}
                                    options={[
                                        { label: t("surveyResults:surveyResults.list.filters.responseRateHighLow"), value: "responseRate:desc" },
                                        { label: t("surveyResults:surveyResults.list.filters.responseRateLowHigh"), value: "responseRate:asc" },
                                        { label: t("surveyResults:surveyResults.list.filters.submittedHighLow"), value: "submittedCount:desc" },
                                        { label: t("surveyResults:surveyResults.list.filters.targetedHighLow"), value: "targetedCount:desc" },
                                        { label: t("surveyResults:surveyResults.list.filters.startDateNewest"), value: "startDate:desc" },
                                        { label: t("surveyResults:surveyResults.list.filters.endDateLatest"), value: "endDate:desc" },
                                    ]}
                                />
                            </>
                        )}
                        actions={<div className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-semibold text-slate-600">{t("surveyResults:surveyResults.list.rowsOnPage", { count: surveys.length })}</div>}
                    />

                    {error ? (
                        <ErrorState description={error} onRetry={() => void loadSurveyResults()} />
                    ) : loading ? (
                        <LoadingState label={t("surveyResults:surveyResults.list.loading")} />
                    ) : surveys.length === 0 ? (
                        <EmptyState
                            title={t("surveyResults:surveyResults.list.empty.title")}
                            description={session?.role === "LECTURER" ? t("surveyResults:surveyResults.list.empty.lecturerDescription") : t("surveyResults:surveyResults.list.empty.description")}
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
                                        <p className="mt-2 text-sm leading-6 text-slate-500">{survey.description || t("surveyResults:surveyResults.common.noDescription")}</p>
                                        <div className="mt-4 grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">{t("surveyResults:surveyResults.list.table.audience")}</span><span className="font-medium text-slate-900">{getAudienceLabel(survey, t)}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">{t("surveyResults:surveyResults.list.table.targeted")}</span><span className="font-medium text-slate-900">{survey.targetedCount}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">{t("surveyResults:surveyResults.list.table.opened")}</span><span className="font-medium text-slate-900">{survey.openedCount}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">{t("surveyResults:surveyResults.list.table.submitted")}</span><span className="font-medium text-slate-900">{survey.submittedCount}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">{t("surveyResults:surveyResults.list.table.responseRate")}</span><span className="font-medium text-slate-900">{formatRate(survey.responseRate)}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">{t("surveyResults:surveyResults.list.table.window")}</span><span className="text-right font-medium text-slate-900">{formatDateRange(survey.startDate, survey.endDate, i18n.language)}</span></div>
                                        </div>
                                        <Link to={`/survey-results/${survey.id}`} className="mt-5 inline-flex w-full items-center justify-center rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                                            {t("surveyResults:surveyResults.list.buttons.viewDetails")}
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
