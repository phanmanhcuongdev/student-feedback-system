import axios from "./axios";
import type {
    ChangePasswordResponse,
    ForgotPasswordResponse,
    LoginResponse,
    OnboardingStatusResponse,
    RegisterStudentRequest,
    RegisterStudentResponse,
    ResetPasswordResponse,
    UploadDocumentsResponse,
    VerifyEmailResponse,
} from "../types/auth";

export const login = async (email: string, password: string): Promise<LoginResponse> => {
    const res = await axios.post<LoginResponse>("/auth/login", {
        email,
        password,
    });

    return res.data;
};

export const registerStudent = async (request: RegisterStudentRequest): Promise<RegisterStudentResponse> => {
    const res = await axios.post<RegisterStudentResponse>("/auth/register-student", request);
    return res.data;
};

export const verifyEmail = async (token: string): Promise<VerifyEmailResponse> => {
    const res = await axios.get<VerifyEmailResponse>("/auth/verify-email", {
        params: { token },
    });
    return res.data;
};

export const uploadDocuments = async (
    studentCard: File,
    nationalId: File
): Promise<UploadDocumentsResponse> => {
    const formData = new FormData();
    formData.append("studentCard", studentCard);
    formData.append("nationalId", nationalId);

    const res = await axios.post<UploadDocumentsResponse>("/auth/upload-docs", formData, {
        headers: {
            "Content-Type": "multipart/form-data",
        },
    });

    return res.data;
};

export const getOnboardingStatus = async (): Promise<OnboardingStatusResponse> => {
    const res = await axios.get<OnboardingStatusResponse>("/auth/onboarding-status");
    return res.data;
};

export const forgotPassword = async (email: string): Promise<ForgotPasswordResponse> => {
    const res = await axios.post<ForgotPasswordResponse>("/auth/forgot-password", { email });
    return res.data;
};

export const resetPassword = async (
    token: string,
    newPassword: string
): Promise<ResetPasswordResponse> => {
    const res = await axios.post<ResetPasswordResponse>("/auth/reset-password", {
        token,
        newPassword,
    });
    return res.data;
};

export const changePassword = async (
    currentPassword: string,
    newPassword: string
): Promise<ChangePasswordResponse> => {
    const res = await axios.post<ChangePasswordResponse>("/auth/change-password", {
        currentPassword,
        newPassword,
    });
    return res.data;
};
