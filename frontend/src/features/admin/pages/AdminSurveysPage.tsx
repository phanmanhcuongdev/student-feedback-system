import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { archiveSurvey, closeSurvey, getManagedSurveys, publishSurvey, setSurveyVisibility } from "../../../api/adminApi";
import { getApiErrorMessage } from "../../../api/apiError";
import DataTable, { type DataTableColumn } from "../../../components/data-view/DataTable";
import DataToolbar from "../../../components/data-view/DataToolbar";
import PaginationControls from "../../../components/data-view/PaginationControls";
import ResponsiveDataList from "../../../components/data-view/ResponsiveDataList";
import SearchInput from "../../../components/data-view/SearchInput";
import SelectFilter from "../../../components/data-view/SelectFilter";
import ConfirmDialog from "../../../components/ui/ConfirmDialog";
import EmptyState from "../../../components/ui/EmptyState";
import ErrorState from "../../../components/ui/ErrorState";
import LoadingState from "../../../components/ui/LoadingState";
import PageHeader from "../../../components/ui/PageHeader";
import StatCard from "../../../components/ui/StatCard";
import StatusBadge from "../../../components/ui/StatusBadge";
import { darkActionButtonClass, darkActionButtonStyle } from "../../../components/ui/buttonStyles";
import type { ManagedSurveyMetrics, ManagedSurveySummary } from "../../../types/survey";

type SurveyActionType = "publish" | "close" | "archive" | "show" | "hide";

type PendingAction = {
    survey: ManagedSurveySummary;
    action: SurveyActionType;
    title: string;
    description: string;
    confirmLabel: string;
    tone?: "default" | "danger";
};

function formatDate(date: string | null) {
    if (!date) {
        return "Not set";
    }

    return new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
    }).format(new Date(date));
}

function formatRate(value: number) {
    return `${value.toFixed(1)}%`;
}

function getAudienceLabel(survey: ManagedSurveySummary) {
    if (survey.recipientScope === "DEPARTMENT") {
        return survey.recipientDepartmentName || (survey.recipientDepartmentId ? `Department ${survey.recipientDepartmentId}` : "Department audience");
    }

    return "All students";
}

function getActionConfig(survey: ManagedSurveySummary, action: SurveyActionType): PendingAction {
    const actionMap: Record<SurveyActionType, PendingAction> = {
        publish: { survey, action, title: "Publish survey", description: `Publish "${survey.title}" and assign it to recipients based on its audience settings.`, confirmLabel: "Publish survey" },
        close: { survey, action, title: "Close survey", description: `Close "${survey.title}" now and stop any further responses regardless of its original end date.`, confirmLabel: "Close survey", tone: "danger" },
        archive: { survey, action, title: "Archive survey", description: `Archive "${survey.title}" as a historical record. Archived surveys stay read-only.`, confirmLabel: "Archive survey" },
        show: { survey, action, title: "Make survey visible", description: `Make "${survey.title}" visible again for its eligible recipients.`, confirmLabel: "Show survey" },
        hide: { survey, action, title: "Hide survey", description: `Hide "${survey.title}" from recipients without changing its lifecycle state.`, confirmLabel: "Hide survey" },
    };

    return actionMap[action];
}

export default function AdminSurveysPage() {
    const [surveys, setSurveys] = useState<ManagedSurveySummary[]>([]);
    const [metrics, setMetrics] = useState<ManagedSurveyMetrics>({
        totalSurveys: 0,
        totalDrafts: 0,
        totalPublished: 0,
        totalOpen: 0,
        totalClosed: 0,
        totalHidden: 0,
    });
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [query, setQuery] = useState("");
    const [debouncedQuery, setDebouncedQuery] = useState("");
    const [lifecycleFilter, setLifecycleFilter] = useState("ALL");
    const [runtimeFilter, setRuntimeFilter] = useState("ALL");
    const [visibilityFilter, setVisibilityFilter] = useState("ALL");
    const [scopeFilter, setScopeFilter] = useState("ALL");
    const [startDateFrom, setStartDateFrom] = useState("");
    const [endDateTo, setEndDateTo] = useState("");
    const [sortBy, setSortBy] = useState("startDate");
    const [sortDir, setSortDir] = useState("desc");
    const [page, setPage] = useState(0);
    const [actionSurveyId, setActionSurveyId] = useState<number | null>(null);
    const [pendingAction, setPendingAction] = useState<PendingAction | null>(null);

    const pageSize = 20;

    const loadSurveys = useCallback(async () => {
        try {
            setLoading(true);
            setError("");
            const response = await getManagedSurveys({
                keyword: debouncedQuery || undefined,
                lifecycleState: lifecycleFilter === "ALL" ? undefined : lifecycleFilter,
                runtimeStatus: runtimeFilter === "ALL" ? undefined : runtimeFilter,
                hidden: visibilityFilter === "ALL" ? undefined : visibilityFilter === "HIDDEN",
                recipientScope: scopeFilter === "ALL" ? undefined : scopeFilter,
                startDateFrom: startDateFrom || undefined,
                endDateTo: endDateTo || undefined,
                page,
                size: pageSize,
                sortBy,
                sortDir,
            });
            setSurveys(response.items);
            setMetrics(response.metrics);
            setTotalElements(response.totalElements);
            setTotalPages(response.totalPages);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to load surveys."));
        } finally {
            setLoading(false);
        }
    }, [debouncedQuery, endDateTo, lifecycleFilter, page, runtimeFilter, scopeFilter, sortBy, sortDir, startDateFrom, visibilityFilter]);

    useEffect(() => {
        const timeout = window.setTimeout(() => {
            setDebouncedQuery(query.trim());
        }, 300);

        return () => window.clearTimeout(timeout);
    }, [query]);

    useEffect(() => {
        void loadSurveys();
    }, [loadSurveys]);

    useEffect(() => {
        setPage(0);
    }, [debouncedQuery, lifecycleFilter, runtimeFilter, visibilityFilter, scopeFilter, startDateFrom, endDateTo, sortBy, sortDir]);

    async function runAction(config: PendingAction) {
        try {
            setActionSurveyId(config.survey.id);
            setError("");

            let response;
            switch (config.action) {
                case "publish":
                    response = await publishSurvey(config.survey.id);
                    break;
                case "close":
                    response = await closeSurvey(config.survey.id);
                    break;
                case "archive":
                    response = await archiveSurvey(config.survey.id);
                    break;
                case "show":
                    response = await setSurveyVisibility(config.survey.id, false);
                    break;
                case "hide":
                    response = await setSurveyVisibility(config.survey.id, true);
                    break;
            }

            if (!response.success) {
                setError(response.message || "Unable to update survey.");
                return;
            }

            setPendingAction(null);
            await loadSurveys();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to update survey."));
        } finally {
            setActionSurveyId(null);
        }
    }

    function openActionDialog(survey: ManagedSurveySummary, action: SurveyActionType) {
        setPendingAction(getActionConfig(survey, action));
    }

    const columns: DataTableColumn<ManagedSurveySummary>[] = [
        {
            key: "title",
            header: "Survey",
            render: (survey) => (
                <div>
                    <p className="font-bold text-slate-950">{survey.title}</p>
                    <p className="mt-1 line-clamp-2 max-w-md text-sm text-slate-500">
                        {survey.description || "No survey description provided."}
                    </p>
                </div>
            ),
        },
        {
            key: "lifecycle",
            header: "Lifecycle",
            render: (survey) => <StatusBadge kind="surveyLifecycle" value={survey.lifecycleState} />,
        },
        {
            key: "runtime",
            header: "Runtime",
            render: (survey) => <StatusBadge kind="surveyRuntime" value={survey.runtimeStatus} />,
        },
        {
            key: "visibility",
            header: "Visibility",
            render: (survey) => <StatusBadge kind="surveyVisibility" value={survey.hidden ? "HIDDEN" : "VISIBLE"} />,
        },
        {
            key: "audience",
            header: "Audience",
            render: (survey) => getAudienceLabel(survey),
        },
        {
            key: "activity",
            header: "Recipients",
            render: (survey) => (
                <div className="text-sm text-slate-600">
                    <p>Targeted: {survey.targetedCount}</p>
                    <p>Opened: {survey.openedCount}</p>
                    <p>Submitted: {survey.submittedCount}</p>
                </div>
            ),
        },
        {
            key: "rate",
            header: "Response rate",
            render: (survey) => formatRate(survey.responseRate),
        },
        {
            key: "window",
            header: "Window",
            render: (survey) => (
                <div className="text-sm text-slate-600">
                    <p>Start: {formatDate(survey.startDate)}</p>
                    <p>End: {formatDate(survey.endDate)}</p>
                </div>
            ),
        },
        {
            key: "actions",
            header: "Actions",
            className: "text-right",
            render: (survey) => (
                <div className="flex justify-end">
                    <div className="flex flex-wrap justify-end gap-2">
                        <Link to={`/admin/surveys/${survey.id}/edit`} className="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                            <span>View/Edit</span>
                        </Link>
                        {survey.lifecycleState === "DRAFT" ? (
                            <button type="button" onClick={() => openActionDialog(survey, "publish")} disabled={actionSurveyId === survey.id} className="inline-flex items-center gap-2 rounded-xl border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm font-semibold text-emerald-700 transition hover:border-emerald-300 hover:bg-emerald-100 disabled:cursor-not-allowed disabled:opacity-60">
                                Publish
                            </button>
                        ) : null}
                        {survey.lifecycleState === "PUBLISHED" ? (
                            <button type="button" onClick={() => openActionDialog(survey, "close")} disabled={actionSurveyId === survey.id} className="inline-flex items-center gap-2 rounded-xl border border-amber-200 bg-amber-50 px-3 py-2 text-sm font-semibold text-amber-700 transition hover:border-amber-300 hover:bg-amber-100 disabled:cursor-not-allowed disabled:opacity-60">
                                Close
                            </button>
                        ) : null}
                        {survey.lifecycleState === "CLOSED" ? (
                            <button type="button" onClick={() => openActionDialog(survey, "archive")} disabled={actionSurveyId === survey.id} className="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-slate-100 px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-200 disabled:cursor-not-allowed disabled:opacity-60">
                                Archive
                            </button>
                        ) : null}
                        {(survey.lifecycleState === "PUBLISHED" || survey.lifecycleState === "CLOSED") ? (
                            <button type="button" onClick={() => openActionDialog(survey, survey.hidden ? "show" : "hide")} disabled={actionSurveyId === survey.id} className="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60">
                                {survey.hidden ? "Show" : "Hide"}
                            </button>
                        ) : null}
                    </div>
                </div>
            ),
        },
    ];

    const listMeta = useMemo(() => `${surveys.length} row${surveys.length === 1 ? "" : "s"} on page`, [surveys.length]);

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-2xl px-6 py-10">
                <PageHeader
                    eyebrow="Admin / Surveys"
                    title="Survey management"
                    description="Operate surveys as managed campaigns with clear lifecycle state, runtime availability, visibility, recipient scope, and response activity."
                    actions={(
                        <>
                            <div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm">
                                {totalElements} matching survey{totalElements === 1 ? "" : "s"}
                            </div>
                            <Link to="/admin/surveys/create" className={`${darkActionButtonClass} px-5 py-3 text-sm font-semibold`} aria-label="Create survey" style={darkActionButtonStyle}>
                                <span className="material-symbols-outlined text-[18px] text-white" style={darkActionButtonStyle}>add</span>
                                <span className="text-white" style={darkActionButtonStyle}>Create survey</span>
                            </Link>
                        </>
                    )}
                />

                <div className="mt-6 space-y-6">
                    <div className="grid gap-4 md:grid-cols-3 xl:grid-cols-6">
                        <StatCard label="Total surveys" value={metrics.totalSurveys} />
                        <StatCard label="Drafts" value={metrics.totalDrafts} tone="slate" />
                        <StatCard label="Published" value={metrics.totalPublished} tone="emerald" />
                        <StatCard label="Open now" value={metrics.totalOpen} tone="blue" />
                        <StatCard label="Closed" value={metrics.totalClosed} tone="amber" />
                        <StatCard label="Hidden" value={metrics.totalHidden} tone="slate" />
                    </div>

                    <DataToolbar
                        filters={(
                            <>
                                <SearchInput value={query} onChange={setQuery} placeholder="Search by title or description" />
                                <SelectFilter label="Lifecycle" value={lifecycleFilter} onChange={setLifecycleFilter} options={[{ label: "All lifecycle states", value: "ALL" }, { label: "Draft", value: "DRAFT" }, { label: "Published", value: "PUBLISHED" }, { label: "Closed", value: "CLOSED" }, { label: "Archived", value: "ARCHIVED" }]} />
                                <SelectFilter label="Runtime" value={runtimeFilter} onChange={setRuntimeFilter} options={[{ label: "All runtime states", value: "ALL" }, { label: "Not open", value: "NOT_OPEN" }, { label: "Open", value: "OPEN" }, { label: "Closed", value: "CLOSED" }]} />
                                <SelectFilter label="Visibility" value={visibilityFilter} onChange={setVisibilityFilter} options={[{ label: "All visibility", value: "ALL" }, { label: "Visible", value: "VISIBLE" }, { label: "Hidden", value: "HIDDEN" }]} />
                                <SelectFilter label="Audience" value={scopeFilter} onChange={setScopeFilter} options={[{ label: "All audiences", value: "ALL" }, { label: "All students", value: "ALL_STUDENTS" }, { label: "Department", value: "DEPARTMENT" }]} />
                                <label className="flex min-w-[170px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                    <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">Start from</span>
                                    <input type="date" value={startDateFrom} onChange={(event) => setStartDateFrom(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                </label>
                                <label className="flex min-w-[170px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                    <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">End by</span>
                                    <input type="date" value={endDateTo} onChange={(event) => setEndDateTo(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                </label>
                                <SelectFilter label="Sort" value={`${sortBy}:${sortDir}`} onChange={(value) => {
                                    const [nextSortBy, nextSortDir] = value.split(":");
                                    setSortBy(nextSortBy);
                                    setSortDir(nextSortDir);
                                }} options={[{ label: "Start date newest", value: "startDate:desc" }, { label: "Start date oldest", value: "startDate:asc" }, { label: "End date latest", value: "endDate:desc" }, { label: "End date earliest", value: "endDate:asc" }, { label: "Response rate high-low", value: "responseRate:desc" }, { label: "Targeted high-low", value: "targetedCount:desc" }, { label: "Opened high-low", value: "openedCount:desc" }, { label: "Submitted high-low", value: "submittedCount:desc" }, { label: "Title A-Z", value: "title:asc" }]} />
                            </>
                        )}
                        actions={<div className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-semibold text-slate-600">{listMeta}</div>}
                    />

                    {error ? <ErrorState description={error} onRetry={() => void loadSurveys()} /> : null}

                    {loading ? (
                        <LoadingState label="Loading surveys..." />
                    ) : surveys.length === 0 ? (
                        <EmptyState
                            title="No matching surveys"
                            description="Adjust the filters, date window, or search terms to find the survey campaign you need."
                            icon="assignment"
                            action={<Link to="/admin/surveys/create" className={`${darkActionButtonClass} px-4 py-3 text-sm font-semibold`} aria-label="Create survey" style={darkActionButtonStyle}><span className="text-white" style={darkActionButtonStyle}>Create survey</span></Link>}
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
                                        <div className="flex items-start justify-between gap-4">
                                            <div>
                                                <div className="flex flex-wrap items-center gap-2">
                                                    <StatusBadge kind="surveyLifecycle" value={survey.lifecycleState} />
                                                    <StatusBadge kind="surveyRuntime" value={survey.runtimeStatus} />
                                                    <StatusBadge kind="surveyVisibility" value={survey.hidden ? "HIDDEN" : "VISIBLE"} />
                                                </div>
                                                <h2 className="mt-3 text-xl font-bold text-slate-950">{survey.title}</h2>
                                                <p className="mt-2 text-sm leading-6 text-slate-500">{survey.description || "No survey description provided."}</p>
                                            </div>
                                            <div className="rounded-2xl bg-slate-100 px-3 py-2 text-xs font-semibold uppercase tracking-[0.16em] text-slate-500">ID {survey.id}</div>
                                        </div>

                                        <div className="mt-4 grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Audience</span><span className="text-right font-medium text-slate-900">{getAudienceLabel(survey)}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Targeted</span><span className="text-right font-medium text-slate-900">{survey.targetedCount}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Opened</span><span className="text-right font-medium text-slate-900">{survey.openedCount}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Submitted</span><span className="text-right font-medium text-slate-900">{survey.submittedCount}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Response rate</span><span className="text-right font-medium text-slate-900">{formatRate(survey.responseRate)}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Window</span><span className="text-right font-medium text-slate-900">{formatDate(survey.startDate)} - {formatDate(survey.endDate)}</span></div>
                                        </div>

                                        <div className="mt-5 grid gap-3">
                                            <Link to={`/admin/surveys/${survey.id}/edit`} className="inline-flex w-full items-center justify-center rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">View/Edit</Link>
                                            {survey.lifecycleState === "DRAFT" ? <button type="button" onClick={() => openActionDialog(survey, "publish")} disabled={actionSurveyId === survey.id} className="inline-flex w-full items-center justify-center rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-700 transition hover:border-emerald-300 hover:bg-emerald-100 disabled:cursor-not-allowed disabled:opacity-60">Publish survey</button> : null}
                                            {survey.lifecycleState === "PUBLISHED" ? <button type="button" onClick={() => openActionDialog(survey, "close")} disabled={actionSurveyId === survey.id} className="inline-flex w-full items-center justify-center rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-semibold text-amber-700 transition hover:border-amber-300 hover:bg-amber-100 disabled:cursor-not-allowed disabled:opacity-60">Close survey</button> : null}
                                            {survey.lifecycleState === "CLOSED" ? <button type="button" onClick={() => openActionDialog(survey, "archive")} disabled={actionSurveyId === survey.id} className="inline-flex w-full items-center justify-center rounded-2xl border border-slate-300 bg-slate-100 px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-200 disabled:cursor-not-allowed disabled:opacity-60">Archive survey</button> : null}
                                            {(survey.lifecycleState === "PUBLISHED" || survey.lifecycleState === "CLOSED") ? <button type="button" onClick={() => openActionDialog(survey, survey.hidden ? "show" : "hide")} disabled={actionSurveyId === survey.id} className="inline-flex w-full items-center justify-center rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60">{survey.hidden ? "Show survey" : "Hide survey"}</button> : null}
                                        </div>
                                    </article>
                                )}
                            />

                            <PaginationControls page={page + 1} pageCount={Math.max(totalPages, 1)} onPageChange={(nextPage) => setPage(nextPage - 1)} />
                        </>
                    )}
                </div>
            </div>

            <ConfirmDialog
                open={pendingAction != null}
                title={pendingAction?.title || ""}
                description={pendingAction?.description || ""}
                confirmLabel={pendingAction?.confirmLabel || "Confirm"}
                tone={pendingAction?.tone}
                busy={pendingAction != null && actionSurveyId === pendingAction.survey.id}
                onCancel={() => setPendingAction(null)}
                onConfirm={() => {
                    if (pendingAction) {
                        void runAction(pendingAction);
                    }
                }}
            />
        </main>
    );
}
