import { useTranslation } from "react-i18next";
import type { AnswersState, SurveyDetail } from "../../../types/surveyDetail";

type SurveyDetailMainProps = {
    survey: SurveyDetail;
    answers: AnswersState;
    onRatingChange: (questionId: number, value: number) => void;
    onTextChange: (questionId: number, value: string) => void;
    onSubmit: () => void;
    submitting: boolean;
};

function getStatusBadgeClasses(status: SurveyDetail["status"]) {
    switch (status) {
        case "OPEN":
            return "bg-green-100 text-green-700";
        case "CLOSED":
            return "bg-red-100 text-red-700";
        default:
            return "bg-slate-100 text-slate-600";
    }
}

function getDaysLeft(endDate: string, t: (key: string, options?: Record<string, unknown>) => string) {
    const now = new Date();
    const end = new Date(endDate);
    const diff = end.getTime() - now.getTime();

    if (diff <= 0) return t("surveys:surveys.detail.daysLeft.closed");
    const days = Math.ceil(diff / (1000 * 60 * 60 * 24));
    return t("surveys:surveys.detail.daysLeft.closesIn", { count: days });
}

export default function SurveyDetailMain({
    survey,
    answers,
    onRatingChange,
    onTextChange,
    onSubmit,
    submitting,
}: SurveyDetailMainProps) {
    const { t } = useTranslation(["surveys"]);
    const answeredCount = survey.questions.filter((q) => {
        const value = answers[q.id];
        if (q.type === "RATING") return typeof value === "number";
        return typeof value === "string" && value.trim().length > 0;
    }).length;

    const progress = survey.questions.length
        ? Math.round((answeredCount / survey.questions.length) * 100)
        : 0;

    const canSubmit = answeredCount === survey.questions.length && survey.status === "OPEN" && !submitting;

    const ratingLabels: Record<number, string> = {
        1: t("surveys:surveys.detail.rating.veryUnsatisfied"),
        2: t("surveys:surveys.detail.rating.unsatisfied"),
        3: t("surveys:surveys.detail.rating.neutral"),
        4: t("surveys:surveys.detail.rating.satisfied"),
        5: t("surveys:surveys.detail.rating.verySatisfied"),
    };

    return (
        <main className="flex-1 bg-[#f8f9ff] px-6 pt-28 pb-20">
            <div className="mx-auto max-w-3xl">
                <header className="mb-10">
                    <div className="mb-4 space-y-1">
                        <div className="mb-2 flex items-center gap-3">
                            <span
                                className={`rounded-full px-3 py-1 text-[10px] font-bold uppercase tracking-widest ${getStatusBadgeClasses(
                                    survey.status
                                )}`}
                            >
                                {survey.status}
                            </span>
                            <span className="text-sm font-medium text-slate-400">
                                {getDaysLeft(survey.endDate, t)}
                            </span>
                        </div>

                        <h1 className="font-manrope text-4xl font-extrabold tracking-tight text-[#0b1c30]">
                            {survey.title}
                        </h1>

                        <p className="max-w-2xl text-lg leading-relaxed text-slate-500">
                            {survey.description}
                        </p>
                    </div>

                    <div className="mt-8">
                        <div className="mb-2 flex items-end justify-between">
                            <span className="text-sm font-semibold text-blue-700">
                                {t("surveys:surveys.detail.progress.answered", { answered: answeredCount, total: survey.questions.length })}
                            </span>
                            <span className="text-sm text-slate-400">{t("surveys:surveys.detail.progress.complete", { progress })}</span>
                        </div>

                        <div className="h-2 w-full overflow-hidden rounded-full bg-[#d3e4fe]">
                            <div
                                className="h-full bg-[#0058be] transition-all duration-300"
                                style={{ width: `${progress}%` }}
                            />
                        </div>
                    </div>
                </header>

                <div className="space-y-6">
                    {survey.questions.map((question, index) => (
                        <div
                            key={question.id}
                            className="rounded-xl bg-white p-8 shadow-sm ring-1 ring-[#c2c6d6]/15"
                        >
                            <div className="flex gap-4">
                                <span className="flex h-8 w-8 shrink-0 items-center justify-center rounded-lg bg-[#eff4ff] text-sm font-bold text-[#0058be]">
                                    {String(index + 1).padStart(2, "0")}
                                </span>

                                <div className="w-full space-y-6">
                                    <h3 className="text-xl font-bold text-[#0b1c30]">
                                        {question.content}
                                    </h3>

                                    {question.type === "RATING" ? (
                                        <div className="space-y-5">
                                            <div className="flex flex-wrap gap-3">
                                                {[1, 2, 3, 4, 5].map((value) => {
                                                    const selected = answers[question.id] === value;

                                                    return (
                                                        <button
                                                            key={value}
                                                            type="button"
                                                            onClick={() => onRatingChange(question.id, value)}
                                                            className={[
                                                                "flex h-12 w-12 items-center justify-center rounded-lg text-lg transition-all",
                                                                selected
                                                                    ? "bg-[#0058be] font-bold text-white ring-4 ring-blue-200"
                                                                    : "border border-slate-300 bg-white font-medium text-slate-700 hover:border-[#0058be] hover:text-[#0058be]",
                                                            ].join(" ")}
                                                        >
                                                            {value}
                                                        </button>
                                                    );
                                                })}
                                            </div>

                                            <div className="px-1 text-center text-[11px] font-bold uppercase tracking-wider text-slate-400">
                                                {typeof answers[question.id] === "number"
                                                    ? ratingLabels[answers[question.id] as number]
                                                    : t("surveys:surveys.detail.rating.select")}
                                            </div>
                                        </div>
                                    ) : (
                                        <textarea
                                            rows={4}
                                            value={
                                                typeof answers[question.id] === "string"
                                                    ? answers[question.id]
                                                    : ""
                                            }
                                            onChange={(e) => onTextChange(question.id, e.target.value)}
                                            placeholder={t("surveys:surveys.detail.textPlaceholder")}
                                            className="w-full rounded-xl bg-[#eff4ff] p-4 text-[#0b1c30] placeholder:text-slate-400 outline-none ring-0 transition-all focus:ring-2 focus:ring-blue-300"
                                        />
                                    )}
                                </div>
                            </div>
                        </div>
                    ))}

                    <div className="flex justify-center pt-8">
                        <button
                            type="button"
                            onClick={onSubmit}
                            disabled={!canSubmit}
                            className={[
                                "flex items-center gap-2 rounded-lg px-12 py-4 font-bold transition-all",
                                canSubmit
                                    ? "bg-blue-600 text-white shadow-md hover:scale-[1.02] hover:bg-blue-700 active:scale-95"
                                    : "cursor-not-allowed bg-slate-300 text-slate-500",
                            ].join(" ")}
                        >
                            <span>{submitting ? t("surveys:surveys.detail.buttons.submitting") : t("surveys:surveys.detail.buttons.submit")}</span>
                            <span className="material-symbols-outlined text-sm">arrow_forward</span>
                        </button>
                    </div>
                </div>
            </div>
        </main>
    );
}
