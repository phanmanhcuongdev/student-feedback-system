import { useState } from "react";
import { getApiErrorMessage, getApiErrorPayload } from "../../../api/apiError";
import { submitSurvey } from "../../../api/surveyApi";
import type { AnswersState, SurveyDetail, SubmitSurveyRequest, SubmitSurveyResponse } from "../../../types/surveyDetail";

function toSubmitRequest(survey: SurveyDetail, answers: AnswersState): SubmitSurveyRequest {
    return {
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
}

export function useSubmitSurvey() {
    const [submitting, setSubmitting] = useState(false);

    async function submit(survey: SurveyDetail, answers: AnswersState): Promise<SubmitSurveyResponse> {
        try {
            setSubmitting(true);
            return await submitSurvey(survey.id, toSubmitRequest(survey, answers));
        } catch (error) {
            const payload = getApiErrorPayload(error);

            return {
                success: false,
                code: payload?.code === "ALREADY_SUBMITTED"
                    || payload?.code === "SURVEY_CLOSED"
                    || payload?.code === "INVALID_INPUT"
                    || payload?.code === "SURVEY_NOT_FOUND"
                    || payload?.code === "STUDENT_NOT_FOUND"
                    ? payload.code
                    : "INVALID_INPUT",
                message: getApiErrorMessage(error, "Failed to submit survey"),
            };
        } finally {
            setSubmitting(false);
        }
    }

    return {
        submitting,
        submit,
    };
}
