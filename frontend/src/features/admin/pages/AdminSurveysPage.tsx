import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
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

function formatDate(date: string | null, language: string, notSetLabel: string) {
    if (!date) {
        return notSetLabel;
    }

    return new Intl.DateTimeFormat(language === "vi" ? "vi-VN" : "en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
    }).format(new Date(date));
}

function formatRate(value: number) {
    return `${value.toFixed(1)}%`;
}

function getAudienceLabel(survey: ManagedSurveySummary, t: (key: string, options?: Record<string, unknown>) => string) {
    if (survey.recipientScope === "DEPARTMENT") {
        return survey.recipientDepartmentName || (survey.recipientDepartmentId ? t("admin:admin.surveys.list.departmentWithId", { id: survey.recipientDepartmentId }) : t("admin:admin.surveys.list.departmentAudience"));
    }

    return t("admin:admin.surveys.form.audience.allStudents");
}

function getActionConfig(survey: ManagedSurveySummary, action: SurveyActionType, t: (key: string, options?: Record<string, unknown>) => string): PendingAction {
    const actionMap: Record<SurveyActionType, PendingAction> = {
        publish: { survey, action, title: t("admin:admin.surveys.form.lifecycleActions.publish"), description: t("admin:admin.surveys.list.confirm.publish", { title: survey.title }), confirmLabel: t("admin:admin.surveys.form.lifecycleActions.publish") },
        close: { survey, action, title: t("admin:admin.surveys.form.lifecycleActions.close"), description: t("admin:admin.surveys.list.confirm.close", { title: survey.title }), confirmLabel: t("admin:admin.surveys.form.lifecycleActions.close"), tone: "danger" },
        archive: { survey, action, title: t("admin:admin.surveys.form.lifecycleActions.archive"), description: t("admin:admin.surveys.list.confirm.archive", { title: survey.title }), confirmLabel: t("admin:admin.surveys.form.lifecycleActions.archive") },
        show: { survey, action, title: t("admin:admin.surveys.form.lifecycleActions.makeVisible"), description: t("admin:admin.surveys.list.confirm.show", { title: survey.title }), confirmLabel: t("admin:admin.surveys.form.lifecycleActions.show") },
        hide: { survey, action, title: t("admin:admin.surveys.form.lifecycleActions.hide"), description: t("admin:admin.surveys.list.confirm.hide", { title: survey.title }), confirmLabel: t("admin:admin.surveys.form.lifecycleActions.hide") },
    };

    return actionMap[action];
}

export default function AdminSurveysPage() {
    const { i18n, t } = useTranslation(["admin", "common"]);
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
            setError(getApiErrorMessage(requestError, t("admin:admin.surveys.list.errors.load")));
        } finally {
            setLoading(false);
        }
    }, [debouncedQuery, endDateTo, lifecycleFilter, page, runtimeFilter, scopeFilter, sortBy, sortDir, startDateFrom, visibilityFilter, t]);

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
                setError(response.message || t("admin:admin.surveys.form.errors.update"));
                return;
            }

            setPendingAction(null);
            await loadSurveys();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.surveys.form.errors.update")));
        } finally {
            setActionSurveyId(null);
        }
    }

    function openActionDialog(survey: ManagedSurveySummary, action: SurveyActionType) {
        setPendingAction(getActionConfig(survey, action, t));
    }

    const columns: DataTableColumn<ManagedSurveySummary>[] = [
        {
            key: "title",
            header: t("admin:admin.surveys.list.table.survey"),
            render: (survey) => (
                <div>
                    <p className="font-bold text-slate-950">{survey.title}</p>
                    <p className="mt-1 line-clamp-2 max-w-md text-sm text-slate-500">
                        {survey.description || t("admin:admin.dashboard.common.noSurveyDescription")}
                    </p>
                </div>
            ),
        },
        {
            key: "lifecycle",
            header: t("admin:admin.surveys.list.table.lifecycle"),
            render: (survey) => <StatusBadge kind="surveyLifecycle" value={survey.lifecycleState} />,
        },
        {
            key: "runtime",
            header: t("admin:admin.surveys.list.table.runtime"),
            render: (survey) => <StatusBadge kind="surveyRuntime" value={survey.runtimeStatus} />,
        },
        {
            key: "visibility",
            header: t("admin:admin.surveys.list.table.visibility"),
            render: (survey) => <StatusBadge kind="surveyVisibility" value={survey.hidden ? "HIDDEN" : "VISIBLE"} />,
        },
        {
            key: "audience",
            header: t("admin:admin.surveys.list.table.audience"),
            render: (survey) => getAudienceLabel(survey, t),
        },
        {
            key: "activity",
            header: t("admin:admin.surveys.list.table.recipients"),
            render: (survey) => (
                <div className="text-sm text-slate-600">
                    <p>{t("admin:admin.surveys.form.stats.targeted")}: {survey.targetedCount}</p>
                    <p>{t("admin:admin.surveys.form.stats.opened")}: {survey.openedCount}</p>
                    <p>{t("admin:admin.surveys.form.stats.submitted")}: {survey.submittedCount}</p>
                </div>
            ),
        },
        {
            key: "rate",
            header: t("admin:admin.surveys.form.stats.responseRate"),
            render: (survey) => formatRate(survey.responseRate),
        },
        {
            key: "window",
            header: t("admin:admin.surveys.list.table.window"),
            render: (survey) => (
                <div className="text-sm text-slate-600">
                    <p>{t("admin:admin.surveys.list.table.start")}: {formatDate(survey.startDate, i18n.language, t("admin:admin.surveys.list.notSet"))}</p>
                    <p>{t("admin:admin.surveys.list.table.end")}: {formatDate(survey.endDate, i18n.language, t("admin:admin.surveys.list.notSet"))}</p>
                </div>
            ),
        },
        {
            key: "actions",
            header: t("admin:admin.users.table.actions"),
            className: "text-right",
            render: (survey) => (
                <div className="flex justify-end">
                    <div className="flex flex-wrap justify-end gap-2">
                        <Link to={`/admin/surveys/${survey.id}/edit`} className="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                            <span>{t("admin:admin.surveys.list.buttons.viewEdit")}</span>
                        </Link>
                        {survey.lifecycleState === "DRAFT" ? (
                            <button type="button" onClick={() => openActionDialog(survey, "publish")} disabled={actionSurveyId === survey.id} className="inline-flex items-center gap-2 rounded-xl border border-emerald-200 bg-emerald-50 px-3 py-2 text-sm font-semibold text-emerald-700 transition hover:border-emerald-300 hover:bg-emerald-100 disabled:cursor-not-allowed disabled:opacity-60">
                                {t("admin:admin.surveys.list.buttons.publish")}
                            </button>
                        ) : null}
                        {survey.lifecycleState === "PUBLISHED" ? (
                            <button type="button" onClick={() => openActionDialog(survey, "close")} disabled={actionSurveyId === survey.id} className="inline-flex items-center gap-2 rounded-xl border border-amber-200 bg-amber-50 px-3 py-2 text-sm font-semibold text-amber-700 transition hover:border-amber-300 hover:bg-amber-100 disabled:cursor-not-allowed disabled:opacity-60">
                                {t("admin:admin.surveys.list.buttons.close")}
                            </button>
                        ) : null}
                        {survey.lifecycleState === "CLOSED" ? (
                            <button type="button" onClick={() => openActionDialog(survey, "archive")} disabled={actionSurveyId === survey.id} className="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-slate-100 px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-200 disabled:cursor-not-allowed disabled:opacity-60">
                                {t("admin:admin.surveys.list.buttons.archive")}
                            </button>
                        ) : null}
                        {(survey.lifecycleState === "PUBLISHED" || survey.lifecycleState === "CLOSED") ? (
                            <button type="button" onClick={() => openActionDialog(survey, survey.hidden ? "show" : "hide")} disabled={actionSurveyId === survey.id} className="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60">
                                {survey.hidden ? t("admin:admin.surveys.list.buttons.show") : t("admin:admin.surveys.list.buttons.hide")}
                            </button>
                        ) : null}
                    </div>
                </div>
            ),
        },
    ];

    const listMeta = useMemo(() => t("admin:admin.surveys.list.rowsOnPage", { count: surveys.length }), [surveys.length, t]);

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-2xl px-6 py-10">
                <PageHeader
                    eyebrow={t("admin:admin.surveys.form.header.eyebrow")}
                    title={t("admin:admin.surveys.list.header.title")}
                    description={t("admin:admin.surveys.list.header.description")}
                    actions={(
                        <>
                            <div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm">
                                {t("admin:admin.surveys.list.header.matchingCount", { count: totalElements })}
                            </div>
                            <Link to="/admin/surveys/create" className={`${darkActionButtonClass} px-5 py-3 text-sm font-semibold`} aria-label={t("admin:admin.surveys.form.header.createTitle")} style={darkActionButtonStyle}>
                                <span className="material-symbols-outlined text-[18px] text-white" style={darkActionButtonStyle}>add</span>
                                <span className="text-white" style={darkActionButtonStyle}>{t("admin:admin.surveys.form.header.createTitle")}</span>
                            </Link>
                        </>
                    )}
                />

                <div className="mt-6 space-y-6">
                    <div className="grid gap-4 md:grid-cols-3 xl:grid-cols-6">
                        <StatCard label={t("admin:admin.surveys.list.stats.totalSurveys")} value={metrics.totalSurveys} />
                        <StatCard label={t("admin:admin.surveys.list.stats.drafts")} value={metrics.totalDrafts} tone="slate" />
                        <StatCard label={t("admin:admin.surveys.list.stats.published")} value={metrics.totalPublished} tone="emerald" />
                        <StatCard label={t("admin:admin.surveys.list.stats.openNow")} value={metrics.totalOpen} tone="blue" />
                        <StatCard label={t("admin:admin.surveys.list.stats.closed")} value={metrics.totalClosed} tone="amber" />
                        <StatCard label={t("admin:admin.surveys.list.stats.hidden")} value={metrics.totalHidden} tone="slate" />
                    </div>

                    <DataToolbar
                        filters={(
                            <>
                                <SearchInput value={query} onChange={setQuery} placeholder={t("admin:admin.surveys.list.filters.search")} />
                                <SelectFilter label={t("admin:admin.surveys.list.table.lifecycle")} value={lifecycleFilter} onChange={setLifecycleFilter} options={[{ label: t("admin:admin.surveys.list.filters.allLifecycle"), value: "ALL" }, { label: t("admin:admin.surveys.list.filters.draft"), value: "DRAFT" }, { label: t("admin:admin.surveys.list.filters.published"), value: "PUBLISHED" }, { label: t("admin:admin.surveys.list.filters.closed"), value: "CLOSED" }, { label: t("admin:admin.surveys.list.filters.archived"), value: "ARCHIVED" }]} />
                                <SelectFilter label={t("admin:admin.surveys.list.table.runtime")} value={runtimeFilter} onChange={setRuntimeFilter} options={[{ label: t("admin:admin.surveys.list.filters.allRuntime"), value: "ALL" }, { label: t("admin:admin.surveys.list.filters.notOpen"), value: "NOT_OPEN" }, { label: t("admin:admin.surveys.list.filters.open"), value: "OPEN" }, { label: t("admin:admin.surveys.list.filters.closed"), value: "CLOSED" }]} />
                                <SelectFilter label={t("admin:admin.surveys.list.table.visibility")} value={visibilityFilter} onChange={setVisibilityFilter} options={[{ label: t("admin:admin.surveys.list.filters.allVisibility"), value: "ALL" }, { label: t("admin:admin.surveys.list.filters.visible"), value: "VISIBLE" }, { label: t("admin:admin.surveys.list.filters.hidden"), value: "HIDDEN" }]} />
                                <SelectFilter label={t("admin:admin.surveys.list.table.audience")} value={scopeFilter} onChange={setScopeFilter} options={[{ label: t("admin:admin.surveys.list.filters.allAudiences"), value: "ALL" }, { label: t("admin:admin.surveys.form.audience.allStudents"), value: "ALL_STUDENTS" }, { label: t("admin:admin.surveys.form.fields.department"), value: "DEPARTMENT" }]} />
                                <label className="flex min-w-[170px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                    <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">{t("admin:admin.surveys.list.filters.startFrom")}</span>
                                    <input type="date" value={startDateFrom} onChange={(event) => setStartDateFrom(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                </label>
                                <label className="flex min-w-[170px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                    <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">{t("admin:admin.surveys.list.filters.endBy")}</span>
                                    <input type="date" value={endDateTo} onChange={(event) => setEndDateTo(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                </label>
                                <SelectFilter label={t("admin:admin.users.filters.sort")} value={`${sortBy}:${sortDir}`} onChange={(value) => {
                                    const [nextSortBy, nextSortDir] = value.split(":");
                                    setSortBy(nextSortBy);
                                    setSortDir(nextSortDir);
                                }} options={[{ label: t("admin:admin.surveys.list.filters.startDateNewest"), value: "startDate:desc" }, { label: t("admin:admin.surveys.list.filters.startDateOldest"), value: "startDate:asc" }, { label: t("admin:admin.surveys.list.filters.endDateLatest"), value: "endDate:desc" }, { label: t("admin:admin.surveys.list.filters.endDateEarliest"), value: "endDate:asc" }, { label: t("admin:admin.surveys.list.filters.responseRateHighLow"), value: "responseRate:desc" }, { label: t("admin:admin.surveys.list.filters.targetedHighLow"), value: "targetedCount:desc" }, { label: t("admin:admin.surveys.list.filters.openedHighLow"), value: "openedCount:desc" }, { label: t("admin:admin.surveys.list.filters.submittedHighLow"), value: "submittedCount:desc" }, { label: t("admin:admin.surveys.list.filters.titleAsc"), value: "title:asc" }]} />
                            </>
                        )}
                        actions={<div className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-semibold text-slate-600">{listMeta}</div>}
                    />

                    {error ? <ErrorState description={error} onRetry={() => void loadSurveys()} /> : null}

                    {loading ? (
                        <LoadingState label={t("admin:admin.surveys.list.loading")} />
                    ) : surveys.length === 0 ? (
                        <EmptyState
                            title={t("admin:admin.surveys.list.empty.title")}
                            description={t("admin:admin.surveys.list.empty.description")}
                            icon="assignment"
                            action={<Link to="/admin/surveys/create" className={`${darkActionButtonClass} px-4 py-3 text-sm font-semibold`} aria-label={t("admin:admin.surveys.form.header.createTitle")} style={darkActionButtonStyle}><span className="text-white" style={darkActionButtonStyle}>{t("admin:admin.surveys.form.header.createTitle")}</span></Link>}
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
                                                <p className="mt-2 text-sm leading-6 text-slate-500">{survey.description || t("admin:admin.dashboard.common.noSurveyDescription")}</p>
                                            </div>
                                            <div className="rounded-2xl bg-slate-100 px-3 py-2 text-xs font-semibold uppercase tracking-[0.16em] text-slate-500">{t("admin:admin.users.card.id", { id: survey.id })}</div>
                                        </div>

                                        <div className="mt-4 grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">{t("admin:admin.surveys.list.table.audience")}</span><span className="text-right font-medium text-slate-900">{getAudienceLabel(survey, t)}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">{t("admin:admin.surveys.form.stats.targeted")}</span><span className="text-right font-medium text-slate-900">{survey.targetedCount}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">{t("admin:admin.surveys.form.stats.opened")}</span><span className="text-right font-medium text-slate-900">{survey.openedCount}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">{t("admin:admin.surveys.form.stats.submitted")}</span><span className="text-right font-medium text-slate-900">{survey.submittedCount}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">{t("admin:admin.surveys.form.stats.responseRate")}</span><span className="text-right font-medium text-slate-900">{formatRate(survey.responseRate)}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">{t("admin:admin.surveys.list.table.window")}</span><span className="text-right font-medium text-slate-900">{formatDate(survey.startDate, i18n.language, t("admin:admin.surveys.list.notSet"))} - {formatDate(survey.endDate, i18n.language, t("admin:admin.surveys.list.notSet"))}</span></div>
                                        </div>

                                        <div className="mt-5 grid gap-3">
                                            <Link to={`/admin/surveys/${survey.id}/edit`} className="inline-flex w-full items-center justify-center rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">{t("admin:admin.surveys.list.buttons.viewEdit")}</Link>
                                            {survey.lifecycleState === "DRAFT" ? <button type="button" onClick={() => openActionDialog(survey, "publish")} disabled={actionSurveyId === survey.id} className="inline-flex w-full items-center justify-center rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-700 transition hover:border-emerald-300 hover:bg-emerald-100 disabled:cursor-not-allowed disabled:opacity-60">{t("admin:admin.surveys.form.lifecycleActions.publish")}</button> : null}
                                            {survey.lifecycleState === "PUBLISHED" ? <button type="button" onClick={() => openActionDialog(survey, "close")} disabled={actionSurveyId === survey.id} className="inline-flex w-full items-center justify-center rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-semibold text-amber-700 transition hover:border-amber-300 hover:bg-amber-100 disabled:cursor-not-allowed disabled:opacity-60">{t("admin:admin.surveys.form.lifecycleActions.close")}</button> : null}
                                            {survey.lifecycleState === "CLOSED" ? <button type="button" onClick={() => openActionDialog(survey, "archive")} disabled={actionSurveyId === survey.id} className="inline-flex w-full items-center justify-center rounded-2xl border border-slate-300 bg-slate-100 px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-200 disabled:cursor-not-allowed disabled:opacity-60">{t("admin:admin.surveys.form.lifecycleActions.archive")}</button> : null}
                                            {(survey.lifecycleState === "PUBLISHED" || survey.lifecycleState === "CLOSED") ? <button type="button" onClick={() => openActionDialog(survey, survey.hidden ? "show" : "hide")} disabled={actionSurveyId === survey.id} className="inline-flex w-full items-center justify-center rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60">{survey.hidden ? t("admin:admin.surveys.form.lifecycleActions.show") : t("admin:admin.surveys.form.lifecycleActions.hide")}</button> : null}
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
                confirmLabel={pendingAction?.confirmLabel || t("common:common.actions.confirm")}
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
