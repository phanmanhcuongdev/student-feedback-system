import { type ReactNode, useCallback, useEffect, useMemo, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
    archiveSurvey,
    applySurveyTemplate,
    closeSurvey,
    createSurvey,
    getManagedSurvey,
    getQuestionBankEntries,
    getSurveyManagementDepartments,
    getSurveyTemplates,
    publishSurvey,
    setSurveyVisibility,
    updateSurvey,
} from "../../../api/adminApi";
import { getApiErrorMessage } from "../../../api/apiError";
import ConfirmDialog from "../../../components/ui/ConfirmDialog";
import EmptyState from "../../../components/ui/EmptyState";
import ErrorState from "../../../components/ui/ErrorState";
import FormSection from "../../../components/ui/FormSection";
import LoadingState from "../../../components/ui/LoadingState";
import PageHeader from "../../../components/ui/PageHeader";
import SectionCard from "../../../components/ui/SectionCard";
import StatCard from "../../../components/ui/StatCard";
import StatusBadge from "../../../components/ui/StatusBadge";
import { darkActionButtonClass, darkActionButtonStyle } from "../../../components/ui/buttonStyles";
import type { DepartmentOption } from "../../../types/admin";
import type { CreateQuestionData, CreateSurveyData, QuestionBankEntry, SurveyLifecycleState, SurveyRuntimeStatus, SurveyTemplate } from "../../../types/survey";

type LifecycleAction = "publish" | "close" | "archive" | "show" | "hide";

function toDateTimeLocal(value: string) {
    const date = new Date(value);
    const offset = date.getTimezoneOffset();
    const local = new Date(date.getTime() - offset * 60000);
    return local.toISOString().slice(0, 16);
}

function formatDateTime(value: string | null, language: string, notYetLabel: string) {
    if (!value) {
        return notYetLabel;
    }

    return new Intl.DateTimeFormat(language === "vi" ? "vi-VN" : "en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    }).format(new Date(value));
}

function formatRate(value: number) {
    return `${value.toFixed(1)}%`;
}

function buildActionCopy(action: LifecycleAction, title: string, t: (key: string, options?: Record<string, unknown>) => string) {
    switch (action) {
        case "publish":
            return { title: t("admin:admin.surveys.form.lifecycleActions.publish"), description: t("admin:admin.surveys.form.confirm.publish", { title }), confirmLabel: t("admin:admin.surveys.form.lifecycleActions.publish") };
        case "close":
            return { title: t("admin:admin.surveys.form.lifecycleActions.close"), description: t("admin:admin.surveys.form.confirm.close", { title }), confirmLabel: t("admin:admin.surveys.form.lifecycleActions.close"), tone: "danger" as const };
        case "archive":
            return { title: t("admin:admin.surveys.form.lifecycleActions.archive"), description: t("admin:admin.surveys.form.confirm.archive", { title }), confirmLabel: t("admin:admin.surveys.form.lifecycleActions.archive") };
        case "show":
            return { title: t("admin:admin.surveys.form.lifecycleActions.makeVisible"), description: t("admin:admin.surveys.form.confirm.show", { title }), confirmLabel: t("admin:admin.surveys.form.lifecycleActions.show") };
        case "hide":
            return { title: t("admin:admin.surveys.form.lifecycleActions.hide"), description: t("admin:admin.surveys.form.confirm.hide", { title }), confirmLabel: t("admin:admin.surveys.form.lifecycleActions.hide") };
    }
}

export default function CreateSurveyPage() {
    const { i18n, t } = useTranslation(["admin", "validation"]);
    const navigate = useNavigate();
    const location = useLocation();
    const { id } = useParams();
    const surveyId = id ? Number(id) : null;
    const isEditMode = Number.isFinite(surveyId);

    const [loading, setLoading] = useState(false);
    const [loadingSurvey, setLoadingSurvey] = useState(isEditMode);
    const [toggling, setToggling] = useState(false);
    const [error, setError] = useState("");
    const [feedback, setFeedback] = useState("");
    const [departments, setDepartments] = useState<DepartmentOption[]>([]);
    const [questionBank, setQuestionBank] = useState<QuestionBankEntry[]>([]);
    const [templates, setTemplates] = useState<SurveyTemplate[]>([]);
    const [selectedTemplateId, setSelectedTemplateId] = useState("");
    const [pendingTemplateId, setPendingTemplateId] = useState<number | null>(null);
    const [pendingAction, setPendingAction] = useState<LifecycleAction | null>(null);

    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const [questions, setQuestions] = useState<CreateQuestionData[]>([]);
    const [recipientScope, setRecipientScope] = useState<"ALL_STUDENTS" | "DEPARTMENT">("ALL_STUDENTS");
    const [recipientDepartmentId, setRecipientDepartmentId] = useState("");
    const [hidden, setHidden] = useState(false);
    const [lifecycleState, setLifecycleState] = useState<SurveyLifecycleState | null>(null);
    const [runtimeStatus, setRuntimeStatus] = useState<SurveyRuntimeStatus | null>(null);
    const [responseCount, setResponseCount] = useState(0);
    const [targetedCount, setTargetedCount] = useState(0);
    const [openedCount, setOpenedCount] = useState(0);
    const [responseRate, setResponseRate] = useState(0);
    const [pendingRecipients, setPendingRecipients] = useState<Array<{
        studentId: number;
        studentName: string;
        studentCode: string;
        departmentName: string | null;
        participationStatus: "ASSIGNED" | "OPENED" | "SUBMITTED";
        openedAt: string | null;
        submittedAt: string | null;
    }>>([]);

    const isDraft = lifecycleState === "DRAFT";
    const isPublished = lifecycleState === "PUBLISHED";
    const isClosed = lifecycleState === "CLOSED";
    const formLocked = isEditMode && !isDraft;
    const recipientsLocked = responseCount > 0 || formLocked;
    const questionLocked = responseCount > 0 || formLocked;
    const notOpenedCount = Math.max(targetedCount - openedCount, 0);
    const hasDraftContent = Boolean(
        title.trim()
        || description.trim()
        || startDate
        || endDate
        || questions.length > 0
        || recipientScope !== "ALL_STUDENTS"
        || recipientDepartmentId
    );

    const lifecycleHelp = useMemo(() => {
        if (!isEditMode) {
            return t("admin:admin.surveys.form.lifecycleHelp.new");
        }
        if (isDraft) {
            return t("admin:admin.surveys.form.lifecycleHelp.draft");
        }
        if (isPublished) {
            return t("admin:admin.surveys.form.lifecycleHelp.published");
        }
        if (isClosed) {
            return t("admin:admin.surveys.form.lifecycleHelp.closed");
        }
        return t("admin:admin.surveys.form.lifecycleHelp.archived");
    }, [isClosed, isDraft, isEditMode, isPublished, t]);

    const loadSurveyData = useCallback(async () => {
        if (!isEditMode || !surveyId) {
            return;
        }

        try {
            setLoadingSurvey(true);
            setError("");
            const survey = await getManagedSurvey(surveyId);
            setTitle(survey.title);
            setDescription(survey.description || "");
            setStartDate(survey.startDate ? toDateTimeLocal(survey.startDate) : "");
            setEndDate(survey.endDate ? toDateTimeLocal(survey.endDate) : "");
            setQuestions(survey.questions.map((question) => ({ content: question.content, type: question.type })));
            setRecipientScope(survey.recipientScope);
            setRecipientDepartmentId(survey.recipientDepartmentId != null ? String(survey.recipientDepartmentId) : "");
            setHidden(survey.hidden);
            setLifecycleState(survey.lifecycleState);
            setRuntimeStatus(survey.runtimeStatus);
            setResponseCount(survey.responseCount);
            setTargetedCount(survey.targetedCount);
            setOpenedCount(survey.openedCount);
            setResponseRate(survey.responseRate);
            setPendingRecipients(survey.pendingRecipients);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.surveys.form.errors.load")));
        } finally {
            setLoadingSurvey(false);
        }
    }, [isEditMode, surveyId, t]);

    useEffect(() => {
        const navigationState = location.state as { feedback?: string } | null;
        if (navigationState?.feedback) {
            setFeedback(navigationState.feedback);
            navigate(location.pathname, { replace: true, state: null });
        }
    }, [location.pathname, location.state, navigate]);

    useEffect(() => {
        void loadSurveyData();
    }, [loadSurveyData]);

    useEffect(() => {
        async function loadAssets() {
            try {
                const [departmentItems, bankPage, templatePage] = await Promise.all([
                    getSurveyManagementDepartments(),
                    getQuestionBankEntries({ active: true, size: 100 }),
                    getSurveyTemplates({ active: true, size: 100 }),
                ]);
                setDepartments(departmentItems);
                setQuestionBank(bankPage.items);
                setTemplates(templatePage.items);
            } catch {
                setDepartments([]);
                setQuestionBank([]);
                setTemplates([]);
            }
        }

        void loadAssets();
    }, []);

    function addQuestion() {
        setQuestions((current) => [...current, { content: "", type: "RATING", questionBankEntryId: null }]);
    }

    function addQuestionFromBank(entryId: string) {
        const entry = questionBank.find((item) => item.id === Number(entryId));
        if (!entry) {
            return;
        }
        setQuestions((current) => [...current, { content: entry.content, type: entry.type, questionBankEntryId: entry.id }]);
    }

    function removeQuestion(index: number) {
        setQuestions((current) => current.filter((_, itemIndex) => itemIndex !== index));
    }

    function updateQuestion(index: number, field: keyof CreateQuestionData, value: string) {
        setQuestions((current) => current.map((question, itemIndex) => itemIndex === index ? { ...question, [field]: value } : question));
    }

    async function applyTemplate(templateId: number) {
        try {
            setError("");
            setFeedback("");
            const template = await applySurveyTemplate(templateId);
            if (template.suggestedTitle) {
                setTitle(template.suggestedTitle);
            } else {
                setTitle("");
            }
            if (template.suggestedSurveyDescription) {
                setDescription(template.suggestedSurveyDescription);
            } else {
                setDescription("");
            }
            setRecipientScope(template.recipientScope);
            setRecipientDepartmentId(template.recipientDepartmentId != null ? String(template.recipientDepartmentId) : "");
            setQuestions(template.questions.map((question) => ({
                content: question.content,
                type: question.type,
                questionBankEntryId: question.questionBankEntryId,
            })));
            setFeedback(t("admin:admin.surveys.form.feedback.templateApplied", { name: template.name }));
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.surveys.form.errors.applyTemplate")));
        } finally {
            setPendingTemplateId(null);
        }
    }

    async function handleApplyTemplate() {
        const templateId = Number(selectedTemplateId);
        if (!Number.isFinite(templateId) || templateId <= 0) {
            setError(t("validation:validation.admin.surveys.selectTemplate"));
            return;
        }

        if (hasDraftContent) {
            setPendingTemplateId(templateId);
            return;
        }

        await applyTemplate(templateId);
    }

    async function handleSubmit(event: React.FormEvent) {
        event.preventDefault();
        setError("");
        setFeedback("");

        if (!title.trim()) {
            setError(t("validation:validation.admin.surveys.titleRequired"));
            return;
        }
        if (questions.length === 0) {
            setError(t("validation:validation.admin.surveys.questionRequired"));
            return;
        }
        if (questions.some((question) => !question.content.trim())) {
            setError(t("validation:validation.admin.surveys.questionContentRequired"));
            return;
        }
        if (recipientScope === "DEPARTMENT" && !recipientDepartmentId.trim()) {
            setError(t("validation:validation.admin.surveys.departmentRequired"));
            return;
        }

        const payload: CreateSurveyData = {
            title: title.trim(),
            description: description.trim() || null,
            startDate: startDate ? new Date(startDate).toISOString() : null,
            endDate: endDate ? new Date(endDate).toISOString() : null,
            questions: questions.map((question) => ({ content: question.content.trim(), type: question.type, questionBankEntryId: question.questionBankEntryId || null })),
            recipientScope,
            recipientDepartmentId: recipientScope === "DEPARTMENT" ? Number(recipientDepartmentId) : null,
        };

        try {
            setLoading(true);
            if (isEditMode && surveyId) {
                const response = await updateSurvey(surveyId, payload);
                if (!response.success) {
                    setError(response.message || t("admin:admin.surveys.form.errors.save"));
                    return;
                }
                setFeedback(response.message);
                await loadSurveyData();
                return;
            }

            const response = await createSurvey(payload);
            if (!response.success) {
                setError(response.message || t("admin:admin.surveys.form.errors.save"));
                return;
            }
            navigate(`/admin/surveys/${response.surveyId}/edit`, {
                state: { feedback: t("admin:admin.surveys.form.feedback.draftCreated") },
            });
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.surveys.form.errors.save")));
        } finally {
            setLoading(false);
        }
    }

    async function handleLifecycleAction() {
        if (!surveyId || !pendingAction) {
            return;
        }

        try {
            setToggling(true);
            setError("");
            setFeedback("");
            let response;
            switch (pendingAction) {
                case "publish":
                    response = await publishSurvey(surveyId);
                    break;
                case "close":
                    response = await closeSurvey(surveyId);
                    break;
                case "archive":
                    response = await archiveSurvey(surveyId);
                    break;
                case "show":
                    response = await setSurveyVisibility(surveyId, false);
                    break;
                case "hide":
                    response = await setSurveyVisibility(surveyId, true);
                    break;
            }

            if (!response.success) {
                setError(response.message || t("admin:admin.surveys.form.errors.update"));
                return;
            }

            setPendingAction(null);
            setFeedback(response.message);
            await loadSurveyData();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.surveys.form.errors.update")));
        } finally {
            setToggling(false);
        }
    }

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-xl px-6 py-10">
                <PageHeader
                    eyebrow={t("admin:admin.surveys.form.header.eyebrow")}
                    title={isEditMode ? t("admin:admin.surveys.form.header.manageTitle") : t("admin:admin.surveys.form.header.createTitle")}
                    description={isEditMode ? t("admin:admin.surveys.form.header.manageDescription") : t("admin:admin.surveys.form.header.createDescription")}
                    actions={<Link to="/admin/surveys" className="inline-flex items-center rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">{t("admin:admin.surveys.form.buttons.backToSurveys")}</Link>}
                />

                <div className="mt-6 space-y-6">
                    {error ? <ErrorState description={error} onRetry={() => void (isEditMode ? loadSurveyData() : Promise.resolve())} /> : null}
                    {feedback ? <div className="rounded-[24px] border border-emerald-200 bg-emerald-50 px-5 py-4 text-sm font-medium text-emerald-700">{feedback}</div> : null}

                    {loadingSurvey ? (
                        <LoadingState label={t("admin:admin.surveys.form.loading")} />
                    ) : (
                        <form onSubmit={handleSubmit} className="space-y-6">
                            {isEditMode ? (
                                <>
                                    <SectionCard title={t("admin:admin.surveys.form.lifecycle.title")} description={lifecycleHelp} actions={<div className="flex flex-wrap gap-2"><StatusBadge kind="surveyLifecycle" value={lifecycleState} /><StatusBadge kind="surveyRuntime" value={runtimeStatus} /><StatusBadge kind="surveyVisibility" value={hidden ? "HIDDEN" : "VISIBLE"} /></div>}>
                                        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
                                            <StatCard label={t("admin:admin.surveys.form.stats.targeted")} value={targetedCount} />
                                            <StatCard label={t("admin:admin.surveys.form.stats.opened")} value={openedCount} tone="blue" />
                                            <StatCard label={t("admin:admin.surveys.form.stats.submitted")} value={responseCount} tone="emerald" />
                                            <StatCard label={t("admin:admin.surveys.form.stats.notOpened")} value={notOpenedCount} tone="amber" />
                                            <StatCard label={t("admin:admin.surveys.form.stats.responseRate")} value={formatRate(responseRate)} tone="slate" />
                                        </div>
                                    </SectionCard>

                                    <SectionCard title={t("admin:admin.surveys.form.lifecycleActions.title")} description={t("admin:admin.surveys.form.lifecycleActions.description")}>
                                        <div className="grid gap-3 lg:grid-cols-2 xl:grid-cols-4">
                                            <button type="button" onClick={() => setPendingAction("publish")} disabled={toggling || !isDraft} className="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-700 transition hover:border-emerald-300 hover:bg-emerald-100 disabled:cursor-not-allowed disabled:opacity-60">{t("admin:admin.surveys.form.lifecycleActions.publish")}</button>
                                            <button type="button" onClick={() => setPendingAction("close")} disabled={toggling || !isPublished} className="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-semibold text-amber-700 transition hover:border-amber-300 hover:bg-amber-100 disabled:cursor-not-allowed disabled:opacity-60">{t("admin:admin.surveys.form.lifecycleActions.close")}</button>
                                            <button type="button" onClick={() => setPendingAction("archive")} disabled={toggling || !isClosed} className="rounded-2xl border border-slate-300 bg-slate-100 px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-200 disabled:cursor-not-allowed disabled:opacity-60">{t("admin:admin.surveys.form.lifecycleActions.archive")}</button>
                                            <button type="button" onClick={() => setPendingAction(hidden ? "show" : "hide")} disabled={toggling || (!isPublished && !isClosed)} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60">{hidden ? t("admin:admin.surveys.form.lifecycleActions.show") : t("admin:admin.surveys.form.lifecycleActions.hide")}</button>
                                        </div>
                                    </SectionCard>
                                </>
                            ) : null}

                            <FormSection title={t("admin:admin.surveys.form.information.title")} description={t("admin:admin.surveys.form.information.description")}>
                                {!formLocked ? (
                                    <div className="mb-5 grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 md:grid-cols-[minmax(0,1fr)_auto]">
                                        <select value={selectedTemplateId} onChange={(event) => setSelectedTemplateId(event.target.value)} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-base outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5">
                                            <option value="">{t("admin:admin.surveys.form.templates.select")}</option>
                                            {templates.map((template) => (
                                                <option key={template.id} value={template.id}>{template.name}</option>
                                            ))}
                                        </select>
                                        <button type="button" onClick={() => void handleApplyTemplate()} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                                            {t("admin:admin.surveys.form.templates.replace")}
                                        </button>
                                        <p className="md:col-span-2 text-xs leading-5 text-slate-500">{t("admin:admin.surveys.form.templates.help")}</p>
                                    </div>
                                ) : null}
                                <div className="grid gap-5">
                                    <Field label={t("admin:admin.surveys.form.fields.title")}>
                                        <input type="text" value={title} onChange={(event) => setTitle(event.target.value)} disabled={formLocked} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-base outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100" />
                                    </Field>
                                    <Field label={t("admin:admin.surveys.form.fields.description")} description={t("admin:admin.surveys.form.fields.descriptionHelp")}>
                                        <textarea value={description} onChange={(event) => setDescription(event.target.value)} rows={4} disabled={formLocked} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-base outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100" />
                                    </Field>
                                </div>
                            </FormSection>

                            <FormSection title={t("admin:admin.surveys.form.schedule.title")} description={t("admin:admin.surveys.form.schedule.description")}>
                                <div className="grid gap-5 md:grid-cols-2">
                                    <Field label={t("admin:admin.surveys.form.fields.startDate")}>
                                        <input type="datetime-local" value={startDate} onChange={(event) => setStartDate(event.target.value)} disabled={formLocked} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-base outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100" />
                                    </Field>
                                    <Field label={t("admin:admin.surveys.form.fields.endDate")}>
                                        <input type="datetime-local" value={endDate} onChange={(event) => setEndDate(event.target.value)} disabled={formLocked} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-base outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100" />
                                    </Field>
                                </div>
                            </FormSection>

                            <FormSection title={t("admin:admin.surveys.form.audience.title")} description={t("admin:admin.surveys.form.audience.description")}>
                                <div className="grid gap-5 md:grid-cols-2">
                                    <Field label={t("admin:admin.surveys.form.fields.recipientScope")}>
                                        <select value={recipientScope} onChange={(event) => setRecipientScope(event.target.value as "ALL_STUDENTS" | "DEPARTMENT")} disabled={recipientsLocked} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-base outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100">
                                            <option value="ALL_STUDENTS">{t("admin:admin.surveys.form.audience.allStudents")}</option>
                                            <option value="DEPARTMENT">{t("admin:admin.surveys.form.audience.departmentOnly")}</option>
                                        </select>
                                    </Field>
                                    <Field label={t("admin:admin.surveys.form.fields.department")} description={recipientScope === "DEPARTMENT" && departments.length === 0 ? t("admin:admin.surveys.form.audience.departmentUnavailable") : undefined}>
                                        <select value={recipientDepartmentId} onChange={(event) => setRecipientDepartmentId(event.target.value)} disabled={recipientScope !== "DEPARTMENT" || recipientsLocked || departments.length === 0} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-base outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100">
                                            <option value="">{t("admin:admin.surveys.form.audience.selectDepartment")}</option>
                                            {departments.map((department) => (
                                                <option key={department.id} value={department.id}>{department.name}</option>
                                            ))}
                                        </select>
                                    </Field>
                                </div>
                                {recipientsLocked ? <p className="text-sm font-medium text-amber-700">{t("admin:admin.surveys.form.audience.locked")}</p> : null}
                            </FormSection>

                            <FormSection title={t("admin:admin.surveys.form.questions.title")} description={t("admin:admin.surveys.form.questions.description")}>
                                <div className="flex flex-wrap justify-end gap-3">
                                    <select defaultValue="" onChange={(event) => { addQuestionFromBank(event.target.value); event.currentTarget.value = ""; }} disabled={questionLocked || questionBank.length === 0} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition focus:border-slate-500 disabled:cursor-not-allowed disabled:opacity-60">
                                        <option value="">{t("admin:admin.surveys.form.questions.addFromBank")}</option>
                                        {questionBank.map((entry) => (
                                            <option key={entry.id} value={entry.id}>{entry.content}</option>
                                        ))}
                                    </select>
                                    <button type="button" onClick={addQuestion} disabled={questionLocked} className="inline-flex items-center gap-2 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60">
                                        <span className="material-symbols-outlined text-[18px]">add</span>
                                        {t("admin:admin.surveys.form.questions.addQuestion")}
                                    </button>
                                </div>

                                {questions.length === 0 ? (
                                    <EmptyState title={t("admin:admin.surveys.form.questions.emptyTitle")} description={t("admin:admin.surveys.form.questions.emptyDescription")} icon="quiz" />
                                ) : (
                                    <div className="grid gap-4">
                                        {questions.map((question, index) => (
                                            <SectionCard key={index} className="p-5 sm:p-5">
                                                <div className="grid gap-4 md:grid-cols-[minmax(0,1fr)_200px_auto]">
                                                    <Field label={t("admin:admin.surveys.form.questions.questionNumber", { number: index + 1 })}>
                                                        <input type="text" value={question.content} onChange={(event) => updateQuestion(index, "content", event.target.value)} disabled={questionLocked} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100" />
                                                    </Field>
                                                    <Field label={t("admin:admin.surveys.form.fields.type")}>
                                                        <select value={question.type} onChange={(event) => updateQuestion(index, "type", event.target.value as CreateQuestionData["type"])} disabled={questionLocked} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100">
                                                            <option value="RATING">{t("admin:admin.surveys.form.questions.ratingType")}</option>
                                                            <option value="TEXT">{t("admin:admin.surveys.form.questions.textType")}</option>
                                                        </select>
                                                    </Field>
                                                    <div className="flex items-end">
                                                        <button type="button" onClick={() => removeQuestion(index)} disabled={questionLocked} className="inline-flex w-full items-center justify-center rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700 transition hover:border-red-300 hover:bg-red-100 disabled:cursor-not-allowed disabled:opacity-60">{t("admin:admin.surveys.form.questions.remove")}</button>
                                                    </div>
                                                </div>
                                            </SectionCard>
                                        ))}
                                    </div>
                                )}

                                {questionLocked ? <p className="text-sm font-medium text-amber-700">{t("admin:admin.surveys.form.questions.locked")}</p> : null}
                                {!questionLocked ? <p className="text-sm text-slate-500">{t("admin:admin.surveys.form.questions.bankHelp")}</p> : null}
                            </FormSection>

                            {isEditMode ? (
                                <SectionCard title={t("admin:admin.surveys.form.recipientActivity.title")} description={t("admin:admin.surveys.form.recipientActivity.description")}>
                                    {pendingRecipients.length === 0 ? (
                                        <EmptyState title={t("admin:admin.surveys.form.recipientActivity.emptyTitle")} description={t("admin:admin.surveys.form.recipientActivity.emptyDescription")} icon="group" />
                                    ) : (
                                        <div className="grid gap-3">
                                            {pendingRecipients.map((recipient) => (
                                                <div key={recipient.studentId} className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
                                                    <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                                                        <div>
                                                            <p className="text-base font-bold text-slate-950">{recipient.studentName}</p>
                                                            <p className="mt-1 text-sm text-slate-500">{recipient.studentCode}{recipient.departmentName ? ` | ${recipient.departmentName}` : ""}</p>
                                                        </div>
                                                        <StatusBadge kind="surveyParticipation" value={recipient.participationStatus} />
                                                    </div>
                                                    <div className="mt-3 grid gap-2 text-sm text-slate-600 md:grid-cols-2">
                                                        <p>{t("admin:admin.surveys.form.recipientActivity.firstOpened")} <span className="font-medium text-slate-900">{formatDateTime(recipient.openedAt, i18n.language, t("admin:admin.surveys.form.common.notYet"))}</span></p>
                                                        <p>{t("admin:admin.surveys.form.recipientActivity.submitted")} <span className="font-medium text-slate-900">{formatDateTime(recipient.submittedAt, i18n.language, t("admin:admin.surveys.form.common.notYet"))}</span></p>
                                                    </div>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </SectionCard>
                            ) : null}

                            <div className="flex justify-end">
                                <button type="submit" disabled={loading || formLocked} className={`${darkActionButtonClass} px-6 py-4 text-sm font-semibold`} style={darkActionButtonStyle}>
                                    <span className="text-white" style={darkActionButtonStyle}>
                                        {loading ? t("admin:admin.surveys.form.buttons.saving") : isEditMode ? t("admin:admin.surveys.form.buttons.saveDraftChanges") : t("admin:admin.surveys.form.buttons.createDraft")}
                                    </span>
                                </button>
                            </div>
                        </form>
                    )}
                </div>
            </div>

            <ConfirmDialog
                open={pendingAction != null}
                title={buildActionCopy(pendingAction || "publish", title, t).title}
                description={buildActionCopy(pendingAction || "publish", title, t).description}
                confirmLabel={buildActionCopy(pendingAction || "publish", title, t).confirmLabel}
                tone={buildActionCopy(pendingAction || "publish", title, t).tone}
                busy={toggling}
                onCancel={() => setPendingAction(null)}
                onConfirm={() => void handleLifecycleAction()}
            />
            <ConfirmDialog
                open={pendingTemplateId != null}
                title={t("admin:admin.surveys.form.templates.confirmTitle")}
                description={t("admin:admin.surveys.form.templates.confirmDescription")}
                confirmLabel={t("admin:admin.surveys.form.templates.confirmLabel")}
                tone="danger"
                busy={false}
                onCancel={() => setPendingTemplateId(null)}
                onConfirm={() => pendingTemplateId != null ? void applyTemplate(pendingTemplateId) : undefined}
            />
        </main>
    );
}

function Field({ label, description, children }: { label: string; description?: string; children: ReactNode }) {
    return (
        <label className="block">
            <span className="mb-2 block text-sm font-semibold text-slate-700">{label}</span>
            {children}
            {description ? <span className="mt-2 block text-xs leading-5 text-slate-500">{description}</span> : null}
        </label>
    );
}
