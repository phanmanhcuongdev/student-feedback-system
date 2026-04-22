import { useEffect, useMemo, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getApiErrorMessage } from "../../../api/apiError";
import { getOnboardingStatus, uploadDocuments } from "../../../api/authApi";
import AuthShell from "../components/AuthShell";
import { useAuth } from "../useAuth";
import type { OnboardingStatusResponse } from "../../../types/auth";

const MAX_FILE_SIZE_BYTES = 5 * 1024 * 1024;

export default function UploadDocumentsPage() {
    const { t } = useTranslation(["auth", "validation"]);
    const navigate = useNavigate();
    const { session, logout } = useAuth();
    const isRejectedSession = session?.studentStatus === "REJECTED";
    const [studentCard, setStudentCard] = useState<File | null>(null);
    const [nationalId, setNationalId] = useState<File | null>(null);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [submitting, setSubmitting] = useState(false);
    const [status, setStatus] = useState<OnboardingStatusResponse | null>(null);
    const [statusLoading, setStatusLoading] = useState(true);

    const canSubmit = useMemo(
        () => studentCard !== null && nationalId !== null && !submitting && status?.canUploadDocuments !== false,
        [nationalId, status?.canUploadDocuments, studentCard, submitting]
    );

    function handleFileChange(
        file: File | null,
        label: string,
        assign: (nextFile: File | null) => void
    ) {
        setSuccess("");

        if (!file) {
            assign(null);
            return;
        }

        if (file.size > MAX_FILE_SIZE_BYTES) {
            assign(null);
            setError(t("validation:validation.auth.fileMaxSize", { label, size: "5MB" }));
            return;
        }

        setError("");
        assign(file);
    }

    useEffect(() => {
        async function fetchOnboardingStatus() {
            try {
                setStatusLoading(true);
                setError("");
                setStatus(await getOnboardingStatus());
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, t("auth:auth.uploadDocuments.errors.statusUnavailable")));
            } finally {
                setStatusLoading(false);
            }
        }

        if (session?.role === "STUDENT") {
            fetchOnboardingStatus();
        }
    }, [session?.role]);

    if (!session) {
        return <Navigate to="/login" replace />;
    }

    if (session.role !== "STUDENT") {
        return <Navigate to="/" replace />;
    }

    if (session.studentStatus !== "EMAIL_VERIFIED" && session.studentStatus !== "REJECTED") {
        return <Navigate to="/" replace />;
    }

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!studentCard || !nationalId) {
            return;
        }
        if (studentCard.size > MAX_FILE_SIZE_BYTES || nationalId.size > MAX_FILE_SIZE_BYTES) {
            setError(t("validation:validation.auth.eachFileMaxSize", { size: "5MB" }));
            return;
        }

        setSubmitting(true);
        setError("");
        setSuccess("");

        try {
            const response = await uploadDocuments(studentCard, nationalId);
            if (!response.success) {
                setError(response.message || t("auth:auth.uploadDocuments.errors.failed"));
                return;
            }

            setSuccess(response.message);
            logout();
            navigate("/login", {
                replace: true,
                state: {
                    notice: isRejectedSession
                        ? t("auth:auth.uploadDocuments.notices.resubmitted")
                        : t("auth:auth.uploadDocuments.notices.uploaded"),
                },
            });
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("auth:auth.uploadDocuments.errors.unavailable")));
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <AuthShell
            eyebrow={isRejectedSession ? t("auth:auth.uploadDocuments.eyebrow.resubmission") : t("auth:auth.uploadDocuments.eyebrow.upload")}
            title={isRejectedSession
                ? t("auth:auth.uploadDocuments.title.resubmission")
                : t("auth:auth.uploadDocuments.title.upload")}
            description={isRejectedSession
                ? t("auth:auth.uploadDocuments.description.resubmission")
                : t("auth:auth.uploadDocuments.description.upload")}
            footer={(
                <p className="text-sm text-slate-500">
                    {t("auth:auth.uploadDocuments.footer.signedInAs")}{" "}
                    <span className="font-semibold text-blue-700">{session.email}</span>
                </p>
            )}
        >
            {statusLoading ? (
                <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4 text-sm text-slate-500">
                    {t("auth:auth.uploadDocuments.status.loading")}
                </div>
            ) : null}

            {status && isRejectedSession ? (
                <div className="space-y-3 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-4 text-sm text-amber-900">
                    <p className="font-semibold uppercase tracking-[0.12em] text-amber-700">{t("auth:auth.uploadDocuments.review.title")}</p>
                    {status.reviewReason ? (
                        <p>
                            <span className="font-semibold">{t("auth:auth.uploadDocuments.review.reason")}</span> {status.reviewReason}
                        </p>
                    ) : null}
                    {status.reviewNotes ? (
                        <p className="whitespace-pre-wrap">
                            <span className="font-semibold">{t("auth:auth.uploadDocuments.review.notes")}</span> {status.reviewNotes}
                        </p>
                    ) : null}
                    <p>
                        <span className="font-semibold">{t("auth:auth.uploadDocuments.review.previousResubmissions")}</span> {status.resubmissionCount}
                    </p>
                </div>
            ) : null}

            <form className="space-y-4" onSubmit={handleSubmit}>
                <label className="block space-y-2">
                    <span className="text-sm font-semibold text-slate-700">{t("auth:auth.uploadDocuments.fields.studentCard")}</span>
                    <input
                        type="file"
                        accept="image/*,.pdf,.doc,.docx"
                        onChange={(event) => handleFileChange(event.target.files?.[0] ?? null, t("auth:auth.uploadDocuments.fileLabels.studentCard"), setStudentCard)}
                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition file:mr-4 file:rounded-full file:border-0 file:bg-blue-600 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-white hover:file:bg-blue-700"
                    />
                    <span className="block text-xs text-slate-500">{t("auth:auth.uploadDocuments.help.maxFileSize", { size: "5MB" })}</span>
                </label>

                <label className="block space-y-2">
                    <span className="text-sm font-semibold text-slate-700">{t("auth:auth.uploadDocuments.fields.nationalId")}</span>
                    <input
                        type="file"
                        accept="image/*,.pdf,.doc,.docx"
                        onChange={(event) => handleFileChange(event.target.files?.[0] ?? null, t("auth:auth.uploadDocuments.fileLabels.nationalId"), setNationalId)}
                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition file:mr-4 file:rounded-full file:border-0 file:bg-blue-600 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-white hover:file:bg-blue-700"
                    />
                    <span className="block text-xs text-slate-500">{t("auth:auth.uploadDocuments.help.maxFileSize", { size: "5MB" })}</span>
                </label>

                {error ? (
                    <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                        {error}
                    </div>
                ) : null}

                {success ? (
                    <div className="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
                        {success}
                    </div>
                ) : null}

                <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4 text-sm text-slate-500">
                    {isRejectedSession
                        ? t("auth:auth.uploadDocuments.noticeText.resubmission")
                        : t("auth:auth.uploadDocuments.noticeText.upload")}
                </div>

                <button
                    type="submit"
                    disabled={!canSubmit}
                    className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#0f5bcf_0%,#1d78ec_100%)] px-5 py-3.5 text-sm font-bold text-white shadow-[0_16px_36px_rgba(29,120,236,0.28)] transition hover:translate-y-[-1px] hover:shadow-[0_20px_44px_rgba(29,120,236,0.32)] disabled:cursor-not-allowed disabled:opacity-60 disabled:shadow-none"
                >
                    <span>{submitting ? t("auth:auth.uploadDocuments.buttons.submitting") : isRejectedSession ? t("auth:auth.uploadDocuments.buttons.resubmit") : t("auth:auth.uploadDocuments.buttons.upload")}</span>
                    <span className="material-symbols-outlined text-base">cloud_upload</span>
                </button>

                {success ? (
                    <Link
                        to="/login"
                        className="inline-flex w-full items-center justify-center gap-2 rounded-2xl border border-slate-300 bg-white px-5 py-3 text-sm font-bold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50"
                    >
                        <span>{t("auth:auth.uploadDocuments.buttons.returnToLogin")}</span>
                        <span className="material-symbols-outlined text-base">arrow_forward</span>
                    </Link>
                ) : null}
            </form>
        </AuthShell>
    );
}
