import { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import ErrorState from "../../../components/ui/ErrorState";
import LoadingState from "../../../components/ui/LoadingState";
import type { AnswersState } from "../../../types/surveyDetail";
import SurveyDetailMain from "../components/SurveyDetailMain";
import { useSubmitSurvey } from "../hooks/useSubmitSurvey";
import { useSurveyDetail } from "../hooks/useSurveyDetail";
import SubmissionResultModal from "../popup/SubmissionResultModal";

type SubmitModalState = {
    isOpen: boolean;
    success: boolean;
    message: string;
};

export default function SurveyDetailPage() {
    const { t } = useTranslation(["surveys"]);
    const { id } = useParams();
    const navigate = useNavigate();
    const surveyId = Number(id);

    const [answers, setAnswers] = useState<AnswersState>({});
    const { survey, loading, error } = useSurveyDetail(surveyId);
    const { submitting, submit } = useSubmitSurvey();
    const [submitModal, setSubmitModal] = useState<SubmitModalState>({
        isOpen: false,
        success: false,
        message: "",
    });

    function handleRatingChange(questionId: number, value: number) {
        setAnswers((prev) => ({
            ...prev,
            [questionId]: value,
        }));
    }

    function handleTextChange(questionId: number, value: string) {
        setAnswers((prev) => ({
            ...prev,
            [questionId]: value,
        }));
    }

    async function handleSubmit() {
        if (!survey || submitting) {
            return;
        }

        const result = await submit(survey, answers);
        setSubmitModal({
            isOpen: true,
            success: result.success,
            message: result.message,
        });
    }

    function handleModalOk() {
        if (submitModal.success) {
            navigate("/surveys");
            return;
        }

        setSubmitModal((prev) => ({
            ...prev,
            isOpen: false,
        }));
    }

    return (
        <>
            <div className="min-h-screen bg-slate-100 text-slate-900">
                <div className="mx-auto max-w-3xl px-6 pt-8">
                    <Link
                        to="/surveys"
                        className="inline-flex items-center gap-2 rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-semibold text-slate-600 shadow-sm transition hover:border-slate-300 hover:text-slate-900"
                    >
                        <span className="material-symbols-outlined text-[18px]">arrow_back</span>
                        <span>{t("surveys:surveys.detail.backToSurveys")}</span>
                    </Link>
                </div>

                {loading && (
                    <div className="mx-auto max-w-3xl px-6 pt-12">
                        <LoadingState label={t("surveys:surveys.detail.loading")} />
                    </div>
                )}

                {error && (
                    <div className="mx-auto max-w-3xl px-6 pt-12">
                        <ErrorState description={error} />
                    </div>
                )}

                {!loading && !error && survey && (
                    <SurveyDetailMain
                        survey={survey}
                        answers={answers}
                        onRatingChange={handleRatingChange}
                        onTextChange={handleTextChange}
                        onSubmit={handleSubmit}
                        submitting={submitting}
                    />
                )}
            </div>

            <SubmissionResultModal
                isOpen={submitModal.isOpen}
                success={submitModal.success}
                message={submitModal.message}
                onOk={handleModalOk}
            />
        </>
    );
}
