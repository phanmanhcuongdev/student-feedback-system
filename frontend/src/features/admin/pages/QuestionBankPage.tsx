import { useCallback, useEffect, useState } from "react";
import {
    archiveQuestionBankEntry,
    createQuestionBankEntry,
    getQuestionBankEntries,
    restoreQuestionBankEntry,
    updateQuestionBankEntry,
} from "../../../api/adminApi";
import { getApiErrorMessage } from "../../../api/apiError";
import DataToolbar from "../../../components/data-view/DataToolbar";
import PaginationControls from "../../../components/data-view/PaginationControls";
import SearchInput from "../../../components/data-view/SearchInput";
import SelectFilter from "../../../components/data-view/SelectFilter";
import EmptyState from "../../../components/ui/EmptyState";
import ErrorState from "../../../components/ui/ErrorState";
import LoadingState from "../../../components/ui/LoadingState";
import PageHeader from "../../../components/ui/PageHeader";
import SectionCard from "../../../components/ui/SectionCard";
import type { QuestionBankEntry } from "../../../types/survey";

type QuestionBankForm = {
    content: string;
    type: "RATING" | "TEXT";
    category: string;
};

const emptyForm: QuestionBankForm = { content: "", type: "RATING", category: "" };

export default function QuestionBankPage() {
    const [items, setItems] = useState<QuestionBankEntry[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [feedback, setFeedback] = useState("");
    const [keyword, setKeyword] = useState("");
    const [debouncedKeyword, setDebouncedKeyword] = useState("");
    const [typeFilter, setTypeFilter] = useState("ALL");
    const [activeFilter, setActiveFilter] = useState("ACTIVE");
    const [categoryFilter, setCategoryFilter] = useState("");
    const [debouncedCategoryFilter, setDebouncedCategoryFilter] = useState("");
    const [editingId, setEditingId] = useState<number | null>(null);
    const [form, setForm] = useState(emptyForm);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [page, setPage] = useState(0);
    const pageSize = 12;

    const load = useCallback(async () => {
        try {
            setLoading(true);
            setError("");
            const response = await getQuestionBankEntries({
                keyword: debouncedKeyword || undefined,
                type: typeFilter === "ALL" ? undefined : typeFilter,
                active: activeFilter === "ALL" ? undefined : activeFilter === "ACTIVE",
                category: debouncedCategoryFilter || undefined,
                page,
                size: pageSize,
            });
            if (response.items.length === 0 && response.totalPages > 0 && page >= response.totalPages) {
                setPage(response.totalPages - 1);
                return;
            }
            setItems(response.items);
            setTotalElements(response.totalElements);
            setTotalPages(response.totalPages);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to load question bank."));
        } finally {
            setLoading(false);
        }
    }, [activeFilter, debouncedCategoryFilter, debouncedKeyword, page, typeFilter]);

    useEffect(() => {
        const timeout = window.setTimeout(() => setDebouncedKeyword(keyword.trim()), 300);
        return () => window.clearTimeout(timeout);
    }, [keyword]);

    useEffect(() => {
        const timeout = window.setTimeout(() => setDebouncedCategoryFilter(categoryFilter.trim()), 300);
        return () => window.clearTimeout(timeout);
    }, [categoryFilter]);

    useEffect(() => {
        void load();
    }, [load]);

    useEffect(() => {
        setPage(0);
    }, [activeFilter, debouncedCategoryFilter, debouncedKeyword, typeFilter]);

    function edit(item: QuestionBankEntry) {
        setEditingId(item.id);
        setForm({ content: item.content, type: item.type, category: item.category || "" });
    }

    function resetForm() {
        setEditingId(null);
        setForm(emptyForm);
    }

    async function submit(event: React.FormEvent) {
        event.preventDefault();
        setError("");
        setFeedback("");
        if (!form.content.trim()) {
            setError("Question content is required.");
            return;
        }

        try {
            setSaving(true);
            const payload = {
                content: form.content.trim(),
                type: form.type,
                category: form.category.trim() || null,
            };
            if (editingId) {
                await updateQuestionBankEntry(editingId, payload);
                setFeedback("Question-bank entry updated.");
            } else {
                await createQuestionBankEntry(payload);
                setFeedback("Question-bank entry created.");
            }
            resetForm();
            await load();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to save question-bank entry."));
        } finally {
            setSaving(false);
        }
    }

    async function setActive(item: QuestionBankEntry, active: boolean) {
        try {
            setError("");
            setFeedback("");
            if (active) {
                await restoreQuestionBankEntry(item.id);
                setFeedback("Question-bank entry restored.");
            } else {
                await archiveQuestionBankEntry(item.id);
                setFeedback("Question-bank entry archived.");
            }
            await load();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to update question-bank entry."));
        }
    }

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-xl px-6 py-10">
                <PageHeader
                    eyebrow="Admin / Survey Assets"
                    title="Question bank"
                    description="Create reusable question assets that can be copied into draft surveys and survey templates."
                />

                <div className="mt-6 space-y-6">
                    {error ? <ErrorState description={error} onRetry={() => void load()} /> : null}
                    {feedback ? <div className="rounded-[24px] border border-emerald-200 bg-emerald-50 px-5 py-4 text-sm font-medium text-emerald-700">{feedback}</div> : null}

                    <SectionCard title={editingId ? "Edit question asset" : "Create question asset"} description="Question-bank entries are reusable source questions. Archiving keeps old survey copies intact.">
                        <p className="mb-4 text-sm text-slate-500">Adding a bank question to a survey or template copies its current text and type. Later edits here affect future use only.</p>
                        <form onSubmit={submit} className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_180px_220px_auto]">
                            <input value={form.content} onChange={(event) => setForm((current) => ({ ...current, content: event.target.value }))} placeholder="Question content" className="rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-slate-500" />
                            <select value={form.type} onChange={(event) => setForm((current) => ({ ...current, type: event.target.value as "RATING" | "TEXT" }))} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-slate-500">
                                <option value="RATING">Rating</option>
                                <option value="TEXT">Text</option>
                            </select>
                            <input value={form.category} onChange={(event) => setForm((current) => ({ ...current, category: event.target.value }))} placeholder="Category" className="rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-slate-500" />
                            <div className="flex gap-2">
                                <button type="submit" disabled={saving} className="rounded-2xl bg-slate-950 px-5 py-3 text-sm font-semibold text-white disabled:opacity-60">{saving ? "Saving..." : editingId ? "Update" : "Create"}</button>
                                {editingId ? <button type="button" onClick={resetForm} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700">Cancel</button> : null}
                            </div>
                        </form>
                    </SectionCard>

                    <SectionCard title="Reusable questions" description="Search and manage active or archived question-bank entries.">
                        <div className="mb-4">
                            <DataToolbar
                                filters={(
                                    <>
                                        <SearchInput value={keyword} onChange={setKeyword} placeholder="Search content or category" />
                                        <SelectFilter
                                            label="Type"
                                            value={typeFilter}
                                            onChange={setTypeFilter}
                                            options={[
                                                { label: "All types", value: "ALL" },
                                                { label: "Rating", value: "RATING" },
                                                { label: "Text", value: "TEXT" },
                                            ]}
                                        />
                                        <SelectFilter
                                            label="State"
                                            value={activeFilter}
                                            onChange={setActiveFilter}
                                            options={[
                                                { label: "Active", value: "ACTIVE" },
                                                { label: "Archived", value: "ARCHIVED" },
                                                { label: "All states", value: "ALL" },
                                            ]}
                                        />
                                        <label className="flex min-w-[210px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                            <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">Category</span>
                                            <input value={categoryFilter} onChange={(event) => setCategoryFilter(event.target.value)} placeholder="Any" className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                        </label>
                                    </>
                                )}
                                actions={<div className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-semibold text-slate-600">{items.length} displayed</div>}
                            />
                        </div>
                        <p className="mb-4 text-sm text-slate-500">Showing {items.length} of {totalElements} question-bank entries.</p>
                        {loading ? <LoadingState label="Loading question bank..." /> : (
                            items.length === 0 ? (
                                <EmptyState title="No matching question-bank entries" description="Adjust search, type, state, or category filters." icon="quiz" />
                            ) : (
                                <>
                                    <div className="grid gap-3">
                                        {items.map((item) => (
                                            <div key={item.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                                <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                                                    <div>
                                                        <p className="font-bold text-slate-950">{item.content}</p>
                                                        <p className="mt-1 text-sm text-slate-500">{item.type}{item.category ? ` | ${item.category}` : ""} | {item.active ? "Active" : "Archived"}</p>
                                                    </div>
                                                    <div className="flex gap-2">
                                                        <button type="button" onClick={() => edit(item)} className="rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700">Edit</button>
                                                        <button type="button" onClick={() => void setActive(item, !item.active)} className="rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700">{item.active ? "Archive" : "Restore"}</button>
                                                    </div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                    <div className="mt-4">
                                        <PaginationControls page={page + 1} pageCount={Math.max(totalPages, 1)} onPageChange={(nextPage) => setPage(nextPage - 1)} />
                                    </div>
                                </>
                            )
                        )}
                    </SectionCard>
                </div>
            </div>
        </main>
    );
}
