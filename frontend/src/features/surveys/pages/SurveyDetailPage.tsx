import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import SurveyDetailMain from "../components/SurveyDetailMain";
import Footer from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
import type { AnswersState, SurveyDetail } from "../../../types/surveyDetail";
import { getSurveyDetail } from "../../../api/surveyApi";

export default function SurveyDetailPage() {
    const { id } = useParams();
    const surveyId = Number(id);

    const [survey, setSurvey] = useState<SurveyDetail | null>(null);
    const [answers, setAnswers] = useState<AnswersState>({});
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

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

    function handleSubmit() {
        if (!survey) return;

        const payload = {
            studentId: 1,
            answers: survey.questions.map((question) => {
                const value = answers[question.id];

                if (question.type === "RATING") {
                    return {
                        questionId: question.id,
                        rating: typeof value === "number" ? value : null,
                        comment: null,
                    };
                }

                return {
                    questionId: question.id,
                    rating: null,
                    comment: typeof value === "string" ? value : null,
                };
            }),
        };

        console.log("submit payload", payload);
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
                    />
                )}
            </div>

            <Footer />
        </>
    );
}