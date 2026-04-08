import axios from "./axios";
import type { SurveyResultDetail, SurveyResultSummary } from "../types/surveyResult";

export async function getSurveyResults(): Promise<SurveyResultSummary[]> {
    const response = await axios.get<SurveyResultSummary[]>("/v1/survey-results");
    return response.data;
}

export async function getSurveyResult(surveyId: number): Promise<SurveyResultDetail> {
    const response = await axios.get<SurveyResultDetail>(`/v1/survey-results/${surveyId}`);
    return response.data;
}
