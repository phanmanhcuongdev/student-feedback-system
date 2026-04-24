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
    submitted: boolean;
}

export interface StudentSurveyPage {
    items: Survey[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export interface CreateQuestionData {
    content: string;
    type: "RATING" | "TEXT";
    questionBankEntryId?: number | null;
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

export interface QuestionBankEntry {
    id: number;
    content: string;
    type: "RATING" | "TEXT";
    category: string | null;
    active: boolean;
    createdAt: string;
    updatedAt: string | null;
}

export interface QuestionBankPage {
    items: QuestionBankEntry[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}

export interface SurveyTemplateQuestion {
    id: number;
    questionBankEntryId: number | null;
    content: string;
    type: "RATING" | "TEXT";
    displayOrder: number;
}

export interface SurveyTemplate {
    id: number;
    name: string;
    description: string | null;
    suggestedTitle: string | null;
    suggestedSurveyDescription: string | null;
    recipientScope: "ALL_STUDENTS" | "DEPARTMENT";
    recipientDepartmentId: number | null;
    active: boolean;
    createdAt: string;
    updatedAt: string | null;
    questions: SurveyTemplateQuestion[];
}

export interface SurveyTemplatePage {
    items: SurveyTemplate[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
}
