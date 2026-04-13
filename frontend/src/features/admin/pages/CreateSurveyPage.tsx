import { type ReactNode, useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { closeSurvey, createSurvey, getManagedSurvey, setSurveyVisibility, updateSurvey } from "../../../api/adminApi";
import { getApiErrorMessage } from "../../../api/apiError";
import MainFooter from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
import type { CreateQuestionData, CreateSurveyData } from "../../../types/survey";

export default function CreateSurveyPage() {
    const navigate = useNavigate();
    const { id } = useParams();
    const surveyId = id ? Number(id) : null;
    const isEditMode = Number.isFinite(surveyId);

    const [loading, setLoading] = useState(false);
    const [loadingSurvey, setLoadingSurvey] = useState(isEditMode);
    const [toggling, setToggling] = useState(false);
    const [error, setError] = useState("");
    const [feedback, setFeedback] = useState("");

    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const [questions, setQuestions] = useState<CreateQuestionData[]>([]);
    const [recipientScope, setRecipientScope] = useState<"ALL_STUDENTS" | "DEPARTMENT">("ALL_STUDENTS");
    const [recipientDepartmentId, setRecipientDepartmentId] = useState("");
    const [hidden, setHidden] = useState(false);
    const [status, setStatus] = useState<string | null>(null);
    const [responseCount, setResponseCount] = useState(0);

    useEffect(() => {
        async function loadSurvey() {
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
                setQuestions(survey.questions.map((question) => ({
                    content: question.content,
                    type: question.type,
                })));
                setRecipientScope(survey.recipientScope);
                setRecipientDepartmentId(survey.recipientDepartmentId != null ? String(survey.recipientDepartmentId) : "");
                setHidden(survey.hidden);
                setStatus(survey.status);
                setResponseCount(survey.responseCount);
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, "Unable to load survey."));
            } finally {
                setLoadingSurvey(false);
            }
        }

        loadSurvey();
    }, [isEditMode, surveyId]);

    function addQuestion() {
        setQuestions([...questions, { content: "", type: "RATING" }]);
    }

    function removeQuestion(index: number) {
        setQuestions(questions.filter((_, i) => i !== index));
    }

    function updateQuestion(index: number, field: keyof CreateQuestionData, value: string) {
        const next = [...questions];
        next[index] = { ...next[index], [field]: value };
        setQuestions(next);
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
            setError("Department ID is required for department recipients.");
            return;
        }

        const payload: CreateSurveyData = {
            title: title.trim(),
            description: description.trim() || null,
            startDate: startDate ? new Date(startDate).toISOString() : null,
            endDate: endDate ? new Date(endDate).toISOString() : null,
            questions: questions.map((question) => ({
                content: question.content.trim(),
                type: question.type,
            })),
            recipientScope,
            recipientDepartmentId: recipientScope === "DEPARTMENT" ? Number(recipientDepartmentId) : null,
        };

        try {
            setLoading(true);
            const response = isEditMode && surveyId
                ? await updateSurvey(surveyId, payload)
                : await createSurvey(payload);

            if (!response.success) {
                setError(response.message || "Unable to save survey.");
                return;
            }

            if (isEditMode) {
                setFeedback(response.message);
            } else {
                navigate("/admin/surveys");
            }
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to save survey."));
        } finally {
            setLoading(false);
        }
    }

    async function handleCloseSurvey() {
        if (!surveyId) {
            return;
        }

        try {
            setToggling(true);
            setError("");
            setFeedback("");
            const response = await closeSurvey(surveyId);
            if (!response.success) {
                setError(response.message || "Unable to close survey.");
                return;
            }
            setFeedback(response.message);
            setStatus("CLOSED");
            setEndDate(toDateTimeLocal(new Date().toISOString()));
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to close survey."));
        } finally {
            setToggling(false);
        }
    }

    async function handleToggleVisibility() {
        if (!surveyId) {
            return;
        }

        try {
            setToggling(true);
            setError("");
            setFeedback("");
            const nextHidden = !hidden;
            const response = await setSurveyVisibility(surveyId, nextHidden);
            if (!response.success) {
                setError(response.message || "Unable to update visibility.");
                return;
            }
            setHidden(nextHidden);
            setFeedback(response.message);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to update visibility."));
        } finally {
            setToggling(false);
        }
    }

    return (
        <>
            <MainHeader />
            <main className="min-h-screen bg-[linear-gradient(180deg,#f4f8ff_0%,#eef3f8_44%,#f7fafc_100%)]">
                <div className="mx-auto max-w-screen-md px-6 py-10">
                    <div className="mb-10 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
                        <div>
                            <span className="mb-3 inline-flex rounded-full border border-indigo-200 bg-indigo-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-indigo-700">
                                Admin / Surveys
                            </span>
                            <h1 className="text-4xl font-extrabold tracking-tight text-slate-950">
                                {isEditMode ? "Manage survey" : "Create new survey"}
                            </h1>
                            <p className="mt-4 text-base leading-7 text-slate-500">
                                {isEditMode
                                    ? "Update survey details, manage visibility, and keep recipient rules simple."
                                    : "Design a new feedback survey, attach questions, define availability dates, and choose the initial recipients."}
                            </p>
                        </div>
                        <Link
                            to="/admin/surveys"
                            className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-bold text-slate-700 transition hover:border-slate-300 hover:bg-slate-50"
                        >
                            Back to surveys
                        </Link>
                    </div>

                    {error ? (
                        <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700 shadow-sm">
                            {error}
                        </div>
                    ) : null}

                    {feedback ? (
                        <div className="mb-6 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700 shadow-sm">
                            {feedback}
                        </div>
                    ) : null}

                    {loadingSurvey ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-10 text-sm font-medium text-slate-500 shadow-sm">
                            Loading survey...
                        </div>
                    ) : (
                        <form onSubmit={handleSubmit} className="space-y-8">
                            {isEditMode ? (
                                <section className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                                    <div className="grid gap-4 md:grid-cols-3">
                                        <InfoTile label="Status" value={status || "Unknown"} />
                                        <InfoTile label="Responses" value={String(responseCount)} />
                                        <InfoTile label="Visibility" value={hidden ? "Hidden" : "Visible"} />
                                    </div>

                                    <div className="mt-5 flex flex-col gap-3 md:flex-row">
                                        <button
                                            type="button"
                                            onClick={handleCloseSurvey}
                                            disabled={toggling || status === "CLOSED"}
                                            className="inline-flex flex-1 items-center justify-center gap-2 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-bold text-amber-700 transition hover:border-amber-300 hover:bg-amber-100 disabled:cursor-not-allowed disabled:opacity-60"
                                        >
                                            <span>{toggling ? "Updating..." : "Close survey"}</span>
                                            <span className="material-symbols-outlined text-[18px]">event_busy</span>
                                        </button>
                                        <button
                                            type="button"
                                            onClick={handleToggleVisibility}
                                            disabled={toggling}
                                            className="inline-flex flex-1 items-center justify-center gap-2 rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm font-bold text-slate-700 transition hover:border-slate-300 hover:bg-slate-100 disabled:cursor-not-allowed disabled:opacity-60"
                                        >
                                            <span>{toggling ? "Updating..." : hidden ? "Show survey" : "Hide survey"}</span>
                                            <span className="material-symbols-outlined text-[18px]">{hidden ? "visibility" : "visibility_off"}</span>
                                        </button>
                                    </div>
                                </section>
                            ) : null}

                            <div className="rounded-[28px] border border-slate-200 bg-white p-6 md:p-8 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                                <h2 className="mb-6 border-b border-slate-100 pb-4 text-xl font-bold text-slate-900">
                                    Survey details
                                </h2>
                                <div className="grid gap-6">
                                    <Field label="Title">
                                        <input
                                            type="text"
                                            value={title}
                                            onChange={(event) => setTitle(event.target.value)}
                                            className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-base outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
                                        />
                                    </Field>

                                    <Field label="Description">
                                        <textarea
                                            value={description}
                                            onChange={(event) => setDescription(event.target.value)}
                                            rows={3}
                                            className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-base outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
                                        />
                                    </Field>

                                    <div className="grid gap-6 md:grid-cols-2">
                                        <Field label="Start date">
                                            <input
                                                type="datetime-local"
                                                value={startDate}
                                                onChange={(event) => setStartDate(event.target.value)}
                                                className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-base outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
                                            />
                                        </Field>
                                        <Field label="End date">
                                            <input
                                                type="datetime-local"
                                                value={endDate}
                                                onChange={(event) => setEndDate(event.target.value)}
                                                className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-base outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
                                            />
                                        </Field>
                                    </div>
                                </div>
                            </div>

                            <div className="rounded-[28px] border border-slate-200 bg-white p-6 md:p-8 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                                <h2 className="mb-6 border-b border-slate-100 pb-4 text-xl font-bold text-slate-900">
                                    Recipients
                                </h2>
                                <div className="grid gap-6 md:grid-cols-2">
                                    <Field label="Recipient scope">
                                        <select
                                            value={recipientScope}
                                            onChange={(event) => setRecipientScope(event.target.value as "ALL_STUDENTS" | "DEPARTMENT")}
                                            className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-base outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
                                            disabled={responseCount > 0}
                                        >
                                            <option value="ALL_STUDENTS">All students</option>
                                            <option value="DEPARTMENT">Department only</option>
                                        </select>
                                    </Field>

                                    <Field label="Department ID">
                                        <input
                                            type="number"
                                            value={recipientDepartmentId}
                                            onChange={(event) => setRecipientDepartmentId(event.target.value)}
                                            className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-base outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
                                            disabled={recipientScope !== "DEPARTMENT" || responseCount > 0}
                                            placeholder="Required for department scope"
                                        />
                                    </Field>
                                </div>
                                {responseCount > 0 ? (
                                    <p className="mt-4 text-sm font-medium text-amber-700">
                                        Recipient settings are locked after responses exist.
                                    </p>
                                ) : null}
                            </div>

                            <div className="rounded-[28px] border border-slate-200 bg-white p-6 md:p-8 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                                <div className="mb-6 flex flex-col gap-4 border-b border-slate-100 pb-4 md:flex-row md:items-center md:justify-between">
                                    <div>
                                        <h2 className="text-xl font-bold text-slate-900">Questions</h2>
                                        <p className="mt-1 text-sm text-slate-500">Design the survey questions.</p>
                                    </div>
                                    <button
                                        type="button"
                                        onClick={addQuestion}
                                        disabled={responseCount > 0}
                                        className="inline-flex items-center gap-2 rounded-xl bg-slate-100 px-4 py-2.5 text-sm font-bold text-slate-700 transition hover:bg-slate-200 disabled:cursor-not-allowed disabled:opacity-60"
                                    >
                                        <span className="material-symbols-outlined text-[18px]">add</span>
                                        Add question
                                    </button>
                                </div>

                                {questions.length === 0 ? (
                                    <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 py-12 text-center text-slate-500">
                                        No questions added yet.
                                    </div>
                                ) : (
                                    <div className="grid gap-4">
                                        {questions.map((question, index) => (
                                            <div key={index} className="relative rounded-2xl border border-slate-200 bg-white p-5 pr-14 shadow-sm">
                                                <div className="flex flex-col gap-4 md:flex-row">
                                                    <div className="flex-1">
                                                        <label className="mb-2 block text-xs font-semibold uppercase tracking-wider text-slate-500">
                                                            Question {index + 1}
                                                        </label>
                                                        <input
                                                            type="text"
                                                            value={question.content}
                                                            onChange={(event) => updateQuestion(index, "content", event.target.value)}
                                                            disabled={responseCount > 0}
                                                            className="w-full rounded-xl border border-slate-300 bg-slate-50 px-4 py-2.5 outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-2 focus:ring-indigo-500/10 disabled:cursor-not-allowed disabled:opacity-60"
                                                        />
                                                    </div>
                                                    <div className="w-full md:w-48">
                                                        <label className="mb-2 block text-xs font-semibold uppercase tracking-wider text-slate-500">
                                                            Type
                                                        </label>
                                                        <select
                                                            value={question.type}
                                                            onChange={(event) => updateQuestion(index, "type", event.target.value)}
                                                            disabled={responseCount > 0}
                                                            className="w-full rounded-xl border border-slate-300 bg-slate-50 px-4 py-2.5 outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-2 focus:ring-indigo-500/10 disabled:cursor-not-allowed disabled:opacity-60"
                                                        >
                                                            <option value="RATING">Rating (1-5)</option>
                                                            <option value="TEXT">Free text</option>
                                                        </select>
                                                    </div>
                                                </div>
                                                <button
                                                    type="button"
                                                    onClick={() => removeQuestion(index)}
                                                    disabled={responseCount > 0}
                                                    className="absolute right-4 top-1/2 flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-full bg-red-50 text-red-500 transition hover:bg-red-100 hover:text-red-600 disabled:cursor-not-allowed disabled:opacity-60"
                                                >
                                                    <span className="material-symbols-outlined text-[20px]">delete</span>
                                                </button>
                                            </div>
                                        ))}
                                    </div>
                                )}
                                {responseCount > 0 ? (
                                    <p className="mt-4 text-sm font-medium text-amber-700">
                                        Questions are locked after responses exist.
                                    </p>
                                ) : null}
                            </div>

                            <div className="flex justify-end pt-2">
                                <button
                                    type="submit"
                                    disabled={loading}
                                    className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#4f46e5_0%,#3b82f6_100%)] px-8 py-4 text-base font-bold text-white shadow-[0_16px_36px_rgba(79,70,229,0.22)] transition hover:translate-y-[-1px] hover:shadow-[0_18px_40px_rgba(79,70,229,0.28)] disabled:cursor-not-allowed disabled:opacity-60 disabled:shadow-none md:w-auto"
                                >
                                    <span>{loading ? "Saving..." : isEditMode ? "Save changes" : "Launch survey"}</span>
                                    <span className="material-symbols-outlined text-[20px]">{isEditMode ? "save" : "rocket_launch"}</span>
                                </button>
                            </div>
                        </form>
                    )}
                </div>
            </main>
            <MainFooter />
        </>
    );
}

function toDateTimeLocal(value: string) {
    const date = new Date(value);
    const offset = date.getTimezoneOffset();
    const local = new Date(date.getTime() - offset * 60000);
    return local.toISOString().slice(0, 16);
}

function Field({ label, children }: { label: string; children: ReactNode }) {
    return (
        <label className="block">
            <span className="mb-2 block text-sm font-semibold text-slate-700">{label}</span>
            {children}
        </label>
    );
}

function InfoTile({ label, value }: { label: string; value: string }) {
    return (
        <div className="rounded-2xl border border-slate-200 bg-slate-50 p-4">
            <p className="text-xs font-semibold uppercase tracking-[0.18em] text-slate-500">{label}</p>
            <p className="mt-2 text-xl font-bold text-slate-950">{value}</p>
        </div>
    );
}
