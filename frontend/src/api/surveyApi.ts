import axiosInstance from "./axios";
import type { StudentSurveyPage, Survey } from "../types/survey";
import type { SurveyDetail, SubmitSurveyRequest, SubmitSurveyResponse } from "../types/surveyDetail";

export const getAllSurveys = async (params: {
    status?: string;
    submitted?: boolean;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDir?: string;
}): Promise<StudentSurveyPage> => {
    const response = await axiosInstance.get<StudentSurveyPage>("/v1/surveys", { params });
    return response.data;
};

export const getSurveyById = async (id: number): Promise<Survey> => {
    const response = await axiosInstance.get<Survey>(`/v1/surveys/${id}`);
    return response.data;
};

export async function getSurveyDetail(surveyId: number): Promise<SurveyDetail> {
    const response = await axiosInstance.get<SurveyDetail>(`/v1/surveys/${surveyId}/detail`);
    return response.data;
}

export async function submitSurvey(
    surveyId: number,
    request: SubmitSurveyRequest
): Promise<SubmitSurveyResponse> {
    const response = await axiosInstance.post<SubmitSurveyResponse>(
        `/v1/surveys/${surveyId}/submit`,
        request
    );
    return response.data;
}
