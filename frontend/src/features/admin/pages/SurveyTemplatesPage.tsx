import { useCallback, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import {
    archiveSurveyTemplate,
    createSurveyTemplate,
    getQuestionBankEntries,
    getSurveyManagementDepartments,
    getSurveyTemplates,
    restoreSurveyTemplate,
    updateSurveyTemplate,
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
import type { DepartmentOption } from "../../../types/admin";
import type { CreateQuestionData, QuestionBankEntry, SurveyTemplate } from "../../../types/survey";

type TemplateForm = {
    name: string;
    description: string;
    suggestedTitle: string;
    suggestedSurveyDescription: string;
    recipientScope: "ALL_STUDENTS" | "DEPARTMENT";
    recipientDepartmentId: string;
    questions: CreateQuestionData[];
};

const emptyForm: TemplateForm = {
    name: "",
    description: "",
    suggestedTitle: "",
    suggestedSurveyDescription: "",
    recipientScope: "ALL_STUDENTS",
    recipientDepartmentId: "",
    questions: [],
};

export default function SurveyTemplatesPage() {
    const { t } = useTranslation(["admin", "validation", "common"]);
    const [templates, setTemplates] = useState<SurveyTemplate[]>([]);
    const [bank, setBank] = useState<QuestionBankEntry[]>([]);
    const [departments, setDepartments] = useState<DepartmentOption[]>([]);
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState("");
    const [feedback, setFeedback] = useState("");
    const [editingId, setEditingId] = useState<number | null>(null);
    const [editingWasArchived, setEditingWasArchived] = useState(false);
    const [form, setForm] = useState<TemplateForm>(emptyForm);
    const [totalTemplates, setTotalTemplates] = useState(0);
    const [totalTemplatePages, setTotalTemplatePages] = useState(0);
    const [templatePage, setTemplatePage] = useState(0);
    const [keyword, setKeyword] = useState("");
    const [debouncedKeyword, setDebouncedKeyword] = useState("");
    const [activeFilter, setActiveFilter] = useState("ACTIVE");
    const [bankTotalElements, setBankTotalElements] = useState(0);
    const pageSize = 10;

    const load = useCallback(async () => {
        try {
            setLoading(true);
            setError("");
            const [templateResponse, bankPage, departmentItems] = await Promise.all([
                getSurveyTemplates({
                    keyword: debouncedKeyword || undefined,
                    active: activeFilter === "ALL" ? undefined : activeFilter === "ACTIVE",
                    page: templatePage,
                    size: pageSize,
                }),
                getQuestionBankEntries({ active: true, size: 100 }),
                getSurveyManagementDepartments(),
            ]);
            if (templateResponse.items.length === 0 && templateResponse.totalPages > 0 && templatePage >= templateResponse.totalPages) {
                setTemplatePage(templateResponse.totalPages - 1);
                return;
            }
            setTemplates(templateResponse.items);
            setTotalTemplates(templateResponse.totalElements);
            setTotalTemplatePages(templateResponse.totalPages);
            setBank(bankPage.items);
            setBankTotalElements(bankPage.totalElements);
            setDepartments(departmentItems);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.templates.errors.load")));
        } finally {
            setLoading(false);
        }
    }, [activeFilter, debouncedKeyword, templatePage, t]);

    useEffect(() => {
        const timeout = window.setTimeout(() => setDebouncedKeyword(keyword.trim()), 300);
        return () => window.clearTimeout(timeout);
    }, [keyword]);

    useEffect(() => {
        void load();
    }, [load]);

    useEffect(() => {
        setTemplatePage(0);
    }, [activeFilter, debouncedKeyword]);

    function resetForm() {
        setEditingId(null);
        setEditingWasArchived(false);
        setForm(emptyForm);
    }

    function addBlankQuestion() {
        setForm((current) => ({ ...current, questions: [...current.questions, { content: "", type: "RATING", questionBankEntryId: null }] }));
    }

    function addBankQuestion(id: string) {
        const entry = bank.find((item) => item.id === Number(id));
        if (!entry) {
            return;
        }
        setForm((current) => ({
            ...current,
            questions: [...current.questions, { content: entry.content, type: entry.type, questionBankEntryId: entry.id }],
        }));
    }

    function edit(template: SurveyTemplate) {
        setEditingId(template.id);
        setEditingWasArchived(!template.active);
        setForm({
            name: template.name,
            description: template.description || "",
            suggestedTitle: template.suggestedTitle || "",
            suggestedSurveyDescription: template.suggestedSurveyDescription || "",
            recipientScope: template.recipientScope,
            recipientDepartmentId: template.recipientDepartmentId != null ? String(template.recipientDepartmentId) : "",
            questions: template.questions.map((question) => ({ content: question.content, type: question.type, questionBankEntryId: question.questionBankEntryId })),
        });
    }

    function updateQuestion(index: number, field: keyof CreateQuestionData, value: string | number | null) {
        setForm((current) => ({
            ...current,
            questions: current.questions.map((question, itemIndex) => itemIndex === index ? { ...question, [field]: value } : question),
        }));
    }

    async function submit(event: React.FormEvent) {
        event.preventDefault();
        setError("");
        setFeedback("");
        if (!form.name.trim()) {
            setError(t("validation:validation.admin.templates.nameRequired"));
            return;
        }
        if (form.questions.length === 0 || form.questions.some((question) => !question.content.trim())) {
            setError(t("validation:validation.admin.templates.questionRequired"));
            return;
        }

        const payload = {
            name: form.name.trim(),
            description: form.description.trim() || null,
            suggestedTitle: form.suggestedTitle.trim() || null,
            suggestedSurveyDescription: form.suggestedSurveyDescription.trim() || null,
            recipientScope: form.recipientScope,
            recipientDepartmentId: form.recipientScope === "DEPARTMENT" ? Number(form.recipientDepartmentId) : null,
            questions: form.questions.map((question) => ({
                content: question.content.trim(),
                type: question.type,
                questionBankEntryId: question.questionBankEntryId || null,
            })),
        };

        try {
            setSaving(true);
            if (editingId) {
                await updateSurveyTemplate(editingId, payload);
                setFeedback(t("admin:admin.templates.feedback.updated"));
            } else {
                await createSurveyTemplate(payload);
                setFeedback(t("admin:admin.templates.feedback.created"));
            }
            resetForm();
            await load();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.templates.errors.save")));
        } finally {
            setSaving(false);
        }
    }

    async function setActive(template: SurveyTemplate, active: boolean) {
        try {
            setError("");
            setFeedback("");
            if (active) {
                await restoreSurveyTemplate(template.id);
                setFeedback(t("admin:admin.templates.feedback.restored"));
            } else {
                await archiveSurveyTemplate(template.id);
                setFeedback(t("admin:admin.templates.feedback.archived"));
            }
            await load();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.templates.errors.update")));
        }
    }

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-xl px-6 py-10">
                <PageHeader eyebrow={t("admin:admin.assets.eyebrow")} title={t("admin:admin.templates.header.title")} description={t("admin:admin.templates.header.description")} />
                <div className="mt-6 space-y-6">
                    {error ? <ErrorState description={error} onRetry={() => void load()} /> : null}
                    {feedback ? <div className="rounded-[24px] border border-emerald-200 bg-emerald-50 px-5 py-4 text-sm font-medium text-emerald-700">{feedback}</div> : null}
                    <SectionCard title={editingId ? t("admin:admin.templates.form.editTitle") : t("admin:admin.templates.form.createTitle")} description={t("admin:admin.templates.form.description")}>
                        <form onSubmit={submit} className="space-y-4">
                            {editingWasArchived ? <div className="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-medium text-amber-800">{t("admin:admin.templates.form.archivedNotice")}</div> : null}
                            <div className="grid gap-4 md:grid-cols-2">
                                <input value={form.name} onChange={(event) => setForm((current) => ({ ...current, name: event.target.value }))} placeholder={t("admin:admin.templates.form.namePlaceholder")} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-slate-500" />
                                <input value={form.suggestedTitle} onChange={(event) => setForm((current) => ({ ...current, suggestedTitle: event.target.value }))} placeholder={t("admin:admin.templates.form.suggestedTitlePlaceholder")} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-slate-500" />
                            </div>
                            <textarea value={form.description} onChange={(event) => setForm((current) => ({ ...current, description: event.target.value }))} rows={2} placeholder={t("admin:admin.templates.form.descriptionPlaceholder")} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-slate-500" />
                            <textarea value={form.suggestedSurveyDescription} onChange={(event) => setForm((current) => ({ ...current, suggestedSurveyDescription: event.target.value }))} rows={3} placeholder={t("admin:admin.templates.form.suggestedDescriptionPlaceholder")} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-slate-500" />
                            <div className="grid gap-4 md:grid-cols-2">
                                <select value={form.recipientScope} onChange={(event) => setForm((current) => ({ ...current, recipientScope: event.target.value as "ALL_STUDENTS" | "DEPARTMENT" }))} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-slate-500">
                                    <option value="ALL_STUDENTS">{t("admin:admin.surveys.form.audience.allStudents")}</option>
                                    <option value="DEPARTMENT">{t("admin:admin.templates.form.departmentDefault")}</option>
                                </select>
                                <select value={form.recipientDepartmentId} onChange={(event) => setForm((current) => ({ ...current, recipientDepartmentId: event.target.value }))} disabled={form.recipientScope !== "DEPARTMENT"} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-slate-500 disabled:bg-slate-100">
                                    <option value="">{t("admin:admin.surveys.form.audience.selectDepartment")}</option>
                                    {departments.map((department) => <option key={department.id} value={department.id}>{department.name}</option>)}
                                </select>
                            </div>
                            <div className="flex flex-wrap gap-3">
                                <button type="button" onClick={addBlankQuestion} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700">{t("admin:admin.templates.form.addCustomQuestion")}</button>
                                <select defaultValue="" onChange={(event) => { addBankQuestion(event.target.value); event.currentTarget.value = ""; }} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700">
                                    <option value="">{t("admin:admin.surveys.form.questions.addFromBank")}</option>
                                    {bank.map((entry) => <option key={entry.id} value={entry.id}>{entry.content}</option>)}
                                </select>
                            </div>
                            <p className="text-sm text-slate-500">{t("admin:admin.templates.form.bankCopyHelp")} {bankTotalElements > bank.length ? t("admin:admin.templates.form.bankLimitHelp", { shown: bank.length, total: bankTotalElements }) : ""}</p>
                            <div className="grid gap-3">
                                {form.questions.map((question, index) => (
                                    <div key={index} className="grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 md:grid-cols-[minmax(0,1fr)_160px_auto]">
                                        <input value={question.content} onChange={(event) => updateQuestion(index, "content", event.target.value)} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-slate-500" />
                                        <select value={question.type} onChange={(event) => updateQuestion(index, "type", event.target.value)} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none focus:border-slate-500">
                                            <option value="RATING">{t("admin:admin.questionBank.type.rating")}</option>
                                            <option value="TEXT">{t("admin:admin.questionBank.type.text")}</option>
                                        </select>
                                        <button type="button" onClick={() => setForm((current) => ({ ...current, questions: current.questions.filter((_, itemIndex) => itemIndex !== index) }))} className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{t("admin:admin.surveys.form.questions.remove")}</button>
                                    </div>
                                ))}
                            </div>
                            <div className="flex justify-end gap-2">
                                {editingId ? <button type="button" onClick={resetForm} className="rounded-2xl border border-slate-300 bg-white px-5 py-3 text-sm font-semibold text-slate-700">{t("common:common.actions.cancel")}</button> : null}
                                <button type="submit" disabled={saving} className="rounded-2xl bg-slate-950 px-5 py-3 text-sm font-semibold text-white disabled:opacity-60">{saving ? t("admin:admin.surveys.form.buttons.saving") : editingId ? t("admin:admin.templates.buttons.update") : t("admin:admin.templates.buttons.create")}</button>
                            </div>
                        </form>
                    </SectionCard>
                    <SectionCard title={t("admin:admin.templates.list.title")} description={t("admin:admin.templates.list.description")}>
                        <div className="mb-4">
                            <DataToolbar
                                filters={(
                                    <>
                                        <SearchInput value={keyword} onChange={setKeyword} placeholder={t("admin:admin.templates.filters.search")} />
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
                                    </>
                                )}
                                actions={<div className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-semibold text-slate-600">{t("admin:admin.audit.displayed", { count: templates.length })}</div>}
                            />
                        </div>
                        <p className="mb-4 text-sm text-slate-500">{t("admin:admin.templates.list.showing", { shown: templates.length, total: totalTemplates })}</p>
                        {loading ? <LoadingState label={t("admin:admin.templates.loading")} /> : (
                            templates.length === 0 ? (
                                <EmptyState title={t("admin:admin.templates.empty.title")} description={t("admin:admin.templates.empty.description")} icon="library_add" />
                            ) : (
                                <>
                                    <div className="grid gap-3">
                                        {templates.map((template) => (
                                            <div key={template.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                                <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                                                    <div>
                                                        <p className="font-bold text-slate-950">{template.name}</p>
                                                        <p className="mt-1 text-sm text-slate-500">{t("admin:admin.templates.list.questionCount", { count: template.questions.length })} | {template.recipientScope === "DEPARTMENT" ? t("admin:admin.templates.form.departmentDefault") : t("admin:admin.surveys.form.audience.allStudents")} | {template.active ? t("admin:admin.users.filters.active") : t("admin:admin.surveys.list.filters.archived")}</p>
                                                    </div>
                                                    <div className="flex gap-2">
                                                        <button type="button" onClick={() => edit(template)} className="rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700">{t("admin:admin.questionBank.buttons.edit")}</button>
                                                        <button type="button" onClick={() => void setActive(template, !template.active)} className="rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700">{template.active ? t("admin:admin.questionBank.buttons.archive") : t("admin:admin.questionBank.buttons.restore")}</button>
                                                    </div>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                    <div className="mt-4">
                                        <PaginationControls page={templatePage + 1} pageCount={Math.max(totalTemplatePages, 1)} onPageChange={(nextPage) => setTemplatePage(nextPage - 1)} />
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
