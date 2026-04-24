import { useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import type { Survey } from "../../../types/survey";

type SurveyCardProps = {
    survey: Survey;
};

export default function SurveyCard({ survey }: SurveyCardProps) {
    const { t } = useTranslation(["surveys"]);
    const navigate = useNavigate();
    const isDisabled = survey.status !== "OPEN" || survey.submitted;

    function getRemainingTime(endDate: string) {
        const now = new Date();
        const end = new Date(endDate);
        const diff = end.getTime() - now.getTime();

        if (diff <= 0) {
            return null;
        }

        const minutes = Math.floor(diff / (1000 * 60));
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));

        if (days > 0) return `${days} days left`;
        if (hours > 0) return `${hours} hours left`;
        return `${minutes} mins left`;
    }

    function getButtonText(status: Survey["status"]) {
        if (survey.submitted) {
            return t("surveys:surveys.card.completed");
        }

        switch (status) {
            case "OPEN":
                return t("surveys:surveys.card.start");
            case "CLOSED":
                return t("surveys:surveys.card.closed");
            case "NOT_OPEN":
                return t("surveys:surveys.card.notOpenYet");
            default:
                return t("surveys:surveys.card.unavailable");
        }
    }

    const badgeClass =
        survey.submitted
            ? "bg-emerald-100 text-emerald-700"
            : survey.status === "OPEN"
            ? "bg-green-100 text-green-700"
            : survey.status === "CLOSED"
                ? "bg-red-100 text-red-700"
                : "bg-slate-100 text-slate-600";

    const dotClass =
        survey.submitted
            ? "bg-emerald-500"
            : survey.status === "OPEN"
            ? "bg-green-500"
            : survey.status === "CLOSED"
                ? "bg-red-500"
                : "bg-slate-400";

    function handleStartSurvey(id: number) {
        if (isDisabled) {
            return;
        }

        navigate(`/surveys/${id}`);
    }

    return (
        <div
            className={[
                "flex h-full flex-col rounded-[24px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)] transition-all",
                isDisabled ? "" : "hover:-translate-y-1 hover:shadow-[0_24px_50px_rgba(15,23,42,0.08)]",
            ].join(" ")}
        >
            <div className="flex justify-between items-start mb-4">
                <div
                    className={`px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider flex items-center gap-1 ${badgeClass}`}
                >
                    <span className={`w-1.5 h-1.5 rounded-full ${dotClass}`}></span>
                    {survey.submitted ? t("surveys:surveys.card.completed") : survey.status}
                </div>

                <span className="text-slate-500 text-xs font-medium">
                    {getRemainingTime(survey.endDate)}
                </span>
            </div>

            <h3 className="mb-2 text-xl font-bold text-slate-900">
                {survey.title}
            </h3>

            <p className="mb-6 flex-grow text-sm leading-6 text-slate-500">
                {survey.description}
            </p>

            <div className="space-y-4 border-t border-slate-200 pt-6">
                <div className="flex items-center text-xs text-slate-500 font-medium">
                    <div className="flex items-center gap-2">
                        <span className="material-symbols-outlined text-sm">calendar_today</span>
                        <span>
                            {new Date(survey.startDate).toLocaleDateString("vi-VN")} -{" "}
                            {new Date(survey.endDate).toLocaleDateString("vi-VN")}
                        </span>
                    </div>
                </div>

                <button
                    type="button"
                    disabled={isDisabled}
                    onClick={() => handleStartSurvey(survey.id)}
                    className={[
                        "flex w-full items-center justify-center gap-2 rounded-xl py-3 text-sm font-bold transition-all",
                        isDisabled
                            ? "bg-slate-300 text-slate-500 cursor-not-allowed shadow-none"
                            : "group bg-[linear-gradient(135deg,#0f5bcf_0%,#1d78ec_100%)] text-white shadow-[0_16px_36px_rgba(29,120,236,0.22)] active:scale-95 hover:shadow-[0_18px_40px_rgba(29,120,236,0.28)]",
                    ].join(" ")}
                >
                    {getButtonText(survey.status)}

                    <span
                        className={[
                            "material-symbols-outlined text-sm transition-transform",
                            isDisabled ? "" : "group-hover:translate-x-1",
                        ].join(" ")}
                    >
                        arrow_forward
                    </span>
                </button>
            </div>
        </div>
    );
}
