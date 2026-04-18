import axios from "./axios";
import type { SurveyResultDetail, SurveyResultPage } from "../types/surveyResult";

export async function getSurveyResults(params?: {
    keyword?: string;
    lifecycleState?: string;
    runtimeStatus?: string;
    recipientScope?: string;
    startDateFrom?: string;
    endDateTo?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
}): Promise<SurveyResultPage> {
    const response = await axios.get<SurveyResultPage>("/v1/survey-results", { params });
    return response.data;
}

export async function getSurveyResult(surveyId: number): Promise<SurveyResultDetail> {
    const response = await axios.get<SurveyResultDetail>(`/v1/survey-results/${surveyId}`);
    return response.data;
}
