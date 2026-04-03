import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Footer from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
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
            <MainHeader />

            <div className="min-h-screen bg-[#f8f9ff] text-[#0b1c30]">
                {loading && (
                    <div className="mx-auto max-w-3xl px-6 pt-28 text-lg font-medium text-slate-500">
                        Loading survey...
                    </div>
                )}

                {error && (
                    <div className="mx-auto max-w-3xl px-6 pt-28">
                        <div className="rounded-xl bg-red-50 p-4 text-red-600 shadow-sm">
                            {error}
                        </div>
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
            <Footer />
        </>
    );
}
