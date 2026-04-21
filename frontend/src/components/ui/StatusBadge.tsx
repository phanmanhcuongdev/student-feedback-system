import { useTranslation } from "react-i18next";

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
    labelKey: string;
    className: string;
};

function fallbackLabel(value: string | null | undefined) {
    if (!value) {
        return "";
    }

    return value.replace(/_/g, " ").toLowerCase().replace(/^\w/, (letter) => letter.toUpperCase());
}

function getBadgeMeta(kind: StatusKind, value: string | null | undefined): BadgeMeta {
    const normalizedValue = value ?? "";

    const maps: Record<StatusKind, Record<string, BadgeMeta>> = {
        role: {
            STUDENT: { labelKey: "common.status.role.student", className: "border-blue-200 bg-blue-50 text-blue-700" },
            LECTURER: { labelKey: "common.status.role.lecturer", className: "border-sky-200 bg-sky-50 text-sky-700" },
            ADMIN: { labelKey: "common.status.role.admin", className: "border-slate-200 bg-slate-100 text-slate-700" },
        },
        account: {
            ACTIVE: { labelKey: "common.status.account.active", className: "border-emerald-200 bg-emerald-50 text-emerald-700" },
            INACTIVE: { labelKey: "common.status.account.inactive", className: "border-red-200 bg-red-50 text-red-700" },
        },
        onboarding: {
            EMAIL_VERIFIED: { labelKey: "common.status.onboarding.emailVerified", className: "border-sky-200 bg-sky-50 text-sky-700" },
            EMAIL_UNVERIFIED: { labelKey: "common.status.onboarding.emailUnverified", className: "border-slate-200 bg-slate-100 text-slate-700" },
            PENDING: { labelKey: "common.status.onboarding.pendingReview", className: "border-amber-200 bg-amber-50 text-amber-700" },
            REJECTED: { labelKey: "common.status.onboarding.rejected", className: "border-red-200 bg-red-50 text-red-700" },
            ACTIVE: { labelKey: "common.status.onboarding.approved", className: "border-emerald-200 bg-emerald-50 text-emerald-700" },
        },
        surveyLifecycle: {
            DRAFT: { labelKey: "common.status.surveyLifecycle.draft", className: "border-slate-200 bg-slate-100 text-slate-700" },
            PUBLISHED: { labelKey: "common.status.surveyLifecycle.published", className: "border-emerald-200 bg-emerald-50 text-emerald-700" },
            CLOSED: { labelKey: "common.status.surveyLifecycle.closed", className: "border-amber-200 bg-amber-50 text-amber-700" },
            ARCHIVED: { labelKey: "common.status.surveyLifecycle.archived", className: "border-indigo-200 bg-indigo-50 text-indigo-700" },
        },
        surveyRuntime: {
            NOT_OPEN: { labelKey: "common.status.surveyRuntime.notOpen", className: "border-slate-200 bg-slate-100 text-slate-700" },
            OPEN: { labelKey: "common.status.surveyRuntime.open", className: "border-emerald-200 bg-emerald-50 text-emerald-700" },
            CLOSED: { labelKey: "common.status.surveyRuntime.closed", className: "border-red-200 bg-red-50 text-red-700" },
        },
        surveyVisibility: {
            VISIBLE: { labelKey: "common.status.surveyVisibility.visible", className: "border-emerald-200 bg-emerald-50 text-emerald-700" },
            HIDDEN: { labelKey: "common.status.surveyVisibility.hidden", className: "border-slate-200 bg-slate-100 text-slate-700" },
        },
        surveyParticipation: {
            ASSIGNED: { labelKey: "common.status.surveyParticipation.assigned", className: "border-slate-200 bg-slate-100 text-slate-700" },
            OPENED: { labelKey: "common.status.surveyParticipation.opened", className: "border-sky-200 bg-sky-50 text-sky-700" },
            SUBMITTED: { labelKey: "common.status.surveyParticipation.submitted", className: "border-emerald-200 bg-emerald-50 text-emerald-700" },
        },
        feedback: {
            UNRESOLVED: { labelKey: "common.status.feedback.unresolved", className: "border-amber-200 bg-amber-50 text-amber-700" },
            RESPONDED: { labelKey: "common.status.feedback.responded", className: "border-emerald-200 bg-emerald-50 text-emerald-700" },
        },
    };

    return maps[kind][normalizedValue] ?? {
        labelKey: "",
        className: "border-slate-200 bg-slate-50 text-slate-700",
    };
}

export default function StatusBadge({ kind, value }: StatusBadgeProps) {
    const { t } = useTranslation("common");
    const meta = getBadgeMeta(kind, value);
    const label = meta.labelKey ? t(meta.labelKey) : fallbackLabel(value) || t("common.state.unknown");

    return (
        <span className={`inline-flex rounded-full border px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] ${meta.className}`}>
            {label}
        </span>
    );
}
