export function getDefaultAppRoute(role: string | null | undefined, studentStatus?: string | null): string {
    if (role === "ADMIN") {
        return "/admin/students/pending";
    }

    if (role === "TEACHER") {
        return "/survey-results";
    }

    if (role === "STUDENT" && studentStatus === "EMAIL_VERIFIED") {
        return "/upload-documents";
    }

    return "/surveys";
}
