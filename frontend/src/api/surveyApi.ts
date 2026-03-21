import axiosInstance from './axios';
import type { Survey } from '../types/survey';
import type { SurveyDetail } from '../types/surveyDetail';

export const getAllSurveys = async () : Promise<Survey[]> => {
    const response = await axiosInstance.get<Survey[]>('/api/v1/surveys');
    return response.data;
};

export const getSurveyById = async (id: number): Promise<Survey> => {
    const response = await axiosInstance.get<Survey>(`/api/v1/surveys/${id}`);
    return response.data;
};

export async function getSurveyDetail(surveyId: number): Promise<SurveyDetail> {
    const response = await axiosInstance.get(`/api/v1/surveys/${surveyId}/detail`);
    return response.data;
}