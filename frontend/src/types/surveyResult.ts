export type RatingBreakdown = {
    rating: number;
    count: number;
};

export type QuestionStatistics = {
    id: number;
    content: string;
    type: string;
    responseCount: number;
    averageRating: number | null;
    ratingBreakdown: RatingBreakdown[];
    comments: string[];
};

export type SurveyResultSummary = {
    id: number;
    title: string;
    description: string | null;
    startDate: string;
    endDate: string;
    status: string;
    lifecycleState: string;
    runtimeStatus: string;
    recipientScope: string;
    recipientDepartmentName: string | null;
    responseCount: number;
    targetedCount: number;
    openedCount: number;
    submittedCount: number;
    responseRate: number;
};

export type SurveyResultDetail = SurveyResultSummary & {
    questions: QuestionStatistics[];
};
