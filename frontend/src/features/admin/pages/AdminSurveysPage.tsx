import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getManagedSurveys } from "../../../api/adminApi";
import { getApiErrorMessage } from "../../../api/apiError";
import MainFooter from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
import type { ManagedSurveySummary } from "../../../types/survey";

function formatDate(date: string | null) {
    if (!date) {
        return "Not set";
    }
    return new Intl.DateTimeFormat("en-GB", {
        day: "2-digit",
        month: "short",
        year: "numeric",
    }).format(new Date(date));
}

export default function AdminSurveysPage() {
    const [surveys, setSurveys] = useState<ManagedSurveySummary[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function load() {
            try {
                setLoading(true);
                setError("");
                setSurveys(await getManagedSurveys());
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, "Unable to load surveys."));
            } finally {
                setLoading(false);
            }
        }

        load();
    }, []);

    return (
        <>
            <MainHeader />
            <main className="min-h-screen bg-[linear-gradient(180deg,#f4f8ff_0%,#eef3f8_44%,#f7fafc_100%)]">
                <div className="mx-auto max-w-screen-xl px-6 py-10">
                    <div className="mb-10 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
                        <div className="max-w-3xl">
                            <span className="mb-3 inline-flex rounded-full border border-indigo-200 bg-indigo-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-indigo-700">
                                Admin / Surveys
                            </span>
                            <h1 className="text-4xl font-extrabold tracking-tight text-slate-950">
                                Survey management
                            </h1>
                            <p className="mt-4 text-base leading-7 text-slate-500">
                                Review survey status, recipients, visibility, and open each survey for edits or manual close.
                            </p>
                        </div>
                        <Link
                            to="/admin/surveys/create"
                            className="inline-flex items-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#4f46e5_0%,#3b82f6_100%)] px-5 py-3 text-sm font-bold text-white shadow-[0_16px_36px_rgba(79,70,229,0.22)]"
                        >
                            <span>Create survey</span>
                            <span className="material-symbols-outlined text-[18px]">add</span>
                        </Link>
                    </div>

                    {error ? (
                        <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                            {error}
                        </div>
                    ) : null}

                    {loading ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-10 text-sm font-medium text-slate-500 shadow-sm">
                            Loading surveys...
                        </div>
                    ) : (
                        <div className="grid gap-6 lg:grid-cols-2">
                            {surveys.map((survey) => (
                                <article key={survey.id} className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                                    <div className="mb-5 flex items-start justify-between gap-4">
                                        <div>
                                            <div className="flex flex-wrap items-center gap-2">
                                                <span className="rounded-full border border-sky-200 bg-sky-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-sky-700">
                                                    {survey.status}
                                                </span>
                                                <span className={`rounded-full border px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] ${survey.hidden ? "border-slate-200 bg-slate-100 text-slate-600" : "border-emerald-200 bg-emerald-50 text-emerald-700"}`}>
                                                    {survey.hidden ? "Hidden" : "Visible"}
                                                </span>
                                            </div>
                                            <h2 className="mt-3 text-2xl font-bold text-slate-950">{survey.title}</h2>
                                            <p className="mt-2 text-sm leading-6 text-slate-500">
                                                {survey.description || "No survey description provided."}
                                            </p>
                                        </div>
                                        <div className="rounded-2xl bg-slate-100 px-3 py-2 text-xs font-semibold uppercase tracking-[0.16em] text-slate-500">
                                            ID {survey.id}
                                        </div>
                                    </div>

                                    <div className="grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                        <div className="flex items-center justify-between gap-4">
                                            <span className="font-semibold text-slate-500">Recipients</span>
                                            <span className="font-medium text-slate-900">
                                                {survey.recipientScope === "DEPARTMENT" ? `Department ${survey.recipientDepartmentId}` : "All students"}
                                            </span>
                                        </div>
                                        <div className="flex items-center justify-between gap-4">
                                            <span className="font-semibold text-slate-500">Responses</span>
                                            <span className="font-medium text-slate-900">{survey.responseCount}</span>
                                        </div>
                                        <div className="flex items-center justify-between gap-4">
                                            <span className="font-semibold text-slate-500">Window</span>
                                            <span className="text-right font-medium text-slate-900">
                                                {formatDate(survey.startDate)} - {formatDate(survey.endDate)}
                                            </span>
                                        </div>
                                    </div>

                                    <div className="mt-6">
                                        <Link
                                            to={`/admin/surveys/${survey.id}/edit`}
                                            className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#0f5bcf_0%,#1d78ec_100%)] px-4 py-3 text-sm font-bold text-white shadow-[0_16px_36px_rgba(29,120,236,0.24)]"
                                        >
                                            <span>Manage survey</span>
                                            <span className="material-symbols-outlined text-[18px]">edit</span>
                                        </Link>
                                    </div>
                                </article>
                            ))}
                        </div>
                    )}
                </div>
            </main>
            <MainFooter />
        </>
    );
}
