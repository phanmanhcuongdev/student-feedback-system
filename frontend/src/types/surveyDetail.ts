export type QuestionType = "RATING" | "TEXT";

export type Question = {
    id: number;
    content: string;
    type: QuestionType;
};

export type SurveyStatus = "OPEN" | "CLOSED" | "NOT_OPEN";

export type SurveyDetail = {
    id: number;
    title: string;
    description: string;
    status: SurveyStatus;
    startDate: string;
    endDate: string;
    questions: Question[];
};

export type SubmitSurveyAnswer = {
    questionId: number;
    rating: number | null;
    comment: string | null;
};

export type SubmitSurveyRequest = {
    studentId: number;
    answers: SubmitSurveyAnswer[];
};

export type AnswersState = Record<number, number | string>;