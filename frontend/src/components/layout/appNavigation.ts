export type NavigationRole = "STUDENT" | "LECTURER" | "ADMIN";

export type NavigationItem = {
    labelKey: string;
    to: string;
    icon: string;
};

export type NavigationGroup = {
    titleKey: string;
    items: NavigationItem[];
};

export function getNavigationGroups(role: string): NavigationGroup[] {
    const workspaceByRole: Record<NavigationRole, NavigationGroup[]> = {
        STUDENT: [
            {
                titleKey: "layout.nav.groups.studentWorkspace",
                items: [
                    { labelKey: "layout.nav.items.dashboard", to: "/dashboard/student", icon: "dashboard" },
                    { labelKey: "layout.nav.items.surveys", to: "/surveys", icon: "assignment" },
                    { labelKey: "layout.nav.items.notifications", to: "/notifications", icon: "notifications" },
                    { labelKey: "layout.nav.items.feedback", to: "/feedback", icon: "forum" },
                ],
            },
        ],
        LECTURER: [
            {
                titleKey: "layout.nav.groups.lecturerWorkspace",
                items: [
                    { labelKey: "layout.nav.items.dashboard", to: "/dashboard/lecturer", icon: "dashboard" },
                    { labelKey: "layout.nav.items.surveyResults", to: "/survey-results", icon: "analytics" },
                    { labelKey: "layout.nav.items.feedbackReview", to: "/feedback/manage", icon: "rate_review" },
                ],
            },
        ],
        ADMIN: [
            {
                titleKey: "layout.nav.groups.adminOperations",
                items: [
                    { labelKey: "layout.nav.items.dashboard", to: "/dashboard/admin", icon: "dashboard" },
                    { labelKey: "layout.nav.items.users", to: "/admin/users", icon: "group" },
                    { labelKey: "layout.nav.items.pendingStudents", to: "/admin/students/pending", icon: "pending_actions" },
                    { labelKey: "layout.nav.items.surveys", to: "/admin/surveys", icon: "assignment" },
                    { labelKey: "layout.nav.items.questionBank", to: "/admin/question-bank", icon: "quiz" },
                    { labelKey: "layout.nav.items.surveyTemplates", to: "/admin/survey-templates", icon: "library_add" },
                    { labelKey: "layout.nav.items.surveyResults", to: "/survey-results", icon: "analytics" },
                    { labelKey: "layout.nav.items.auditLogs", to: "/admin/audit-logs", icon: "manage_search" },
                    { labelKey: "layout.nav.items.feedbackReview", to: "/feedback/manage", icon: "rate_review" },
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
            titleKey: "layout.nav.groups.account",
            items: [
                { labelKey: "layout.nav.items.accountOverview", to: "/account", icon: "account_circle" },
                { labelKey: "layout.nav.items.security", to: "/account/security", icon: "shield_lock" },
            ],
        },
    ];
}

export function getRoleLabelKey(role: string) {
    switch (role) {
        case "STUDENT":
            return "common.status.role.student";
        case "LECTURER":
            return "common.status.role.lecturer";
        case "ADMIN":
            return "common.status.role.administrator";
        default:
            return "";
    }
}

export function getStatusLabelKey(status: string | null | undefined): string {
    if (!status) {
        return "";
    }

    const labels: Record<string, string> = {
        ACTIVE: "common.status.onboarding.approved",
        PENDING: "common.status.onboarding.pendingReview",
        REJECTED: "common.status.onboarding.rejected",
        EMAIL_VERIFIED: "common.status.onboarding.emailVerified",
        EMAIL_UNVERIFIED: "common.status.onboarding.emailUnverified",
        OPEN: "common.status.surveyRuntime.open",
        NOT_OPEN: "common.status.surveyRuntime.notOpen",
        CLOSED: "common.status.surveyRuntime.closed",
    };

    return labels[status] ?? "";
}
