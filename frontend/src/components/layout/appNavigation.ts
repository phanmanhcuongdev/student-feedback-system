export type NavigationRole = "STUDENT" | "LECTURER" | "ADMIN";

export type NavigationItem = {
    label: string;
    to: string;
    icon: string;
};

export type NavigationGroup = {
    title: string;
    items: NavigationItem[];
};

export function getNavigationGroups(role: string): NavigationGroup[] {
    const workspaceByRole: Record<NavigationRole, NavigationGroup[]> = {
        STUDENT: [
            {
                title: "Student Workspace",
                items: [
                    { label: "Dashboard", to: "/dashboard/student", icon: "dashboard" },
                    { label: "Surveys", to: "/surveys", icon: "assignment" },
                    { label: "Notifications", to: "/notifications", icon: "notifications" },
                    { label: "Feedback", to: "/feedback", icon: "forum" },
                ],
            },
        ],
        LECTURER: [
            {
                title: "Lecturer Workspace",
                items: [
                    { label: "Dashboard", to: "/dashboard/lecturer", icon: "dashboard" },
                    { label: "Survey Results", to: "/survey-results", icon: "analytics" },
                    { label: "Feedback Review", to: "/feedback/manage", icon: "rate_review" },
                ],
            },
        ],
        ADMIN: [
            {
                title: "Admin Operations",
                items: [
                    { label: "Dashboard", to: "/dashboard/admin", icon: "dashboard" },
                    { label: "Users", to: "/admin/users", icon: "group" },
                    { label: "Pending Students", to: "/admin/students/pending", icon: "pending_actions" },
                    { label: "Surveys", to: "/admin/surveys", icon: "assignment" },
                    { label: "Question Bank", to: "/admin/question-bank", icon: "quiz" },
                    { label: "Survey Templates", to: "/admin/survey-templates", icon: "library_add" },
                    { label: "Survey Results", to: "/survey-results", icon: "analytics" },
                    { label: "Audit Logs", to: "/admin/audit-logs", icon: "manage_search" },
                    { label: "Feedback Review", to: "/feedback/manage", icon: "rate_review" },
                ],
            },
        ],
    };

    const normalizedRole: NavigationRole = role === "ADMIN" || role === "LECTURER" || role === "STUDENT"
        ? role
        : "STUDENT";

    return [
        ...workspaceByRole[normalizedRole],
        {
            title: "Account",
            items: [
                { label: "Account Overview", to: "/account", icon: "account_circle" },
                { label: "Security", to: "/account/security", icon: "shield_lock" },
            ],
        },
    ];
}

export function getRoleLabel(role: string) {
    switch (role) {
        case "STUDENT":
            return "Student";
        case "LECTURER":
            return "Lecturer";
        case "ADMIN":
            return "Administrator";
        default:
            return role;
    }
}

export function renderStatusLabel(status: string | null | undefined): string {
    if (!status) {
        return "";
    }

    const labels: Record<string, string> = {
        ACTIVE: "Approved",
        PENDING: "Pending Review",
        REJECTED: "Rejected",
        EMAIL_VERIFIED: "Email Verified",
        EMAIL_UNVERIFIED: "Email Unverified",
        OPEN: "Open",
        NOT_OPEN: "Not Open",
        CLOSED: "Closed",
    };

    return labels[status] ?? status
        .replace(/_/g, " ")
        .toLowerCase()
        .replace(/^\w/, (letter) => letter.toUpperCase());
}
