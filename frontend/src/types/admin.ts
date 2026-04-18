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
    teacherCode: string | null;
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
    teacherCode: string | null;
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
    totalTeachers: number;
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
