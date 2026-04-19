import { useCallback, useEffect, useState } from "react";
import { getApiErrorMessage } from "../../../api/apiError";
import { getAuditLogs } from "../../../api/adminApi";
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
import type { AuditLogEntry } from "../../../types/admin";

const ACTION_OPTIONS = [
    { label: "All actions", value: "ALL" },
    { label: "Onboarding approved", value: "ONBOARDING_APPROVED" },
    { label: "Onboarding rejected", value: "ONBOARDING_REJECTED" },
    { label: "Survey published", value: "SURVEY_PUBLISHED" },
    { label: "Survey closed", value: "SURVEY_CLOSED" },
    { label: "Survey archived", value: "SURVEY_ARCHIVED" },
    { label: "Survey visibility changed", value: "SURVEY_VISIBILITY_CHANGED" },
    { label: "User profile updated", value: "USER_PROFILE_UPDATED" },
    { label: "User activated", value: "USER_ACTIVATED" },
    { label: "User deactivated", value: "USER_DEACTIVATED" },
];

const TARGET_OPTIONS = [
    { label: "All targets", value: "ALL" },
    { label: "Student", value: "STUDENT" },
    { label: "Survey", value: "SURVEY" },
    { label: "User", value: "USER" },
];

function formatDateTime(value: string) {
    return new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    }).format(new Date(value));
}

function formatCode(value: string) {
    return value
        .replace(/_/g, " ")
        .toLowerCase()
        .replace(/^\w/, (letter) => letter.toUpperCase());
}

function parseOptionalNumber(value: string): number | undefined {
    const trimmed = value.trim();
    if (!trimmed) {
        return undefined;
    }
    const parsed = Number(trimmed);
    return Number.isFinite(parsed) ? parsed : undefined;
}

function DetailBlock({ log }: { log: AuditLogEntry }) {
    const rows = [
        { label: "Details", value: log.details },
        { label: "Old state", value: log.oldState },
        { label: "New state", value: log.newState },
    ].filter((row) => row.value && row.value.trim().length > 0);

    if (rows.length === 0) {
        return <span className="text-slate-400">No extra details</span>;
    }

    return (
        <div className="space-y-2">
            {rows.map((row) => (
                <div key={row.label}>
                    <p className="text-[11px] font-bold uppercase tracking-[0.16em] text-slate-400">{row.label}</p>
                    <p className="mt-1 max-w-xl whitespace-pre-wrap break-words text-sm text-slate-700">{row.value}</p>
                </div>
            ))}
        </div>
    );
}

export default function AuditLogsPage() {
    const [logs, setLogs] = useState<AuditLogEntry[]>([]);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [query, setQuery] = useState("");
    const [debouncedQuery, setDebouncedQuery] = useState("");
    const [actionType, setActionType] = useState("ALL");
    const [targetType, setTargetType] = useState("ALL");
    const [actorUserId, setActorUserId] = useState("");
    const [targetId, setTargetId] = useState("");
    const [createdFrom, setCreatedFrom] = useState("");
    const [createdTo, setCreatedTo] = useState("");
    const [page, setPage] = useState(0);
    const pageSize = 20;

    useEffect(() => {
        const timeout = window.setTimeout(() => setDebouncedQuery(query.trim()), 300);
        return () => window.clearTimeout(timeout);
    }, [query]);

    const loadAuditLogs = useCallback(async () => {
        try {
            setLoading(true);
            setError("");
            const response = await getAuditLogs({
                keyword: debouncedQuery || undefined,
                actionType: actionType === "ALL" ? undefined : actionType,
                targetType: targetType === "ALL" ? undefined : targetType,
                actorUserId: parseOptionalNumber(actorUserId),
                targetId: parseOptionalNumber(targetId),
                createdFrom: createdFrom || undefined,
                createdTo: createdTo || undefined,
                page,
                size: pageSize,
            });
            if (response.items.length === 0 && response.totalPages > 0 && page >= response.totalPages) {
                setPage(response.totalPages - 1);
                return;
            }
            setLogs(response.items);
            setTotalElements(response.totalElements);
            setTotalPages(response.totalPages);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to load audit logs."));
        } finally {
            setLoading(false);
        }
    }, [actionType, actorUserId, createdFrom, createdTo, debouncedQuery, page, targetId, targetType]);

    useEffect(() => {
        void loadAuditLogs();
    }, [loadAuditLogs]);

    useEffect(() => {
        setPage(0);
    }, [actionType, actorUserId, createdFrom, createdTo, debouncedQuery, targetId, targetType]);

    const columns: DataTableColumn<AuditLogEntry>[] = [
        {
            key: "time",
            header: "When",
            render: (log) => formatDateTime(log.createdAt),
        },
        {
            key: "actor",
            header: "Actor",
            render: (log) => `User #${log.actorUserId}`,
        },
        {
            key: "action",
            header: "Action",
            render: (log) => (
                <div>
                    <p className="font-bold text-slate-900">{formatCode(log.actionType)}</p>
                    <p className="mt-1 text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">
                        {formatCode(log.targetType)} #{log.targetId}
                    </p>
                </div>
            ),
        },
        {
            key: "summary",
            header: "Summary",
            render: (log) => (
                <div>
                    <p className="font-medium text-slate-900">{log.summary}</p>
                    <div className="mt-3">
                        <DetailBlock log={log} />
                    </div>
                </div>
            ),
        },
    ];

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-2xl px-6 py-10">
                <PageHeader
                    eyebrow="Admin Audit"
                    title="Audit logs"
                    description="Review privileged actions, lifecycle changes, onboarding decisions, and account operations."
                    actions={<div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm">{totalElements} matching event{totalElements === 1 ? "" : "s"}</div>}
                />

                <div className="mt-6 space-y-6">
                    <DataToolbar
                        filters={(
                            <>
                                <SearchInput value={query} onChange={setQuery} placeholder="Search audit details" />
                                <SelectFilter label="Action" value={actionType} onChange={setActionType} options={ACTION_OPTIONS} />
                                <SelectFilter label="Target" value={targetType} onChange={setTargetType} options={TARGET_OPTIONS} />
                                <label className="flex min-w-[130px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                    <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">Actor</span>
                                    <input value={actorUserId} onChange={(event) => setActorUserId(event.target.value)} inputMode="numeric" placeholder="ID" className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                </label>
                                <label className="flex min-w-[130px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                    <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">Target</span>
                                    <input value={targetId} onChange={(event) => setTargetId(event.target.value)} inputMode="numeric" placeholder="ID" className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                </label>
                                <label className="flex min-w-[165px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                    <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">From</span>
                                    <input type="date" value={createdFrom} onChange={(event) => setCreatedFrom(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                </label>
                                <label className="flex min-w-[165px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                    <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">To</span>
                                    <input type="date" value={createdTo} onChange={(event) => setCreatedTo(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                </label>
                            </>
                        )}
                        actions={<div className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-semibold text-slate-600">{logs.length} displayed</div>}
                    />

                    {error ? (
                        <ErrorState description={error} onRetry={() => void loadAuditLogs()} />
                    ) : loading ? (
                        <LoadingState label="Loading audit logs..." />
                    ) : logs.length === 0 ? (
                        <EmptyState title="No audit logs found" description="Adjust filters to inspect privileged activity." icon="manage_search" />
                    ) : (
                        <>
                            <div className="hidden lg:block">
                                <DataTable columns={columns} items={logs} getRowKey={(log) => log.id} />
                            </div>

                            <ResponsiveDataList
                                items={logs}
                                getKey={(log) => log.id}
                                renderItem={(log) => (
                                    <article className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
                                        <p className="text-xs font-bold uppercase tracking-[0.16em] text-slate-400">{formatDateTime(log.createdAt)}</p>
                                        <h2 className="mt-2 text-lg font-bold text-slate-950">{formatCode(log.actionType)}</h2>
                                        <p className="mt-1 text-sm text-slate-500">User #{log.actorUserId} on {formatCode(log.targetType)} #{log.targetId}</p>
                                        <p className="mt-4 text-sm font-medium text-slate-900">{log.summary}</p>
                                        <div className="mt-4 rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                            <DetailBlock log={log} />
                                        </div>
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
