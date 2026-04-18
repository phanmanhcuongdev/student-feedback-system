import { useCallback, useEffect, useState } from "react";
import DataTable, { type DataTableColumn } from "../../../components/data-view/DataTable";
import DataToolbar from "../../../components/data-view/DataToolbar";
import PaginationControls from "../../../components/data-view/PaginationControls";
import ResponsiveDataList from "../../../components/data-view/ResponsiveDataList";
import SearchInput from "../../../components/data-view/SearchInput";
import SelectFilter from "../../../components/data-view/SelectFilter";
import DetailPanel from "../../../components/ui/DetailPanel";
import EmptyState from "../../../components/ui/EmptyState";
import ErrorState from "../../../components/ui/ErrorState";
import LoadingState from "../../../components/ui/LoadingState";
import PageHeader from "../../../components/ui/PageHeader";
import SectionCard from "../../../components/ui/SectionCard";
import StatusBadge from "../../../components/ui/StatusBadge";
import { darkActionButtonClass, darkActionButtonStyle } from "../../../components/ui/buttonStyles";
import { getApiErrorMessage } from "../../../api/apiError";
import { getAllFeedback, respondToFeedback } from "../../../api/feedbackApi";
import type { FeedbackResponse, StaffFeedback } from "../../../types/feedback";

function formatDate(date: string) {
    return new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    }).format(new Date(date));
}

function getFeedbackStatus(item: StaffFeedback) {
    return item.responses.length > 0 ? "RESPONDED" : "UNRESOLVED";
}

export default function ManageFeedbackPage() {
    const [items, setItems] = useState<StaffFeedback[]>([]);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [query, setQuery] = useState("");
    const [debouncedQuery, setDebouncedQuery] = useState("");
    const [statusFilter, setStatusFilter] = useState("ALL");
    const [dateFilter, setDateFilter] = useState("");
    const [sortBy, setSortBy] = useState("createdAt");
    const [sortDir, setSortDir] = useState("desc");
    const [page, setPage] = useState(0);
    const [activeFeedbackId, setActiveFeedbackId] = useState<number | null>(null);
    const [drafts, setDrafts] = useState<Record<number, string>>({});
    const [submittingId, setSubmittingId] = useState<number | null>(null);

    const pageSize = 10;

    const loadFeedback = useCallback(async () => {
        try {
            setLoading(true);
            setError("");
            const response = await getAllFeedback({
                keyword: debouncedQuery || undefined,
                status: statusFilter === "ALL" ? undefined : statusFilter,
                createdDate: dateFilter || undefined,
                page,
                size: pageSize,
                sortBy,
                sortDir,
            });

            if (response.items.length === 0 && response.totalPages > 0 && page >= response.totalPages) {
                setPage(response.totalPages - 1);
                return;
            }

            setItems(response.items);
            setTotalElements(response.totalElements);
            setTotalPages(response.totalPages);
            setActiveFeedbackId((current) => {
                if (current != null && response.items.some((item) => item.id === current)) {
                    return current;
                }
                return response.items[0]?.id ?? null;
            });
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to load feedback."));
        } finally {
            setLoading(false);
        }
    }, [dateFilter, debouncedQuery, page, sortBy, sortDir, statusFilter]);

    useEffect(() => {
        const timeout = window.setTimeout(() => {
            setDebouncedQuery(query.trim());
        }, 300);

        return () => window.clearTimeout(timeout);
    }, [query]);

    useEffect(() => {
        void loadFeedback();
    }, [loadFeedback]);

    async function handleRespond(feedbackId: number) {
        const content = drafts[feedbackId]?.trim() ?? "";
        if (!content) {
            setError("Response content is required.");
            setActiveFeedbackId(feedbackId);
            return;
        }

        try {
            setSubmittingId(feedbackId);
            setError("");
            const response = await respondToFeedback(feedbackId, content);
            if (!response.success) {
                setError(response.message || "Unable to submit response.");
                return;
            }

            setDrafts((current) => ({ ...current, [feedbackId]: "" }));
            await loadFeedback();
            setActiveFeedbackId(feedbackId);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to submit response right now."));
        } finally {
            setSubmittingId(null);
        }
    }

    useEffect(() => {
        setPage(0);
    }, [debouncedQuery, statusFilter, dateFilter, sortBy, sortDir]);

    const activeItem = items.find((item) => item.id === activeFeedbackId) ?? items[0] ?? null;

    useEffect(() => {
        if (!activeItem && items[0]) {
            setActiveFeedbackId(items[0].id);
        }
    }, [activeItem, items]);

    const columns: DataTableColumn<StaffFeedback>[] = [
        {
            key: "student",
            header: "Student",
            render: (item) => (
                <div>
                    <p className="font-bold text-slate-950">{item.studentName}</p>
                    <p className="mt-1 text-sm text-slate-500">{item.studentEmail || "No email"}</p>
                </div>
            ),
        },
        {
            key: "title",
            header: "Feedback",
            render: (item) => (
                <div>
                    <p className="font-semibold text-slate-900">{item.title}</p>
                    <p className="mt-1 line-clamp-2 max-w-md text-sm text-slate-500">{item.content}</p>
                </div>
            ),
        },
        {
            key: "status",
            header: "Status",
            render: (item) => <StatusBadge kind="feedback" value={getFeedbackStatus(item)} />,
        },
        {
            key: "created",
            header: "Created",
            render: (item) => formatDate(item.createdAt),
        },
        {
            key: "actions",
            header: "Actions",
            className: "text-right",
            render: (item) => (
                <div className="flex justify-end">
                    <button type="button" onClick={() => setActiveFeedbackId(item.id)} className="inline-flex items-center rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                        Open queue item
                    </button>
                </div>
            ),
        },
    ];

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-2xl px-6 py-10">
                <PageHeader
                    eyebrow="Staff Feedback"
                    title="Review student feedback"
                    description="Work feedback as a queue with clear response status, searchable student context, and an in-place reply panel."
                    actions={<div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm">{totalElements} queue item{totalElements === 1 ? "" : "s"}</div>}
                />

                <div className="mt-6 space-y-6">
                    <DataToolbar
                        filters={(
                            <>
                                <SearchInput value={query} onChange={setQuery} placeholder="Search by student, title, or feedback content" />
                                <SelectFilter label="Status" value={statusFilter} onChange={setStatusFilter} options={[{ label: "All statuses", value: "ALL" }, { label: "Unresolved", value: "UNRESOLVED" }, { label: "Responded", value: "RESPONDED" }]} />
                                <label className="flex min-w-[170px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                    <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">Date</span>
                                    <input type="date" value={dateFilter} onChange={(event) => setDateFilter(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
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
                                        { label: "Newest first", value: "createdAt:desc" },
                                        { label: "Oldest first", value: "createdAt:asc" },
                                    ]}
                                />
                            </>
                        )}
                    />

                    {error ? (
                        <ErrorState description={error} onRetry={() => void loadFeedback()} />
                    ) : loading ? (
                        <LoadingState label="Loading feedback..." />
                    ) : items.length === 0 ? (
                        <EmptyState title="No feedback in this queue view" description="Adjust the filters or search terms to find the student feedback item you need." icon="forum" />
                    ) : (
                        <>
                            <div className="hidden lg:block">
                                <DataTable columns={columns} items={items} getRowKey={(item) => item.id} />
                            </div>

                            <ResponsiveDataList
                                items={items}
                                getKey={(item) => item.id}
                                renderItem={(item) => (
                                    <article className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
                                        <div className="flex items-start justify-between gap-4">
                                            <div>
                                                <StatusBadge kind="feedback" value={getFeedbackStatus(item)} />
                                                <h2 className="mt-3 text-xl font-bold text-slate-950">{item.title}</h2>
                                                <p className="mt-2 text-sm text-slate-500">{item.studentName} | {item.studentEmail || "No email"}</p>
                                            </div>
                                            <span className="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">{formatDate(item.createdAt)}</span>
                                        </div>
                                        <p className="mt-4 text-sm leading-6 text-slate-600">{item.content}</p>
                                        <button type="button" onClick={() => setActiveFeedbackId(item.id)} className="mt-5 inline-flex w-full items-center justify-center rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                                            Open queue item
                                        </button>
                                    </article>
                                )}
                            />

                            <PaginationControls page={page + 1} pageCount={Math.max(totalPages, 1)} onPageChange={(nextPage) => setPage(nextPage - 1)} />

                            {activeItem ? (
                                <SectionCard title="Feedback detail" description="Respond in the same queue context without losing the list position.">
                                    <div className="grid gap-6 xl:grid-cols-[320px_minmax(0,1fr)]">
                                        <DetailPanel
                                            title="Context"
                                            items={[
                                                { label: "Student", value: activeItem.studentName },
                                                { label: "Email", value: activeItem.studentEmail || "No email" },
                                                { label: "Status", value: <StatusBadge kind="feedback" value={getFeedbackStatus(activeItem)} /> },
                                                { label: "Created", value: formatDate(activeItem.createdAt) },
                                            ]}
                                        />

                                        <div className="space-y-5">
                                            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
                                                <h3 className="text-xl font-bold text-slate-950">{activeItem.title}</h3>
                                                <p className="mt-3 text-sm leading-6 text-slate-600">{activeItem.content}</p>
                                            </div>

                                            <SectionCard title="Responses" description="Existing replies remain visible above the current draft.">
                                                {activeItem.responses.length === 0 ? (
                                                    <EmptyState title="No responses yet" description="This feedback item is still unresolved." icon="reply" />
                                                ) : (
                                                    <div className="space-y-3">
                                                        {activeItem.responses.map((response) => (
                                                            <ResponseCard key={response.id} response={response} />
                                                        ))}
                                                    </div>
                                                )}
                                            </SectionCard>

                                            <SectionCard title="Reply" description="Responses are added to the shared reply stream for this feedback item.">
                                                <label className="block space-y-2">
                                                    <span className="text-sm font-semibold text-slate-700">Response</span>
                                                    <textarea
                                                        rows={5}
                                                        value={drafts[activeItem.id] ?? ""}
                                                        onChange={(event) => setDrafts((current) => ({ ...current, [activeItem.id]: event.target.value }))}
                                                        className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5"
                                                    />
                                                </label>
                                                <div className="flex justify-end">
                                                    <button type="button" onClick={() => void handleRespond(activeItem.id)} disabled={submittingId === activeItem.id} className={`${darkActionButtonClass} px-5 py-3 text-sm font-semibold`} style={darkActionButtonStyle}>
                                                        <span className="text-white" style={darkActionButtonStyle}>
                                                            {submittingId === activeItem.id ? "Sending..." : "Send response"}
                                                        </span>
                                                    </button>
                                                </div>
                                            </SectionCard>
                                        </div>
                                    </div>
                                </SectionCard>
                            ) : null}
                        </>
                    )}
                </div>
            </div>
        </main>
    );
}

function ResponseCard({ response }: { response: FeedbackResponse }) {
    return (
        <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
            <div className="mb-2 flex items-center justify-between gap-3">
                <div>
                    <p className="text-sm font-bold text-slate-900">{response.responderEmail}</p>
                    <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-slate-500">{response.responderRole}</p>
                </div>
                <span className="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">{formatDate(response.createdAt)}</span>
            </div>
            <p className="text-sm leading-6 text-slate-600">{response.content}</p>
        </div>
    );
}
