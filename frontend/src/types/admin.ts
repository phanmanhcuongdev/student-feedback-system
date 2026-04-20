export type PendingStudent = {
    id: number;
    name: string;
    email: string;
    studentCode: string;
    departmentName: string | null;
    status: string;
    studentCardImageUrl: string | null;
    nationalIdImageUrl: string | null;
    reviewReason: string | null;
    reviewNotes: string | null;
    resubmissionCount: number;
};

export type PendingStudentPage = {
    items: PendingStudent[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
};

export type ManagedUserSummary = {
    id: number;
    email: string;
    role: string;
    name: string;
    departmentId: number | null;
    departmentName: string | null;
    studentStatus: string | null;
    active: boolean;
    studentCode: string | null;
    lecturerCode: string | null;
};

export type ManagedUserDetail = {
    id: number;
    email: string;
    role: string;
    active: boolean;
    name: string;
    departmentId: number | null;
    departmentName: string | null;
    studentCode: string | null;
    lecturerCode: string | null;
    studentStatus: string | null;
};

export type AdminActionResponse = {
    success: boolean;
    code: string;
    message: string;
};

export type ApproveStudentRequest = {
    reviewNotes?: string;
};

export type RejectStudentRequest = {
    reviewReason: string;
    reviewNotes?: string;
};

export type ManagedUserMetrics = {
    totalUsers: number;
    totalStudents: number;
    totalLecturers: number;
    totalAdmins: number;
    totalInactive: number;
    totalPending: number;
};

export type ManagedUserPage = {
    items: ManagedUserSummary[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
    metrics: ManagedUserMetrics;
};

export type DepartmentOption = {
    id: number;
    name: string;
};

export type AdminAnalyticsMetrics = {
    totalSurveys: number;
    totalDrafts: number;
    totalPublished: number;
    totalClosed: number;
    totalArchived: number;
    totalHidden: number;
    totalOpenRuntime: number;
    totalTargeted: number;
    totalOpened: number;
    totalSubmitted: number;
    averageResponseRate: number;
};

export type AdminAnalyticsCount = {
    key: string;
    count: number;
};

export type AdminAnalyticsDepartment = {
    departmentId: number | null;
    departmentName: string;
    surveyCount: number;
    targetedCount: number;
    openedCount: number;
    submittedCount: number;
    responseRate: number;
};

export type AdminAnalyticsAttentionSurvey = {
    id: number;
    title: string;
    lifecycleState: string;
    runtimeStatus: string;
    departmentName: string | null;
    targetedCount: number;
    openedCount: number;
    submittedCount: number;
    responseRate: number;
};

export type AdminAnalyticsOverview = {
    metrics: AdminAnalyticsMetrics;
    lifecycleCounts: AdminAnalyticsCount[];
    runtimeCounts: AdminAnalyticsCount[];
    departmentBreakdown: AdminAnalyticsDepartment[];
    attentionSurveys: AdminAnalyticsAttentionSurvey[];
};

export type AuditLogEntry = {
    id: number;
    actorUserId: number;
    actionType: string;
    targetType: string;
    targetId: number;
    summary: string;
    details: string | null;
    oldState: string | null;
    newState: string | null;
    createdAt: string;
};

export type AuditLogPage = {
    items: AuditLogEntry[];
    page: number;
    size: number;
    totalElements: number;
    totalPages: number;
};
