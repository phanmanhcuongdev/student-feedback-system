import { type ReactNode, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getApiErrorMessage } from "../../../api/apiError";
import { getSurveyResults } from "../../../api/surveyResultApi";
import StatusBadge from "../../../components/ui/StatusBadge";
import type { SurveyResultMetrics, SurveyResultSummary } from "../../../types/surveyResult";

export default function LecturerDashboardPage() {
    const { i18n, t } = useTranslation("admin");
    const [results, setResults] = useState<SurveyResultSummary[]>([]);
    const [metrics, setMetrics] = useState<SurveyResultMetrics>({
        total: 0,
        open: 0,
        closed: 0,
        averageResponseRate: 0,
        totalSubmitted: 0,
        totalResponses: 0,
    });
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function load() {
            try {
                setLoading(true);
                setError("");
                const response = await getSurveyResults({ page: 0, size: 4, sortBy: "responseCount", sortDir: "desc" });
                setResults(response.items);
                setMetrics(response.metrics);
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, t("admin.dashboard.errors.loadFailed")));
            } finally {
                setLoading(false);
            }
        }

        load();
    }, [i18n.resolvedLanguage, t]);

    const topResponseSurveys = results.slice(0, 4);

    return (
        <main className="bg-[linear-gradient(180deg,#f5fbff_0%,#eef6fb_45%,#f8fbfd_100%)]">
            <div className="mx-auto max-w-screen-xl px-6 py-10">
                    <div className="mb-10 max-w-3xl">
                        <span className="mb-3 inline-flex rounded-full border border-sky-200 bg-sky-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-sky-700">
                            {t("admin.dashboard.lecturer.header.eyebrow")}
                        </span>
                        <h1 className="text-4xl font-extrabold tracking-tight text-slate-950">
                            {t("admin.dashboard.lecturer.header.title")}
                        </h1>
                        <p className="mt-4 text-base leading-7 text-slate-500">
                            {t("admin.dashboard.lecturer.header.description")}
                        </p>
                    </div>

                    {error ? (
                        <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                            {error}
                        </div>
                    ) : null}

                    {loading ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-10 text-sm font-medium text-slate-500 shadow-sm">
                            {t("admin.dashboard.loading")}
                        </div>
                    ) : (
                        <>
                            <div className="grid gap-5 md:grid-cols-3">
                                <Metric label={t("admin.dashboard.lecturer.stats.trackedSurveys")} value={metrics.total} tone="sky" />
                                <Metric label={t("admin.dashboard.lecturer.stats.totalResponses")} value={metrics.totalResponses} tone="emerald" />
                                <Metric label={t("admin.dashboard.lecturer.stats.closedSurveys")} value={metrics.closed} tone="slate" />
                            </div>

                            <div className="mt-8 grid gap-6 lg:grid-cols-[1.1fr_0.9fr]">
                                <section className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                                    <div className="mb-5 flex items-center justify-between gap-4">
                                        <div>
                                            <h2 className="text-2xl font-bold text-slate-950">{t("admin.dashboard.lecturer.mostActive.title")}</h2>
                                            <p className="mt-2 text-sm text-slate-500">
                                                {t("admin.dashboard.lecturer.mostActive.description")}
                                            </p>
                                        </div>
                                        <Link
                                            to="/survey-results"
                                            className="rounded-full border border-sky-200 bg-sky-50 px-4 py-2 text-sm font-bold text-sky-700 transition hover:border-sky-300 hover:bg-sky-100"
                                        >
                                            {t("admin.dashboard.lecturer.mostActive.openResultList")}
                                        </Link>
                                    </div>

                                    {topResponseSurveys.length === 0 ? (
                                        <p className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-5 text-sm text-slate-500">
                                            {t("admin.dashboard.lecturer.mostActive.empty")}
                                        </p>
                                    ) : (
                                        <div className="space-y-4">
                                            {topResponseSurveys.map((survey) => (
                                                <article key={survey.id} className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
                                                    <div className="flex items-start justify-between gap-4">
                                                        <div>
                                                            <h3 className="text-lg font-bold text-slate-900">{survey.title}</h3>
                                                            <p className="mt-2 text-sm leading-6 text-slate-600">
                                                                {survey.description || t("admin.dashboard.common.noSurveyDescription")}
                                                            </p>
                                                        </div>
                                                        <StatusBadge kind="surveyRuntime" value={survey.status} />
                                                    </div>
                                                    <div className="mt-4 flex items-center justify-between gap-4 text-sm text-slate-500">
                                                        <span>{t("admin.dashboard.lecturer.mostActive.responseCount", { count: survey.responseCount })}</span>
                                                        <Link to={`/survey-results/${survey.id}`} className="font-bold text-sky-700">
                                                            {t("admin.dashboard.lecturer.mostActive.openStatistics")}
                                                        </Link>
                                                    </div>
                                                </article>
                                            ))}
                                        </div>
                                    )}
                                </section>

                                <section className="space-y-6">
                                    <Panel title={t("admin.dashboard.lecturer.openSurveys.title")} subtitle={t("admin.dashboard.lecturer.openSurveys.description")}>
                                        <StatText value={metrics.open} label={t("admin.dashboard.lecturer.openSurveys.label")} />
                                    </Panel>
                                    <Panel title={t("admin.dashboard.lecturer.coverage.title")} subtitle={t("admin.dashboard.lecturer.coverage.description")}>
                                        <StatText
                                            value={metrics.total === 0 ? 0 : Math.round(metrics.totalResponses / metrics.total)}
                                            label={t("admin.dashboard.lecturer.coverage.label")}
                                        />
                                    </Panel>
                                </section>
                            </div>
                        </>
                    )}
            </div>
        </main>
    );
}

function Metric({ label, value, tone }: { label: string; value: number; tone: "sky" | "emerald" | "slate" }) {
    const toneClassName = {
        sky: "border-sky-200 bg-sky-50 text-sky-700",
        emerald: "border-emerald-200 bg-emerald-50 text-emerald-700",
        slate: "border-slate-200 bg-slate-50 text-slate-700",
    }[tone];

    return (
        <div className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
            <span className={`inline-flex rounded-full border px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] ${toneClassName}`}>
                {label}
            </span>
            <p className="mt-5 text-4xl font-extrabold tracking-tight text-slate-950">{value}</p>
        </div>
    );
}

function Panel({ title, subtitle, children }: { title: string; subtitle: string; children: ReactNode }) {
    return (
        <section className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
            <h2 className="text-2xl font-bold text-slate-950">{title}</h2>
            <p className="mt-2 text-sm text-slate-500">{subtitle}</p>
            <div className="mt-5">{children}</div>
        </section>
    );
}

function StatText({ value, label }: { value: number; label: string }) {
    return (
        <div className="rounded-2xl border border-slate-200 bg-slate-50 p-5">
            <p className="text-4xl font-extrabold tracking-tight text-slate-950">{value}</p>
            <p className="mt-2 text-sm font-medium text-slate-500">{label}</p>
        </div>
    );
}
