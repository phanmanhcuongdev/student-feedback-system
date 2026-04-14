import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getApiErrorMessage } from "../../../api/apiError";
import { getSurveyResult } from "../../../api/surveyResultApi";
import MainFooter from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
import type { QuestionStatistics, SurveyResultDetail } from "../../../types/surveyResult";

function formatDateRange(startDate: string, endDate: string) {
    const formatter = new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
    });

    return `${formatter.format(new Date(startDate))} - ${formatter.format(new Date(endDate))}`;
}

function formatRate(value: number) {
    return `${value.toFixed(1)}%`;
}

function RatingQuestionBlock({ question }: { question: QuestionStatistics }) {
    return (
        <div className="grid gap-5 lg:grid-cols-[220px_minmax(0,1fr)]">
            <div className="rounded-2xl border border-blue-200 bg-blue-50 px-5 py-4">
                <p className="text-xs font-bold uppercase tracking-[0.2em] text-blue-700">Average rating</p>
                <p className="mt-3 text-4xl font-extrabold tracking-tight text-slate-950">
                    {question.averageRating === null ? "-" : question.averageRating.toFixed(2)}
                </p>
                <p className="mt-2 text-sm text-slate-500">
                    Based on {question.responseCount} response{question.responseCount === 1 ? "" : "s"}
                </p>
            </div>

            <div className="space-y-3">
                {question.ratingBreakdown.map((item) => {
                    const denominator = question.responseCount > 0 ? question.responseCount : 1;
                    const percentage = Math.round((item.count / denominator) * 100);

                    return (
                        <div key={item.rating} className="space-y-2">
                            <div className="flex items-center justify-between gap-4 text-sm">
                                <span className="font-semibold text-slate-700">{item.rating} / 5</span>
                                <span className="text-slate-500">
                                    {item.count} response{item.count === 1 ? "" : "s"} | {percentage}%
                                </span>
                            </div>
                            <div className="h-3 overflow-hidden rounded-full bg-slate-100">
                                <div
                                    className="h-full rounded-full bg-[linear-gradient(90deg,#1d78ec_0%,#0f5bcf_100%)]"
                                    style={{ width: `${percentage}%` }}
                                />
                            </div>
                        </div>
                    );
                })}
            </div>
        </div>
    );
}

function TextQuestionBlock({ question }: { question: QuestionStatistics }) {
    return question.comments.length === 0 ? (
        <div className="rounded-2xl border border-slate-200 bg-slate-50 px-5 py-4 text-sm text-slate-500">
            No written comments were submitted for this question.
        </div>
    ) : (
        <div className="space-y-3">
            {question.comments.map((comment, index) => (
                <div
                    key={`${question.id}-${index}`}
                    className="rounded-2xl border border-slate-200 bg-slate-50 px-5 py-4 text-sm leading-6 text-slate-700"
                >
                    {comment}
                </div>
            ))}
        </div>
    );
}

export default function SurveyResultDetailPage() {
    const { id } = useParams();
    const surveyId = Number(id);
    const [survey, setSurvey] = useState<SurveyResultDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

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
            fetchSurveyResult();
            return;
        }

        setLoading(false);
        setError("Invalid survey id.");
    }, [surveyId]);

    return (
        <>
            <MainHeader />

            <main className="min-h-screen bg-[linear-gradient(180deg,#f3f6ff_0%,#edf3fb_46%,#f8fbff_100%)]">
                <div className="mx-auto max-w-screen-xl px-6 py-10">
                    <Link
                        to="/survey-results"
                        className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-600 shadow-sm transition hover:border-slate-300 hover:text-slate-900"
                    >
                        <span className="material-symbols-outlined text-[18px]">arrow_back</span>
                        <span>Back to results</span>
                    </Link>

                    {loading ? (
                        <div className="mt-8 rounded-[28px] border border-slate-200 bg-white px-6 py-10 text-center text-sm font-medium text-slate-500 shadow-sm">
                            Loading survey statistics...
                        </div>
                    ) : error ? (
                        <div className="mt-8 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                            {error}
                        </div>
                    ) : survey ? (
                        <div className="mt-8 space-y-8">
                            <section className="rounded-[32px] border border-slate-200 bg-white p-8 shadow-[0_20px_48px_rgba(15,23,42,0.06)]">
                                <div className="flex flex-col gap-6 lg:flex-row lg:items-start lg:justify-between">
                                    <div className="max-w-3xl">
                                        <span className="inline-flex rounded-full border border-blue-200 bg-blue-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.22em] text-blue-700">
                                            {survey.status}
                                        </span>
                                        <h1 className="mt-4 text-4xl font-extrabold tracking-tight text-slate-950">
                                            {survey.title}
                                        </h1>
                                        <p className="mt-4 text-base leading-7 text-slate-500">
                                            {survey.description || "No survey description provided."}
                                        </p>
                                    </div>

                                        <div className="grid min-w-[260px] gap-4 rounded-[28px] border border-slate-200 bg-slate-50 p-5 text-sm text-slate-600">
                                            <div className="flex items-center justify-between gap-4">
                                                <span className="font-semibold text-slate-500">Targeted</span>
                                                <span className="text-2xl font-extrabold tracking-tight text-slate-950">
                                                    {survey.targetedCount}
                                                </span>
                                            </div>
                                            <div className="flex items-center justify-between gap-4">
                                                <span className="font-semibold text-slate-500">Opened</span>
                                                <span className="font-bold text-slate-900">{survey.openedCount}</span>
                                            </div>
                                            <div className="flex items-center justify-between gap-4">
                                                <span className="font-semibold text-slate-500">Submitted</span>
                                                <span className="font-bold text-slate-900">{survey.submittedCount}</span>
                                            </div>
                                            <div className="flex items-center justify-between gap-4">
                                                <span className="font-semibold text-slate-500">Response rate</span>
                                                <span className="font-bold text-slate-900">{formatRate(survey.responseRate)}</span>
                                            </div>
                                            <div className="flex items-center justify-between gap-4">
                                                <span className="font-semibold text-slate-500">Questions</span>
                                                <span className="font-bold text-slate-900">{survey.questions.length}</span>
                                        </div>
                                        <div className="flex items-center justify-between gap-4">
                                            <span className="font-semibold text-slate-500">Window</span>
                                            <span className="text-right font-medium text-slate-900">
                                                {formatDateRange(survey.startDate, survey.endDate)}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </section>

                            <section className="space-y-6">
                                {survey.questions.map((question, index) => (
                                    <article
                                        key={question.id}
                                        className="rounded-[28px] border border-slate-200 bg-white p-7 shadow-[0_16px_36px_rgba(15,23,42,0.05)]"
                                    >
                                        <div className="mb-6 flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                                            <div className="max-w-3xl">
                                                <span className="inline-flex rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.22em] text-slate-500">
                                                    Question {index + 1} | {question.type}
                                                </span>
                                                <h2 className="mt-4 text-2xl font-bold text-slate-950">
                                                    {question.content}
                                                </h2>
                                            </div>
                                            <div className="rounded-2xl bg-slate-100 px-4 py-3 text-sm font-semibold text-slate-600">
                                                {question.responseCount} response{question.responseCount === 1 ? "" : "s"}
                                            </div>
                                        </div>

                                        {question.type === "RATING" ? (
                                            <RatingQuestionBlock question={question} />
                                        ) : (
                                            <TextQuestionBlock question={question} />
                                        )}
                                    </article>
                                ))}
                            </section>
                        </div>
                    ) : null}
                </div>
            </main>

            <MainFooter />
        </>
    );
}
