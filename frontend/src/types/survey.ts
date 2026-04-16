export type SurveyRuntimeStatus = "OPEN" | "CLOSED" | "NOT_OPEN";
export type SurveyLifecycleState = "DRAFT" | "PUBLISHED" | "CLOSED" | "ARCHIVED";

export interface Survey {
    id: number;
    title: string;
    description: string;
    startDate: string;
    endDate: string;
    createdBy: number;
    status: SurveyRuntimeStatus;
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
    description: string | null;
    startDate: string | null;
    endDate: string | null;
    lifecycleState: SurveyLifecycleState;
    runtimeStatus: SurveyRuntimeStatus;
    hidden: boolean;
    recipientScope: "ALL_STUDENTS" | "DEPARTMENT";
    recipientDepartmentId: number | null;
    recipientDepartmentName: string | null;
    responseCount: number;
    targetedCount: number;
    openedCount: number;
    submittedCount: number;
    responseRate: number;
}

export interface ManagedSurveyDetail extends ManagedSurveySummary {
    questions: Array<{
        id: number;
        content: string;
        type: "RATING" | "TEXT";
    }>;
    pendingRecipients: Array<{
        studentId: number;
        studentName: string;
        studentCode: string;
        departmentName: string | null;
        participationStatus: "ASSIGNED" | "OPENED" | "SUBMITTED";
        openedAt: string | null;
        submittedAt: string | null;
    }>;
}

export interface ManagedSurveyMetrics {
    totalSurveys: number;
    totalDrafts: number;
    totalPublished: number;
    totalOpen: number;
    totalClosed: number;
    totalHidden: number;
}

export interface ManagedSurveyPage {
    items: ManagedSurveySummary[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    metrics: ManagedSurveyMetrics;
}

export interface CreateSurveyResponse {
    success: boolean;
    surveyId: number;
    code: string;
    message: string;
}
