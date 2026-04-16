type StatusKind =
    | "role"
    | "account"
    | "onboarding"
    | "surveyLifecycle"
    | "surveyRuntime"
    | "surveyVisibility"
    | "surveyParticipation"
    | "feedback";

type StatusBadgeProps = {
    kind: StatusKind;
    value: string | null | undefined;
};

type BadgeMeta = {
    label: string;
    className: string;
};

function fallbackLabel(value: string | null | undefined) {
    if (!value) {
        return "Unknown";
    }

    return value.replace(/_/g, " ").toLowerCase().replace(/^\w/, (letter) => letter.toUpperCase());
}

function getBadgeMeta(kind: StatusKind, value: string | null | undefined): BadgeMeta {
    const normalizedValue = value ?? "";

    const maps: Record<StatusKind, Record<string, BadgeMeta>> = {
        role: {
            STUDENT: { label: "Student", className: "border-blue-200 bg-blue-50 text-blue-700" },
            TEACHER: { label: "Lecturer", className: "border-sky-200 bg-sky-50 text-sky-700" },
            ADMIN: { label: "Admin", className: "border-slate-200 bg-slate-100 text-slate-700" },
        },
        account: {
            ACTIVE: { label: "Active", className: "border-emerald-200 bg-emerald-50 text-emerald-700" },
            INACTIVE: { label: "Inactive", className: "border-red-200 bg-red-50 text-red-700" },
        },
        onboarding: {
            EMAIL_VERIFIED: { label: "Email Verified", className: "border-sky-200 bg-sky-50 text-sky-700" },
            EMAIL_UNVERIFIED: { label: "Email Unverified", className: "border-slate-200 bg-slate-100 text-slate-700" },
            PENDING: { label: "Pending Review", className: "border-amber-200 bg-amber-50 text-amber-700" },
            REJECTED: { label: "Rejected", className: "border-red-200 bg-red-50 text-red-700" },
            ACTIVE: { label: "Approved", className: "border-emerald-200 bg-emerald-50 text-emerald-700" },
        },
        surveyLifecycle: {
            DRAFT: { label: "Draft", className: "border-slate-200 bg-slate-100 text-slate-700" },
            PUBLISHED: { label: "Published", className: "border-emerald-200 bg-emerald-50 text-emerald-700" },
            CLOSED: { label: "Closed", className: "border-amber-200 bg-amber-50 text-amber-700" },
            ARCHIVED: { label: "Archived", className: "border-indigo-200 bg-indigo-50 text-indigo-700" },
        },
        surveyRuntime: {
            NOT_OPEN: { label: "Not Open", className: "border-slate-200 bg-slate-100 text-slate-700" },
            OPEN: { label: "Open", className: "border-emerald-200 bg-emerald-50 text-emerald-700" },
            CLOSED: { label: "Closed", className: "border-red-200 bg-red-50 text-red-700" },
        },
        surveyVisibility: {
            VISIBLE: { label: "Visible", className: "border-emerald-200 bg-emerald-50 text-emerald-700" },
            HIDDEN: { label: "Hidden", className: "border-slate-200 bg-slate-100 text-slate-700" },
        },
        surveyParticipation: {
            ASSIGNED: { label: "Assigned", className: "border-slate-200 bg-slate-100 text-slate-700" },
            OPENED: { label: "Opened", className: "border-sky-200 bg-sky-50 text-sky-700" },
            SUBMITTED: { label: "Submitted", className: "border-emerald-200 bg-emerald-50 text-emerald-700" },
        },
        feedback: {
            UNRESOLVED: { label: "Unresolved", className: "border-amber-200 bg-amber-50 text-amber-700" },
            RESPONDED: { label: "Responded", className: "border-emerald-200 bg-emerald-50 text-emerald-700" },
        },
    };

    return maps[kind][normalizedValue] ?? {
        label: fallbackLabel(value),
        className: "border-slate-200 bg-slate-50 text-slate-700",
    };
}

export default function StatusBadge({ kind, value }: StatusBadgeProps) {
    const meta = getBadgeMeta(kind, value);

    return (
        <span className={`inline-flex rounded-full border px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] ${meta.className}`}>
            {meta.label}
        </span>
    );
}
