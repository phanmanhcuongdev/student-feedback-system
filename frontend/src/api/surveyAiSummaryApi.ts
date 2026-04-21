import axios from "./axios";
import type { SurveyAiSummary } from "../types/surveyAiSummary";

export async function getSurveyAiSummary(surveyId: number): Promise<SurveyAiSummary> {
    const response = await axios.get<SurveyAiSummary>(`/v1/survey-results/${surveyId}/ai-summary`);
    return response.data;
}

export async function generateSurveyAiSummary(surveyId: number): Promise<SurveyAiSummary> {
    const response = await axios.post<SurveyAiSummary>(`/v1/survey-results/${surveyId}/ai-summary/generate`);
    return response.data;
}
