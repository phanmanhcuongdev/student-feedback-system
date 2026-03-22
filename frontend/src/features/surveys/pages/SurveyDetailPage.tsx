import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import SurveyDetailMain from "../components/SurveyDetailMain";
import Footer from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
import SubmissionResultModal from "../popup/SubmissionResultModal";
import type { AnswersState, SurveyDetail } from "../../../types/surveyDetail";
import {getSurveyDetail, submitSurvey} from "../../../api/surveyApi";

type SubmitModalState = {
    isOpen: boolean;
    success: boolean;
    message: string;
};


export default function SurveyDetailPage() {
    const { id } = useParams();
    const navigate = useNavigate();
    const surveyId = Number(id);

    const [survey, setSurvey] = useState<SurveyDetail | null>(null);
    const [answers, setAnswers] = useState<AnswersState>({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    const [submitModal, setSubmitModal] = useState<SubmitModalState>({
        isOpen: false,
        success: false,
        message: "",
    });

    useEffect(() => {
        async function fetchSurveyDetail() {
            try {
                setLoading(true);
                setError(null);

                const data = await getSurveyDetail(surveyId);
                setSurvey(data);
            } catch (err) {
                setError(err instanceof Error ? err.message : "Failed to load survey detail");
            } finally {
                setLoading(false);
            }
        }

        if (!Number.isNaN(surveyId)) {
            fetchSurveyDetail();
        } else {
            setLoading(false);
            setError("Invalid survey id");
        }
    }, [surveyId]);

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
        if (!survey || submitting) return;

        try
        {
            setSubmitting(true);
            setError(null);

            const payload = {
                studentId: 6,
                surveyId: survey.id,
                answers: survey.questions.map((question) => {
                    const value = answers[question.id];

                    if (question.type === "RATING") {
                        return {
                            questionId: question.id,
                            rating: typeof value === "number" ? value : null,
                            comment: null
                        };
                    }

                    return {
                        questionId: question.id,
                        rating: null,
                        comment: typeof value === "string" ? value : null,
                    };
                }),
            };

            const result = await submitSurvey(payload);

            setSubmitModal({
                isOpen: true,
                success: result.success,
                message: result.message,
            });

        } catch (err) {
            setSubmitModal({
                isOpen: true,
                success: false,
                message: err instanceof Error ? err.message : "Failed to submit survey",
            });
        } finally {
            setSubmitting(false);
        }


    }

    function handleModalOk() {
        if (submitModal.success) {
            navigate("/");
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