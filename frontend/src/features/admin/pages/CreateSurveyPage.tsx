import { type ReactNode, useCallback, useEffect, useMemo, useState } from "react";
import { Link, useLocation, useNavigate, useParams } from "react-router-dom";
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

function formatDateTime(value: string | null) {
    if (!value) {
        return "Not yet";
    }

    return new Intl.DateTimeFormat("en-GB", {
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

function buildActionCopy(action: LifecycleAction, title: string) {
    switch (action) {
        case "publish":
            return { title: "Publish survey", description: `Publish "${title}" and assign it to the configured recipients.`, confirmLabel: "Publish survey" };
        case "close":
            return { title: "Close survey", description: `Close "${title}" now and stop collecting responses immediately.`, confirmLabel: "Close survey", tone: "danger" as const };
        case "archive":
            return { title: "Archive survey", description: `Archive "${title}" as a historical record. It will remain read-only.`, confirmLabel: "Archive survey" };
        case "show":
            return { title: "Make survey visible", description: `Make "${title}" visible again for eligible recipients.`, confirmLabel: "Show survey" };
        case "hide":
            return { title: "Hide survey", description: `Hide "${title}" from recipients without changing its lifecycle state.`, confirmLabel: "Hide survey" };
    }
}

export default function CreateSurveyPage() {
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
            return "New surveys start as drafts. Configure survey information, schedule, audience, and questions before publishing.";
        }
        if (isDraft) {
            return "Draft surveys remain editable. Publish only when the dates, audience, and question set are complete.";
        }
        if (isPublished) {
            return "Published surveys follow their schedule. Only visibility changes and explicit closure are available.";
        }
        if (isClosed) {
            return "Closed surveys are read-only. Archive them when they should move into record-keeping mode.";
        }
        return "Archived surveys remain read-only historical records.";
    }, [isClosed, isDraft, isEditMode, isPublished]);

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
            setError(getApiErrorMessage(requestError, "Unable to load survey."));
        } finally {
            setLoadingSurvey(false);
        }
    }, [isEditMode, surveyId]);

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
            setFeedback(`Applied template "${template.name}" in replace mode. Template questions were copied into this draft.`);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to apply survey template."));
        } finally {
            setPendingTemplateId(null);
        }
    }

    async function handleApplyTemplate() {
        const templateId = Number(selectedTemplateId);
        if (!Number.isFinite(templateId) || templateId <= 0) {
            setError("Select a template to apply.");
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
            setError("Title is required.");
            return;
        }
        if (questions.length === 0) {
            setError("At least one question is required.");
            return;
        }
        if (questions.some((question) => !question.content.trim())) {
            setError("All questions must have content.");
            return;
        }
        if (recipientScope === "DEPARTMENT" && !recipientDepartmentId.trim()) {
            setError("Select a department for department-scoped recipients.");
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
                    setError(response.message || "Unable to save survey.");
                    return;
                }
                setFeedback(response.message);
                await loadSurveyData();
                return;
            }

            const response = await createSurvey(payload);
            if (!response.success) {
                setError(response.message || "Unable to save survey.");
                return;
            }
            navigate(`/admin/surveys/${response.surveyId}/edit`, {
                state: { feedback: "Survey draft created. Review it and publish when ready." },
            });
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to save survey."));
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
                setError(response.message || "Unable to update survey.");
                return;
            }

            setPendingAction(null);
            setFeedback(response.message);
            await loadSurveyData();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to update survey."));
        } finally {
            setToggling(false);
        }
    }

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-xl px-6 py-10">
                <PageHeader
                    eyebrow="Admin / Surveys"
                    title={isEditMode ? "Manage survey" : "Create survey"}
                    description={isEditMode ? "Review survey setup, monitor recipient activity, and control lifecycle transitions from one operational page." : "Create a survey draft with clear information, schedule, audience, and question structure before publishing."}
                    actions={<Link to="/admin/surveys" className="inline-flex items-center rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">Back to surveys</Link>}
                />

                <div className="mt-6 space-y-6">
                    {error ? <ErrorState description={error} onRetry={() => void (isEditMode ? loadSurveyData() : Promise.resolve())} /> : null}
                    {feedback ? <div className="rounded-[24px] border border-emerald-200 bg-emerald-50 px-5 py-4 text-sm font-medium text-emerald-700">{feedback}</div> : null}

                    {loadingSurvey ? (
                        <LoadingState label="Loading survey..." />
                    ) : (
                        <form onSubmit={handleSubmit} className="space-y-6">
                            {isEditMode ? (
                                <>
                                    <SectionCard title="Lifecycle overview" description={lifecycleHelp} actions={<div className="flex flex-wrap gap-2"><StatusBadge kind="surveyLifecycle" value={lifecycleState} /><StatusBadge kind="surveyRuntime" value={runtimeStatus} /><StatusBadge kind="surveyVisibility" value={hidden ? "HIDDEN" : "VISIBLE"} /></div>}>
                                        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-5">
                                            <StatCard label="Targeted" value={targetedCount} />
                                            <StatCard label="Opened" value={openedCount} tone="blue" />
                                            <StatCard label="Submitted" value={responseCount} tone="emerald" />
                                            <StatCard label="Not opened" value={notOpenedCount} tone="amber" />
                                            <StatCard label="Response rate" value={formatRate(responseRate)} tone="slate" />
                                        </div>
                                    </SectionCard>

                                    <SectionCard title="Lifecycle actions" description="Only show publish, close, archive, and visibility actions when the current lifecycle allows them.">
                                        <div className="grid gap-3 lg:grid-cols-2 xl:grid-cols-4">
                                            <button type="button" onClick={() => setPendingAction("publish")} disabled={toggling || !isDraft} className="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-700 transition hover:border-emerald-300 hover:bg-emerald-100 disabled:cursor-not-allowed disabled:opacity-60">Publish survey</button>
                                            <button type="button" onClick={() => setPendingAction("close")} disabled={toggling || !isPublished} className="rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-semibold text-amber-700 transition hover:border-amber-300 hover:bg-amber-100 disabled:cursor-not-allowed disabled:opacity-60">Close survey</button>
                                            <button type="button" onClick={() => setPendingAction("archive")} disabled={toggling || !isClosed} className="rounded-2xl border border-slate-300 bg-slate-100 px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-200 disabled:cursor-not-allowed disabled:opacity-60">Archive survey</button>
                                            <button type="button" onClick={() => setPendingAction(hidden ? "show" : "hide")} disabled={toggling || (!isPublished && !isClosed)} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60">{hidden ? "Show survey" : "Hide survey"}</button>
                                        </div>
                                    </SectionCard>
                                </>
                            ) : null}

                            <FormSection title="Survey information" description="Keep the survey title and description readable for admins who manage many campaigns in parallel.">
                                {!formLocked ? (
                                    <div className="mb-5 grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 md:grid-cols-[minmax(0,1fr)_auto]">
                                        <select value={selectedTemplateId} onChange={(event) => setSelectedTemplateId(event.target.value)} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-base outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5">
                                            <option value="">Select survey template</option>
                                            {templates.map((template) => (
                                                <option key={template.id} value={template.id}>{template.name}</option>
                                            ))}
                                        </select>
                                        <button type="button" onClick={() => void handleApplyTemplate()} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                                            Replace with template
                                        </button>
                                        <p className="md:col-span-2 text-xs leading-5 text-slate-500">Replace mode copies the selected template into this draft. It overwrites current title, description, audience, and questions, and later template changes will not sync into this survey.</p>
                                    </div>
                                ) : null}
                                <div className="grid gap-5">
                                    <Field label="Title">
                                        <input type="text" value={title} onChange={(event) => setTitle(event.target.value)} disabled={formLocked} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-base outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100" />
                                    </Field>
                                    <Field label="Description" description="This is used in admin search and should help distinguish surveys quickly.">
                                        <textarea value={description} onChange={(event) => setDescription(event.target.value)} rows={4} disabled={formLocked} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-base outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100" />
                                    </Field>
                                </div>
                            </FormSection>

                            <FormSection title="Schedule" description="Runtime status is derived from lifecycle state and this date window.">
                                <div className="grid gap-5 md:grid-cols-2">
                                    <Field label="Start date">
                                        <input type="datetime-local" value={startDate} onChange={(event) => setStartDate(event.target.value)} disabled={formLocked} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-base outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100" />
                                    </Field>
                                    <Field label="End date">
                                        <input type="datetime-local" value={endDate} onChange={(event) => setEndDate(event.target.value)} disabled={formLocked} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-base outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100" />
                                    </Field>
                                </div>
                            </FormSection>

                            <FormSection title="Audience / recipients" description="Recipient configuration should describe who receives the survey without exposing backend-shaped IDs.">
                                <div className="grid gap-5 md:grid-cols-2">
                                    <Field label="Recipient scope">
                                        <select value={recipientScope} onChange={(event) => setRecipientScope(event.target.value as "ALL_STUDENTS" | "DEPARTMENT")} disabled={recipientsLocked} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-base outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100">
                                            <option value="ALL_STUDENTS">All students</option>
                                            <option value="DEPARTMENT">Department only</option>
                                        </select>
                                    </Field>
                                    <Field label="Department" description={recipientScope === "DEPARTMENT" && departments.length === 0 ? "Department lookup is unavailable right now. This selection will remain empty until departments can be loaded." : undefined}>
                                        <select value={recipientDepartmentId} onChange={(event) => setRecipientDepartmentId(event.target.value)} disabled={recipientScope !== "DEPARTMENT" || recipientsLocked || departments.length === 0} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-base outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100">
                                            <option value="">Select department</option>
                                            {departments.map((department) => (
                                                <option key={department.id} value={department.id}>{department.name}</option>
                                            ))}
                                        </select>
                                    </Field>
                                </div>
                                {recipientsLocked ? <p className="text-sm font-medium text-amber-700">Recipient settings are locked after publication or once responses exist.</p> : null}
                            </FormSection>

                            <FormSection title="Questions" description="This phase keeps the current editor model but organizes it into a cleaner operational section.">
                                <div className="flex flex-wrap justify-end gap-3">
                                    <select defaultValue="" onChange={(event) => { addQuestionFromBank(event.target.value); event.currentTarget.value = ""; }} disabled={questionLocked || questionBank.length === 0} className="rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition focus:border-slate-500 disabled:cursor-not-allowed disabled:opacity-60">
                                        <option value="">Add from question bank</option>
                                        {questionBank.map((entry) => (
                                            <option key={entry.id} value={entry.id}>{entry.content}</option>
                                        ))}
                                    </select>
                                    <button type="button" onClick={addQuestion} disabled={questionLocked} className="inline-flex items-center gap-2 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60">
                                        <span className="material-symbols-outlined text-[18px]">add</span>
                                        Add question
                                    </button>
                                </div>

                                {questions.length === 0 ? (
                                    <EmptyState title="No questions yet" description="Add at least one question before saving or publishing this survey." icon="quiz" />
                                ) : (
                                    <div className="grid gap-4">
                                        {questions.map((question, index) => (
                                            <SectionCard key={index} className="p-5 sm:p-5">
                                                <div className="grid gap-4 md:grid-cols-[minmax(0,1fr)_200px_auto]">
                                                    <Field label={`Question ${index + 1}`}>
                                                        <input type="text" value={question.content} onChange={(event) => updateQuestion(index, "content", event.target.value)} disabled={questionLocked} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100" />
                                                    </Field>
                                                    <Field label="Type">
                                                        <select value={question.type} onChange={(event) => updateQuestion(index, "type", event.target.value as CreateQuestionData["type"])} disabled={questionLocked} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5 disabled:cursor-not-allowed disabled:bg-slate-100">
                                                            <option value="RATING">Rating (1-5)</option>
                                                            <option value="TEXT">Free text</option>
                                                        </select>
                                                    </Field>
                                                    <div className="flex items-end">
                                                        <button type="button" onClick={() => removeQuestion(index)} disabled={questionLocked} className="inline-flex w-full items-center justify-center rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700 transition hover:border-red-300 hover:bg-red-100 disabled:cursor-not-allowed disabled:opacity-60">Remove</button>
                                                    </div>
                                                </div>
                                            </SectionCard>
                                        ))}
                                    </div>
                                )}

                                {questionLocked ? <p className="text-sm font-medium text-amber-700">Questions are locked after publication or once responses exist.</p> : null}
                                {!questionLocked ? <p className="text-sm text-slate-500">Question-bank items are copied into the draft. Later edits or archives in the question bank do not change this survey.</p> : null}
                            </FormSection>

                            {isEditMode ? (
                                <SectionCard title="Recipient activity" description="These recipient counts and pending participants help explain real survey performance, not just configuration state.">
                                    {pendingRecipients.length === 0 ? (
                                        <EmptyState title="No pending recipients" description="Everyone assigned to this survey has already submitted, or the survey has not assigned recipients yet." icon="group" />
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
                                                        <p>First opened: <span className="font-medium text-slate-900">{formatDateTime(recipient.openedAt)}</span></p>
                                                        <p>Submitted: <span className="font-medium text-slate-900">{formatDateTime(recipient.submittedAt)}</span></p>
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
                                        {loading ? "Saving..." : isEditMode ? "Save draft changes" : "Create draft"}
                                    </span>
                                </button>
                            </div>
                        </form>
                    )}
                </div>
            </div>

            <ConfirmDialog
                open={pendingAction != null}
                title={buildActionCopy(pendingAction || "publish", title).title}
                description={buildActionCopy(pendingAction || "publish", title).description}
                confirmLabel={buildActionCopy(pendingAction || "publish", title).confirmLabel}
                tone={buildActionCopy(pendingAction || "publish", title).tone}
                busy={toggling}
                onCancel={() => setPendingAction(null)}
                onConfirm={() => void handleLifecycleAction()}
            />
            <ConfirmDialog
                open={pendingTemplateId != null}
                title="Replace draft with template"
                description="This will overwrite the current draft title, description, audience, and question list. The template will be copied into this draft and will not stay linked for future changes."
                confirmLabel="Replace draft"
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
