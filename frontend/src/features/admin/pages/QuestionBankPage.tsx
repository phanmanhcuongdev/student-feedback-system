import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
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
    const { t } = useTranslation(["admin", "validation", "common"]);
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
            setError(getApiErrorMessage(requestError, t("admin:admin.questionBank.errors.load")));
        } finally {
            setLoading(false);
        }
    }, [activeFilter, debouncedCategoryFilter, debouncedKeyword, page, typeFilter, t]);

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
            setError(t("validation:validation.admin.questionBank.contentRequired"));
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
                setFeedback(t("admin:admin.questionBank.feedback.updated"));
            } else {
                await createQuestionBankEntry(payload);
                setFeedback(t("admin:admin.questionBank.feedback.created"));
            }
            resetForm();
            await load();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.questionBank.errors.save")));
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
                setFeedback(t("admin:admin.questionBank.feedback.restored"));
            } else {
                await archiveQuestionBankEntry(item.id);
                setFeedback(t("admin:admin.questionBank.feedback.archived"));
            }
            await load();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.questionBank.errors.update")));
        }
    }

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-xl px-6 py-10">
                <PageHeader
                    eyebrow={t("admin:admin.assets.eyebrow")}
                    title={t("admin:admin.questionBank.header.title")}
                    description={t("admin:admin.questionBank.header.description")}
                />

                <div className="mt-6 space-y-6">
                    {error ? <ErrorState description={error} onRetry={() => void load()} /> : null}
                    {feedback ? <div className="rounded-[24px] border border-emerald-200 bg-emerald-50 px-5 py-4 text-sm font-medium text-emerald-700">{feedback}</div> : null}

                    <SectionCard title={editingId ? t("admin:admin.questionBank.form.editTitle") : t("admin:admin.questionBank.form.createTitle")} description={t("admin:admin.questionBank.form.description")}>
                        <p className="mb-4 text-sm text-slate-500">{t("admin:admin.questionBank.form.copyHelp")}</p>
                        <form onSubmit={submit} className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_180px_220px_auto]">
                            <input value={form.content} onChange={(event) => setForm((current) => ({ ...current, content: event.target.value }))} placeholder={t("admin:admin.questionBank.form.contentPlaceholder")} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-slate-500" />
                            <select value={form.type} onChange={(event) => setForm((current) => ({ ...current, type: event.target.value as "RATING" | "TEXT" }))} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-slate-500">
                                <option value="RATING">{t("admin:admin.questionBank.type.rating")}</option>
                                <option value="TEXT">{t("admin:admin.questionBank.type.text")}</option>
                            </select>
                            <input value={form.category} onChange={(event) => setForm((current) => ({ ...current, category: event.target.value }))} placeholder={t("admin:admin.questionBank.form.categoryPlaceholder")} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-slate-500" />
                            <div className="flex gap-2">
                                <button type="submit" disabled={saving} className="rounded-2xl bg-slate-950 px-5 py-3 text-sm font-semibold text-white disabled:opacity-60">{saving ? t("admin:admin.surveys.form.buttons.saving") : editingId ? t("admin:admin.questionBank.buttons.update") : t("admin:admin.questionBank.buttons.create")}</button>
                                {editingId ? <button type="button" onClick={resetForm} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700">{t("common:common.actions.cancel")}</button> : null}
                            </div>
                        </form>
                    </SectionCard>

                    <SectionCard title={t("admin:admin.questionBank.list.title")} description={t("admin:admin.questionBank.list.description")}>
                        <div className="mb-4">
                            <DataToolbar
                                filters={(
                                    <>
                                        <SearchInput value={keyword} onChange={setKeyword} placeholder={t("admin:admin.questionBank.filters.search")} />
                                        <SelectFilter
                                            label={t("admin:admin.surveys.form.fields.type")}
                                            value={typeFilter}
                                            onChange={setTypeFilter}
                                            options={[
                                                { label: t("admin:admin.questionBank.filters.allTypes"), value: "ALL" },
                                                { label: t("admin:admin.questionBank.type.rating"), value: "RATING" },
                                                { label: t("admin:admin.questionBank.type.text"), value: "TEXT" },
                                            ]}
                                        />
                                        <SelectFilter
                                            label={t("admin:admin.questionBank.filters.state")}
                                            value={activeFilter}
                                            onChange={setActiveFilter}
                                            options={[
                                                { label: t("admin:admin.users.filters.active"), value: "ACTIVE" },
                                                { label: t("admin:admin.surveys.list.filters.archived"), value: "ARCHIVED" },
                                                { label: t("admin:admin.users.filters.allStates"), value: "ALL" },
                                            ]}
                                        />
                                        <label className="flex min-w-[210px] items-center gap-3 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-700 shadow-sm">
                                            <span className="shrink-0 text-xs font-bold uppercase tracking-[0.16em] text-slate-400">{t("admin:admin.questionBank.form.categoryPlaceholder")}</span>
                                            <input value={categoryFilter} onChange={(event) => setCategoryFilter(event.target.value)} placeholder={t("admin:admin.questionBank.filters.any")} className="w-full border-0 bg-transparent p-0 text-sm font-medium text-slate-900 outline-none" />
                                        </label>
                                    </>
                                )}
                                actions={<div className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-semibold text-slate-600">{t("admin:admin.audit.displayed", { count: items.length })}</div>}
                            />
                        </div>
                        <p className="mb-4 text-sm text-slate-500">{t("admin:admin.questionBank.list.showing", { shown: items.length, total: totalElements })}</p>
                        {loading ? <LoadingState label={t("admin:admin.questionBank.loading")} /> : (
                            items.length === 0 ? (
                                <EmptyState title={t("admin:admin.questionBank.empty.title")} description={t("admin:admin.questionBank.empty.description")} icon="quiz" />
                            ) : (
                                <>
                                    <div className="grid gap-3">
                                        {items.map((item) => (
                                            <div key={item.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                                <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                                                    <div>
                                                        <p className="font-bold text-slate-950">{item.content}</p>
                                                        <p className="mt-1 text-sm text-slate-500">{item.type}{item.category ? ` | ${item.category}` : ""} | {item.active ? t("admin:admin.users.filters.active") : t("admin:admin.surveys.list.filters.archived")}</p>
                                                    </div>
                                                    <div className="flex gap-2">
                                                        <button type="button" onClick={() => edit(item)} className="rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700">{t("admin:admin.questionBank.buttons.edit")}</button>
                                                        <button type="button" onClick={() => void setActive(item, !item.active)} className="rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700">{item.active ? t("admin:admin.questionBank.buttons.archive") : t("admin:admin.questionBank.buttons.restore")}</button>
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
