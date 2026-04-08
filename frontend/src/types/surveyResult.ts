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
    description: string;
    startDate: string;
    endDate: string;
    status: string;
    responseCount: number;
};

export type SurveyResultDetail = SurveyResultSummary & {
    questions: QuestionStatistics[];
};
