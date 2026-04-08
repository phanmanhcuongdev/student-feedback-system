export type PendingStudent = {
    id: number;
    name: string;
    email: string;
    studentCode: string;
    departmentName: string | null;
    status: string;
    studentCardImageUrl: string | null;
    nationalIdImageUrl: string | null;
};

export type AdminActionResponse = {
    success: boolean;
    code: string;
    message: string;
};
