export interface Survey {
    id: number;
    title: string;
    description: string;
    startDate: string;
    endDate: string;
    createdBy: number;
    status: "OPEN" | "CLOSED" | "NOT_OPEN";
}

export interface CreateQuestionData {
    content: string;
    type: "RATING" | "TEXT";
}

export interface CreateSurveyData {
    title: string;
    description: string | null;
    startDate: string | null;
    endDate: string | null;
    questions: CreateQuestionData[];
}