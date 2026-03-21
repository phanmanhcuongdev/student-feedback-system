import type { Survey } from "../../../types/survey";
import { useNavigate } from "react-router-dom";

type SurveyCardProps = {
    survey: Survey;
};

export default function SurveyCard({survey}: SurveyCardProps) {
    function getRemainingTime(endDate: string) {
        const now = new Date();
        const end = new Date(endDate);

        const diff = end.getTime() - now.getTime();

        if (diff <= 0) return null;

        const minutes = Math.floor(diff / (1000 * 60));
        const hours = Math.floor(diff / (1000 * 60 * 60));
        const days = Math.floor(diff / (1000 * 60 * 60 * 24));

        if (days > 0) return `${days} days left`;
        if (hours > 0) return `${hours} hours left`;
        return `${minutes} mins left`;
    }

    const badgeClass =
        survey.status === "OPEN"
            ? "bg-green-100 text-green-700"
            : survey.status === "CLOSED"
                ? "bg-red-100 text-red-700"
                : "bg-slate-100 text-slate-600";

    const dotClass =
        survey.status === "OPEN"
            ? "bg-green-500"
            : survey.status === "CLOSED"
                ? "bg-red-500"
                : "bg-slate-400";

    const navigate = useNavigate();

    function handleStartSurvey(id: number) {
        navigate(`/surveys/${id}`);
    }

    return (
        <div className="bg-white rounded-xl p-6 transition-all hover:shadow-lg hover:-translate-y-1 flex flex-col h-full border border-slate-200">

            {/* HEADER */}
            <div className="flex justify-between items-start mb-4">

                <div className={`px-3 py-1 rounded-full text-[10px] font-bold uppercase tracking-wider flex items-center gap-1 ${badgeClass}`}>
                    <span className={`w-1.5 h-1.5 rounded-full ${dotClass}`}></span>
                    {survey.status}
                </div>

                <span className="text-slate-500 text-xs font-medium">
                    {getRemainingTime(survey.endDate)}
                </span>
            </div>

            {/* BODY */}
            <h3 className="text-xl font-bold text-slate-900 mb-2">
                {survey.title}
            </h3>

            <p className="text-slate-500 text-sm mb-6 flex-grow">
                {survey.description}
            </p>

            {/* FOOTER */}
            <div className="space-y-4 pt-6 border-t border-slate-200">

                <div className="flex items-center justify-between text-xs text-slate-500 font-medium">

                    <div className="flex items-center gap-2">
                        <span className="material-symbols-outlined text-sm">calendar_today</span>
                        <span>
                            {new Date(survey.startDate).toLocaleDateString("vi-VN")} -{" "}
                            {new Date(survey.endDate).toLocaleDateString("vi-VN")}
                        </span>
                    </div>

                    <div className="flex items-center gap-1">
                        <span className="text-blue-600">65%</span>
                        <span>capacity</span>
                    </div>
                </div>

                {/* Progress */}
                <div className="h-1.5 w-full bg-slate-200 rounded-full overflow-hidden">
                    <div className="h-full bg-blue-600 w-[65%]"></div>
                </div>

                {/* Button */}
                <button className="w-full bg-blue-600 text-white py-3 rounded-lg font-bold text-sm shadow-md active:scale-95 transition-all flex items-center justify-center gap-2 group hover:bg-blue-700" onClick={() => handleStartSurvey(survey.id)}>
                    Start Survey
                    <span className="material-symbols-outlined text-sm group-hover:translate-x-1 transition-transform">
            arrow_forward
          </span>
                </button>

            </div>
        </div>
    );
}