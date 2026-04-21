export type SurveyAiSummary = {
    surveyId: number;
    status: string;
    jobId: number | null;
    commentCount: number;
    summary: string | null;
    highlights: string[];
    concerns: string[];
    actions: string[];
    errorMessage: string | null;
    requestedAt: string | null;
    startedAt: string | null;
    finishedAt: string | null;
};
