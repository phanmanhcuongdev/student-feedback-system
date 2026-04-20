export type UserRole = "STUDENT" | "ADMIN" | "LECTURER" | string;

export type AuthSession = {
    userId: number;
    email: string;
    role: UserRole;
    studentStatus: string | null;
    accessToken: string;
};

export type LoginResponse = {
    success: boolean;
    code: string;
    userId: number | null;
    role: UserRole | null;
    studentStatus: string | null;
    accessToken: string | null;
    message: string;
};

export type RegisterStudentRequest = {
    name: string;
    email: string;
    password: string;
    studentCode: string;
    departmentName: string;
};

export type RegisterStudentResponse = {
    success: boolean;
    code: string;
    message: string;
};

export type VerifyEmailResponse = {
    success: boolean;
    code: string;
    message: string;
    studentStatus: string | null;
};

export type UploadDocumentsResponse = {
    success: boolean;
    code: string;
    message: string;
};

export type OnboardingStatusResponse = {
    success: boolean;
    code: string;
    message: string;
    status: string | null;
    reviewReason: string | null;
    reviewNotes: string | null;
    hasUploadedDocuments: boolean;
    canUploadDocuments: boolean;
    resubmissionCount: number;
};

export type ForgotPasswordResponse = {
    success: boolean;
    code: string;
    message: string;
};

export type ResetPasswordResponse = {
    success: boolean;
    code: string;
    message: string;
};

export type ChangePasswordResponse = {
    success: boolean;
    code: string;
    message: string;
};
