import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getApiErrorMessage } from "../../../api/apiError";
import { generateSurveyAiSummary, getSurveyAiSummary } from "../../../api/surveyAiSummaryApi";
import { exportSurveyResult, getSurveyResult } from "../../../api/surveyResultApi";
import EmptyState from "../../../components/ui/EmptyState";
import ErrorState from "../../../components/ui/ErrorState";
import LoadingState from "../../../components/ui/LoadingState";
import PageHeader from "../../../components/ui/PageHeader";
import SearchInput from "../../../components/data-view/SearchInput";
import SectionCard from "../../../components/ui/SectionCard";
import StatCard from "../../../components/ui/StatCard";
import StatusBadge from "../../../components/ui/StatusBadge";
import { useAuth } from "../../auth/useAuth";
import type { SurveyAiSummary } from "../../../types/surveyAiSummary";
import type { QuestionStatistics, SurveyResultDetail } from "../../../types/surveyResult";

function formatDate(date: string) {
    return new Intl.DateTimeFormat("en-GB", { day: "2-digit", month: "short", year: "numeric" }).format(new Date(date));
}

function formatDateRange(startDate: string, endDate: string) {
    return `${formatDate(startDate)} - ${formatDate(endDate)}`;
}

function formatRate(value: number) {
    return `${value.toFixed(1)}%`;
}

function formatDateTime(value: string | null) {
    if (!value) {
        return "-";
    }
    return new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    }).format(new Date(value));
}

function getSummaryStatusLabel(status: string) {
    switch (status) {
        case "QUEUED":
            return "Queued";
        case "PROCESSING":
            return "Processing";
        case "COMPLETED":
            return "Completed";
        case "FAILED":
            return "Failed";
        default:
            return "Not generated";
    }
}

function isProcessingStatus(status: string | null | undefined) {
    return status === "QUEUED" || status === "PROCESSING";
}

function getAudienceLabel(survey: SurveyResultDetail) {
    if (survey.recipientScope === "DEPARTMENT") {
        return survey.recipientDepartmentName || "Department";
    }
    return "All students";
}

function RatingQuestionBlock({ question }: { question: QuestionStatistics }) {
    return (
        <div className="grid gap-5 lg:grid-cols-[220px_minmax(0,1fr)]">
            <div className="rounded-2xl border border-sky-200 bg-sky-50 px-5 py-4">
                <p className="text-xs font-bold uppercase tracking-[0.2em] text-sky-700">Average rating</p>
                <p className="mt-3 text-4xl font-extrabold tracking-tight text-slate-950">{question.averageRating === null ? "-" : question.averageRating.toFixed(2)}</p>
                <p className="mt-2 text-sm text-slate-500">Based on {question.responseCount} response{question.responseCount === 1 ? "" : "s"}</p>
            </div>

            <div className="space-y-3">
                {question.ratingBreakdown.map((item) => {
                    const denominator = question.responseCount > 0 ? question.responseCount : 1;
                    const percentage = Math.round((item.count / denominator) * 100);
                    return (
                        <div key={item.rating} className="space-y-2">
                            <div className="flex items-center justify-between gap-4 text-sm">
                                <span className="font-semibold text-slate-700">{item.rating} / 5</span>
                                <span className="text-slate-500">{item.count} response{item.count === 1 ? "" : "s"} | {percentage}%</span>
                            </div>
                            <div className="h-3 overflow-hidden rounded-full bg-slate-100">
                                <div className="h-full rounded-full bg-slate-900" style={{ width: `${percentage}%` }} />
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}

export default function SurveyResultDetailPage() {
    const { id } = useParams();
    const { session } = useAuth();
    const surveyId = Number(id);
    const [survey, setSurvey] = useState<SurveyResultDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [commentQuery, setCommentQuery] = useState("");
    const [expandedQuestions, setExpandedQuestions] = useState<Record<number, boolean>>({});
    const [expandedComments, setExpandedComments] = useState<Record<string, boolean>>({});
    const [exporting, setExporting] = useState(false);
    const [aiSummary, setAiSummary] = useState<SurveyAiSummary | null>(null);
    const [aiLoading, setAiLoading] = useState(true);
    const [aiError, setAiError] = useState("");
    const [isGeneratingAiSummary, setIsGeneratingAiSummary] = useState(false);

    async function fetchAiSummaryStatus() {
        try {
            setAiSummary(await getSurveyAiSummary(surveyId));
            setAiError("");
        } catch (requestError) {
            setAiError(getApiErrorMessage(requestError, "Unable to load AI summary status."));
        }
    }

    useEffect(() => {
        async function fetchSurveyResult() {
            try {
                setLoading(true);
                setError("");
                setSurvey(await getSurveyResult(surveyId));
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, "Unable to load survey statistics."));
            } finally {
                setLoading(false);
            }
        }

        if (Number.isFinite(surveyId)) {
            void fetchSurveyResult();
            return;
        }

        setLoading(false);
        setError("Invalid survey id.");
    }, [surveyId]);

    useEffect(() => {
        async function fetchAiSummary() {
            try {
                setAiLoading(true);
                await fetchAiSummaryStatus();
            } finally {
                setAiLoading(false);
            }
        }

        if (Number.isFinite(surveyId)) {
            void fetchAiSummary();
            return;
        }

        setAiLoading(false);
        setAiError("Invalid survey id.");
    }, [surveyId]);

    useEffect(() => {
        if (!Number.isFinite(surveyId) || !isProcessingStatus(aiSummary?.status)) {
            return;
        }

        const intervalId = window.setInterval(() => {
            void fetchAiSummaryStatus();
        }, 4000);

        return () => window.clearInterval(intervalId);
    }, [aiSummary?.status, surveyId]);

    const textQuestions = useMemo(() => survey?.questions.filter((question) => question.type === "TEXT") ?? [], [survey]);
    const ratingQuestions = useMemo(() => survey?.questions.filter((question) => question.type === "RATING") ?? [], [survey]);

    const filteredTextQuestions = useMemo(() => {
        const normalizedQuery = commentQuery.trim().toLowerCase();
        if (!normalizedQuery) {
            return textQuestions;
        }

        return textQuestions
            .map((question) => ({
                ...question,
                comments: question.comments.filter((comment) => comment.toLowerCase().includes(normalizedQuery)),
            }))
            .filter((question) => question.comments.length > 0 || question.content.toLowerCase().includes(normalizedQuery));
    }, [commentQuery, textQuestions]);

    async function handleExport() {
        try {
            setExporting(true);
            setError("");
            const blob = await exportSurveyResult(surveyId);
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement("a");
            link.href = url;
            link.download = `survey-${surveyId}-report.csv`;
            document.body.appendChild(link);
            link.click();
            link.remove();
            window.URL.revokeObjectURL(url);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to export survey report."));
        } finally {
            setExporting(false);
        }
    }

    async function handleGenerateAiSummary() {
        try {
            setIsGeneratingAiSummary(true);
            setAiError("");
            setAiSummary(await generateSurveyAiSummary(surveyId));
        } catch (requestError) {
            setAiError(getApiErrorMessage(requestError, "Unable to generate AI summary."));
        } finally {
            setIsGeneratingAiSummary(false);
        }
    }

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-2xl px-6 py-10">
                <PageHeader
                    eyebrow="Result Review"
                    title={survey?.title || "Survey result detail"}
                    description={survey ? "Review overview metrics, participation, rating distributions, and anonymous text comments in grouped analytics sections." : "Review survey analytics."}
                    actions={<div className="flex flex-wrap gap-2">
                        {session?.role === "ADMIN" ? (
                            <button type="button" onClick={() => void handleExport()} disabled={exporting || !survey} className="inline-flex items-center rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-semibold text-emerald-700 transition hover:border-emerald-300 hover:bg-emerald-100 disabled:cursor-not-allowed disabled:opacity-60">{exporting ? "Exporting..." : "Export CSV"}</button>
                        ) : null}
                        <Link to="/survey-results" className="inline-flex items-center rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">Back to results</Link>
                    </div>}
                />

                <div className="mt-6 space-y-6">
                    {loading ? (
                        <LoadingState label="Loading survey statistics..." />
                    ) : error ? (
                        <ErrorState description={error} />
                    ) : survey ? (
                        <>
                            <SectionCard title="Overview metrics" description={survey.description || "No survey description provided."} actions={<div className="flex flex-wrap gap-2"><StatusBadge kind="surveyLifecycle" value={survey.lifecycleState} /><StatusBadge kind="surveyRuntime" value={survey.runtimeStatus} /></div>}>
                                <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-6">
                                    <StatCard label="Targeted" value={survey.targetedCount} />
                                    <StatCard label="Opened" value={survey.openedCount} tone="blue" />
                                    <StatCard label="Submitted" value={survey.submittedCount} tone="emerald" />
                                    <StatCard label="Response rate" value={formatRate(survey.responseRate)} tone="slate" />
                                    <StatCard label="Rating questions" value={ratingQuestions.length} tone="sky" />
                                    <StatCard label="Text questions" value={textQuestions.length} tone="amber" />
                                </div>
                            </SectionCard>

                            <SectionCard title="Participation summary" description="Identity is intentionally excluded here. This section stays at the aggregate level for staff review.">
                                <div className="grid gap-4 lg:grid-cols-2">
                                    <div className="rounded-2xl border border-slate-200 bg-slate-50 p-5 text-sm text-slate-600">
                                        <div className="grid gap-3">
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Audience</span><span className="font-medium text-slate-900">{getAudienceLabel(survey)}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Window</span><span className="font-medium text-slate-900">{formatDateRange(survey.startDate, survey.endDate)}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Not opened</span><span className="font-medium text-slate-900">{Math.max(survey.targetedCount - survey.openedCount, 0)}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Opened but not submitted</span><span className="font-medium text-slate-900">{Math.max(survey.openedCount - survey.submittedCount, 0)}</span></div>
                                        </div>
                                    </div>
                                    <div className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
                                        <p className="text-xs font-bold uppercase tracking-[0.18em] text-slate-500">Participation progress</p>
                                        <div className="mt-4 space-y-4">
                                            <ProgressBar label="Opened" value={survey.openedCount} total={survey.targetedCount} colorClassName="bg-sky-500" />
                                            <ProgressBar label="Submitted" value={survey.submittedCount} total={survey.targetedCount} colorClassName="bg-emerald-500" />
                                        </div>
                                    </div>
                                </div>
                            </SectionCard>

                            <SectionCard title="Question analytics" description="Rating questions show distribution and averages. Text questions are separated below for easier qualitative review.">
                                <div className="space-y-5">
                                    {ratingQuestions.length === 0 ? (
                                        <EmptyState title="No rating analytics" description="This survey does not contain rating questions with distribution data." icon="query_stats" />
                                    ) : (
                                        ratingQuestions.map((question, index) => (
                                            <article key={question.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
                                                <div className="mb-5 flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                                                    <div>
                                                        <span className="inline-flex rounded-full border border-slate-200 bg-white px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-slate-500">Question {index + 1}</span>
                                                        <h3 className="mt-3 text-xl font-bold text-slate-950">{question.content}</h3>
                                                    </div>
                                                    <div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600">{question.responseCount} response{question.responseCount === 1 ? "" : "s"}</div>
                                                </div>
                                                <RatingQuestionBlock question={question} />
                                            </article>
                                        ))
                                    )}
                                </div>
                            </SectionCard>

                            <SectionCard
                                title="AI summary"
                                description="Generate an admin-facing summary from student text feedback only when you need it. The analysis runs in the background and reuses the latest result when the feedback has not changed."
                                actions={(
                                    <button
                                        type="button"
                                        onClick={() => void handleGenerateAiSummary()}
                                        disabled={!survey || aiLoading || isGeneratingAiSummary || isProcessingStatus(aiSummary?.status)}
                                        className="inline-flex items-center rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-semibold text-amber-800 transition hover:border-amber-300 hover:bg-amber-100 disabled:cursor-not-allowed disabled:opacity-60"
                                    >
                                        {isGeneratingAiSummary ? "Submitting..." : isProcessingStatus(aiSummary?.status) ? "Processing..." : aiSummary?.summary ? "Regenerate AI summary" : "Generate AI summary"}
                                    </button>
                                )}
                            >
                                {aiLoading ? (
                                    <LoadingState label="Loading AI summary status..." />
                                ) : aiError ? (
                                    <ErrorState description={aiError} onRetry={() => void fetchAiSummaryStatus()} />
                                ) : aiSummary ? (
                                    <div className="space-y-5">
                                        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
                                            <StatCard label="Status" value={getSummaryStatusLabel(aiSummary.status)} tone="amber" />
                                            <StatCard label="Text comments" value={aiSummary.commentCount} tone="sky" />
                                            <StatCard label="Requested" value={formatDateTime(aiSummary.requestedAt)} tone="slate" />
                                            <StatCard label="Finished" value={formatDateTime(aiSummary.finishedAt)} tone="emerald" />
                                        </div>

                                        <div className="grid gap-4 lg:grid-cols-[minmax(0,1.3fr)_minmax(320px,0.7fr)]">
                                            <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                                                <div className="flex flex-wrap items-center gap-2">
                                                    <span className="inline-flex rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-slate-500">
                                                        {getSummaryStatusLabel(aiSummary.status)}
                                                    </span>
                                                </div>

                                                <div className="mt-4">
                                                    {aiSummary.summary ? (
                                                        <p className="text-sm leading-7 text-slate-700">{aiSummary.summary}</p>
                                                    ) : aiSummary.status === "FAILED" ? (
                                                        <p className="text-sm leading-7 text-rose-600">{aiSummary.errorMessage || "AI summary generation failed."}</p>
                                                    ) : aiSummary.status === "NOT_REQUESTED" ? (
                                                        <p className="text-sm leading-7 text-slate-600">No AI summary has been generated for this survey yet. The system only analyzes text feedback after you explicitly request it.</p>
                                                    ) : (
                                                        <p className="text-sm leading-7 text-slate-600">The summary job is processing in the background. This panel refreshes automatically until a result is available.</p>
                                                    )}
                                                </div>
                                            </div>

                                            <div className="rounded-3xl border border-slate-200 bg-slate-50 p-6 text-sm text-slate-600">
                                                <p className="text-xs font-bold uppercase tracking-[0.18em] text-slate-500">Job details</p>
                                                <div className="mt-4 grid gap-3">
                                                    <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Job id</span><span className="font-medium text-slate-900">{aiSummary.jobId ?? "-"}</span></div>
                                                    <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Requested at</span><span className="font-medium text-slate-900">{formatDateTime(aiSummary.requestedAt)}</span></div>
                                                    <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Started at</span><span className="font-medium text-slate-900">{formatDateTime(aiSummary.startedAt)}</span></div>
                                                    <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Finished at</span><span className="font-medium text-slate-900">{formatDateTime(aiSummary.finishedAt)}</span></div>
                                                </div>
                                            </div>
                                        </div>

                                        {aiSummary.summary ? (
                                            <div className="grid gap-4 xl:grid-cols-3">
                                                <SummaryListCard title="Highlights" items={aiSummary.highlights} emptyMessage="No strong positive theme was extracted from the current text responses." tone="sky" />
                                                <SummaryListCard title="Concerns" items={aiSummary.concerns} emptyMessage="No repeated concern cluster was extracted from the current text responses." tone="rose" />
                                                <SummaryListCard title="Recommended actions" items={aiSummary.actions} emptyMessage="No concrete action recommendation was generated for the current text responses." tone="amber" />
                                            </div>
                                        ) : null}
                                    </div>
                                ) : null}
                            </SectionCard>

                            <SectionCard title="Text comments" description="Anonymous comments are grouped by question. Search applies only to comment text and question copy.">
                                <div className="space-y-5">
                                    <SearchInput value={commentQuery} onChange={setCommentQuery} placeholder="Search comments or question text" />
                                    {filteredTextQuestions.length === 0 ? (
                                        <EmptyState title="No matching comments" description="Adjust the comment search to find the qualitative feedback you need." icon="comment" />
                                    ) : (
                                        filteredTextQuestions.map((question, index) => {
                                            const isExpanded = expandedQuestions[question.id] ?? false;
                                            const visibleComments = isExpanded ? question.comments : question.comments.slice(0, 3);
                                            return (
                                                <article key={question.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
                                                    <div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                                                        <div>
                                                            <span className="inline-flex rounded-full border border-slate-200 bg-white px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-slate-500">Text question {index + 1}</span>
                                                            <h3 className="mt-3 text-xl font-bold text-slate-950">{question.content}</h3>
                                                            <p className="mt-2 text-sm text-slate-500">{question.comments.length} comment{question.comments.length === 1 ? "" : "s"}</p>
                                                        </div>
                                                        {question.comments.length > 3 ? (
                                                            <button type="button" onClick={() => setExpandedQuestions((current) => ({ ...current, [question.id]: !isExpanded }))} className="rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                                                                {isExpanded ? "Collapse" : "Show all"}
                                                            </button>
                                                        ) : null}
                                                    </div>

                                                    <div className="mt-4 space-y-3">
                                                        {visibleComments.map((comment, commentIndex) => {
                                                            const commentKey = `${question.id}-${commentIndex}`;
                                                            const isLong = comment.length > 220;
                                                            const isCommentExpanded = expandedComments[commentKey] ?? false;
                                                            const displayComment = isLong && !isCommentExpanded ? `${comment.slice(0, 220)}...` : comment;
                                                            return (
                                                                <div key={commentKey} className="rounded-2xl border border-slate-200 bg-white px-4 py-4 text-sm leading-6 text-slate-700">
                                                                    <p>{displayComment}</p>
                                                                    {isLong ? (
                                                                        <button type="button" onClick={() => setExpandedComments((current) => ({ ...current, [commentKey]: !isCommentExpanded }))} className="mt-3 text-sm font-semibold text-slate-700 underline underline-offset-2">
                                                                            {isCommentExpanded ? "Show less" : "Show more"}
                                                                        </button>
                                                                    ) : null}
                                                                </div>
                                                            );
                                                        })}
                                                    </div>
                                                </article>
                                            );
                                        })
                                    )}
                                </div>
                            </SectionCard>
                        </>
                    ) : null}
                </div>
            </div>
        </main>
    );
}

function SummaryListCard({ title, items, emptyMessage, tone }: { title: string; items: string[]; emptyMessage: string; tone: "sky" | "rose" | "amber" }) {
    const toneClasses = {
        sky: "border-sky-200 bg-sky-50 text-sky-800",
        rose: "border-rose-200 bg-rose-50 text-rose-800",
        amber: "border-amber-200 bg-amber-50 text-amber-800",
    }[tone];

    return (
        <article className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
            <h3 className="text-base font-bold text-slate-950">{title}</h3>
            {items.length === 0 ? (
                <p className="mt-3 text-sm leading-6 text-slate-500">{emptyMessage}</p>
            ) : (
                <div className="mt-4 space-y-3">
                    {items.map((item) => (
                        <div key={item} className={`rounded-2xl border px-4 py-4 text-sm leading-6 ${toneClasses}`}>
                            {item}
                        </div>
                    ))}
                </div>
            )}
        </article>
    );
}

function ProgressBar({ label, value, total, colorClassName }: { label: string; value: number; total: number; colorClassName: string }) {
    const percentage = total === 0 ? 0 : Math.round((value / total) * 100);
    return (
        <div className="space-y-2">
            <div className="flex items-center justify-between gap-4 text-sm">
                <span className="font-semibold text-slate-700">{label}</span>
                <span className="text-slate-500">{value} / {total} | {percentage}%</span>
            </div>
            <div className="h-3 overflow-hidden rounded-full bg-slate-200">
                <div className={`h-full rounded-full ${colorClassName}`} style={{ width: `${percentage}%` }} />
            </div>
        </div>
    );
}
