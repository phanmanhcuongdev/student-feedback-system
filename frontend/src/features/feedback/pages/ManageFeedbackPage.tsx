import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
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
import SmartTextDisplay from "../../../components/ui/SmartTextDisplay";
import StatusBadge from "../../../components/ui/StatusBadge";
import { darkActionButtonClass, darkActionButtonStyle } from "../../../components/ui/buttonStyles";
import { getApiErrorMessage } from "../../../api/apiError";
import { getAllFeedback, respondToFeedback } from "../../../api/feedbackApi";
import type { FeedbackResponse, StaffFeedback } from "../../../types/feedback";

const TRANSLATION_POLL_INTERVAL_MS = 5000;

function formatDate(date: string, language: string) {
    return new Intl.DateTimeFormat(language === "vi" ? "vi-VN" : "en-GB", {
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

function hasPendingTranslations(items: StaffFeedback[]) {
    return items.some((item) => !item.isAutoTranslated && !item.contentTranslated);
}

export default function ManageFeedbackPage() {
    const { i18n, t } = useTranslation(["feedback", "validation"]);
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

    const loadFeedback = useCallback(async (options: { silent?: boolean } = {}) => {
        try {
            if (!options.silent) {
                setLoading(true);
            }
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
            setError(getApiErrorMessage(requestError, t("feedback:feedback.staff.errors.load")));
        } finally {
            if (!options.silent) {
                setLoading(false);
            }
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

    useEffect(() => {
        if (!hasPendingTranslations(items)) {
            return;
        }

        const intervalId = window.setInterval(() => {
            void loadFeedback({ silent: true });
        }, TRANSLATION_POLL_INTERVAL_MS);

        return () => window.clearInterval(intervalId);
    }, [items, loadFeedback]);

    async function handleRespond(feedbackId: number) {
        const content = drafts[feedbackId]?.trim() ?? "";
        if (!content) {
            setError(t("validation:validation.feedback.responseRequired"));
            setActiveFeedbackId(feedbackId);
            return;
        }

        try {
            setSubmittingId(feedbackId);
            setError("");
            const response = await respondToFeedback(feedbackId, content);
            if (!response.success) {
                setError(response.message || t("feedback:feedback.staff.errors.submit"));
                return;
            }

            setDrafts((current) => ({ ...current, [feedbackId]: "" }));
            await loadFeedback();
            setActiveFeedbackId(feedbackId);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("feedback:feedback.staff.errors.submitUnavailable")));
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
            header: t("feedback:feedback.staff.table.student"),
            render: (item) => (
                <div>
                    <p className="font-bold text-slate-950">{item.studentName}</p>
                    <p className="mt-1 text-sm text-slate-500">{item.studentEmail || t("feedback:feedback.common.noEmail")}</p>
                </div>
            ),
        },
        {
            key: "title",
            header: t("feedback:feedback.staff.table.feedback"),
            render: (item) => (
                <div>
                    <p className="font-semibold text-slate-900">{item.title}</p>
                    <p className="mt-1 line-clamp-2 max-w-md text-sm text-slate-500">{item.displayContent}</p>
                </div>
            ),
        },
        {
            key: "status",
            header: t("feedback:feedback.staff.table.status"),
            render: (item) => <StatusBadge kind="feedback" value={getFeedbackStatus(item)} />,
        },
        {
            key: "created",
            header: t("feedback:feedback.staff.table.created"),
            render: (item) => formatDate(item.createdAt, i18n.language),
        },
        {
            key: "actions",
            header: t("feedback:feedback.staff.table.actions"),
            className: "text-right",
            render: (item) => (
                <div className="flex justify-end">
                    <button type="button" onClick={() => setActiveFeedbackId(item.id)} className="inline-flex items-center rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                        {t("feedback:feedback.staff.buttons.openQueueItem")}
                    </button>
                </div>
            ),
        },
    ];

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-2xl px-6 py-10">
                <PageHeader
                    eyebrow={t("feedback:feedback.staff.header.eyebrow")}
                    title={t("feedback:feedback.staff.header.title")}
                    description={t("feedback:feedback.staff.header.description")}
                    actions={<div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm">{t("feedback:feedback.staff.header.queueCount", { count: totalElements })}</div>}
                />

                <div className="mt-6 space-y-6">
                    <DataToolbar
                        filters={(
                            <>
                                <SearchInput value={query} onChange={setQuery} placeholder={t("feedback:feedback.staff.filters.search")} />
                                <SelectFilter label={t("feedback:feedback.staff.filters.status")} value={statusFilter} onChange={setStatusFilter} options={[{ label: t("feedback:feedback.staff.filters.allStatuses"), value: "ALL" }, { label: t("feedback:feedback.staff.filters.unresolved"), value: "UNRESOLVED" }, { label: t("feedback:feedback.staff.filters.responded"), value: "RESPONDED" }]} />
                                <label className="flex min-w-[170px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                    <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">{t("feedback:feedback.staff.filters.date")}</span>
                                    <input type="date" value={dateFilter} onChange={(event) => setDateFilter(event.target.value)} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                </label>
                                <SelectFilter
                                    label={t("feedback:feedback.staff.filters.sort")}
                                    value={`${sortBy}:${sortDir}`}
                                    onChange={(value) => {
                                        const [nextSortBy, nextSortDir] = value.split(":");
                                        setSortBy(nextSortBy);
                                        setSortDir(nextSortDir);
                                    }}
                                    options={[
                                        { label: t("feedback:feedback.staff.filters.newestFirst"), value: "createdAt:desc" },
                                        { label: t("feedback:feedback.staff.filters.oldestFirst"), value: "createdAt:asc" },
                                    ]}
                                />
                            </>
                        )}
                    />

                    {error ? (
                        <ErrorState description={error} onRetry={() => void loadFeedback()} />
                    ) : loading ? (
                        <LoadingState label={t("feedback:feedback.staff.loading")} />
                    ) : items.length === 0 ? (
                        <EmptyState title={t("feedback:feedback.staff.empty.title")} description={t("feedback:feedback.staff.empty.description")} icon="forum" />
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
                                                <p className="mt-2 text-sm text-slate-500">{item.studentName} | {item.studentEmail || t("feedback:feedback.common.noEmail")}</p>
                                            </div>
                                            <span className="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">{formatDate(item.createdAt, i18n.language)}</span>
                                        </div>
                                        <div className="mt-4">
                                            <SmartTextDisplay
                                                displayContent={item.displayContent}
                                                originalContent={item.originalContent}
                                                contentTranslated={item.contentTranslated}
                                                isAutoTranslated={item.isAutoTranslated}
                                                sourceLang={item.sourceLang}
                                            />
                                        </div>
                                        <button type="button" onClick={() => setActiveFeedbackId(item.id)} className="mt-5 inline-flex w-full items-center justify-center rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                                            {t("feedback:feedback.staff.buttons.openQueueItem")}
                                        </button>
                                    </article>
                                )}
                            />

                            <PaginationControls page={page + 1} pageCount={Math.max(totalPages, 1)} onPageChange={(nextPage) => setPage(nextPage - 1)} />

                            {activeItem ? (
                                <SectionCard title={t("feedback:feedback.staff.detail.title")} description={t("feedback:feedback.staff.detail.description")}>
                                    <div className="grid gap-6 xl:grid-cols-[320px_minmax(0,1fr)]">
                                        <DetailPanel
                                            title={t("feedback:feedback.staff.detail.context.title")}
                                            items={[
                                                { label: t("feedback:feedback.staff.detail.context.student"), value: activeItem.studentName },
                                                { label: t("feedback:feedback.staff.detail.context.email"), value: activeItem.studentEmail || t("feedback:feedback.common.noEmail") },
                                                { label: t("feedback:feedback.staff.detail.context.status"), value: <StatusBadge kind="feedback" value={getFeedbackStatus(activeItem)} /> },
                                                { label: t("feedback:feedback.staff.detail.context.created"), value: formatDate(activeItem.createdAt, i18n.language) },
                                            ]}
                                        />

                                        <div className="space-y-5">
                                            <div className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
                                                <h3 className="text-xl font-bold text-slate-950">{activeItem.title}</h3>
                                                <SmartTextDisplay
                                                    className="mt-3"
                                                    displayContent={activeItem.displayContent}
                                                    originalContent={activeItem.originalContent}
                                                    contentTranslated={activeItem.contentTranslated}
                                                    isAutoTranslated={activeItem.isAutoTranslated}
                                                    sourceLang={activeItem.sourceLang}
                                                />
                                            </div>

                                            <SectionCard title={t("feedback:feedback.staff.responses.title")} description={t("feedback:feedback.staff.responses.description")}>
                                                {activeItem.responses.length === 0 ? (
                                                    <EmptyState title={t("feedback:feedback.staff.responses.empty.title")} description={t("feedback:feedback.staff.responses.empty.description")} icon="reply" />
                                                ) : (
                                                    <div className="space-y-3">
                                                        {activeItem.responses.map((response) => (
                                                            <ResponseCard key={response.id} response={response} language={i18n.language} />
                                                        ))}
                                                    </div>
                                                )}
                                            </SectionCard>

                                            <SectionCard title={t("feedback:feedback.staff.reply.title")} description={t("feedback:feedback.staff.reply.description")}>
                                                <label className="block space-y-2">
                                                    <span className="text-sm font-semibold text-slate-700">{t("feedback:feedback.staff.reply.field")}</span>
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
                                                            {submittingId === activeItem.id ? t("feedback:feedback.staff.buttons.sending") : t("feedback:feedback.staff.buttons.sendResponse")}
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

function ResponseCard({ response, language }: { response: FeedbackResponse; language: string }) {
    return (
        <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
            <div className="mb-2 flex items-center justify-between gap-3">
                <div>
                    <p className="text-sm font-bold text-slate-900">{response.responderEmail}</p>
                    <p className="text-[11px] font-semibold uppercase tracking-[0.16em] text-slate-500">{response.responderRole}</p>
                </div>
                <span className="text-xs font-semibold uppercase tracking-[0.14em] text-slate-400">{formatDate(response.createdAt, language)}</span>
            </div>
            <p className="text-sm leading-6 text-slate-600">{response.content}</p>
        </div>
    );
}
