export function getDefaultAppRoute(role: string | null | undefined, studentStatus?: string | null): string {
    if (role === "ADMIN") {
        return "/dashboard/admin";
    }

    if (role === "TEACHER") {
        return "/dashboard/lecturer";
    }

    if (role === "STUDENT" && (studentStatus === "EMAIL_VERIFIED" || studentStatus === "REJECTED")) {
        return "/upload-documents";
    }

    return "/dashboard/student";
}
