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
    recipientScope: "ALL_STUDENTS" | "DEPARTMENT";
    recipientDepartmentId: number | null;
}

export interface ManagedSurveySummary {
    id: number;
    title: string;
    description: string;
    startDate: string | null;
    endDate: string | null;
    status: "OPEN" | "CLOSED" | "NOT_OPEN";
    hidden: boolean;
    recipientScope: "ALL_STUDENTS" | "DEPARTMENT";
    recipientDepartmentId: number | null;
    responseCount: number;
}

export interface ManagedSurveyDetail extends ManagedSurveySummary {
    questions: Array<{
        id: number;
        content: string;
        type: "RATING" | "TEXT";
    }>;
}
