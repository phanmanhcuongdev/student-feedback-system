import { useEffect, useMemo, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { getApiErrorMessage } from "../../../api/apiError";
import { getOnboardingStatus, uploadDocuments } from "../../../api/authApi";
import AuthShell from "../components/AuthShell";
import { useAuth } from "../useAuth";
import type { OnboardingStatusResponse } from "../../../types/auth";

export default function UploadDocumentsPage() {
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

    useEffect(() => {
        async function fetchOnboardingStatus() {
            try {
                setStatusLoading(true);
                setError("");
                setStatus(await getOnboardingStatus());
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, "Unable to load your onboarding status right now."));
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

        setSubmitting(true);
        setError("");
        setSuccess("");

        try {
            const response = await uploadDocuments(studentCard, nationalId);
            if (!response.success) {
                setError(response.message || "Upload failed");
                return;
            }

            setSuccess(response.message);
            logout();
            navigate("/login", {
                replace: true,
                state: {
                    notice: isRejectedSession
                        ? "Documents resubmitted successfully. Your account is back in the administrator review queue."
                        : "Documents uploaded successfully. Your account is now pending administrator approval.",
                },
            });
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to upload your documents right now."));
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <AuthShell
            eyebrow={isRejectedSession ? "Document Resubmission" : "Document Upload"}
            title={isRejectedSession
                ? "Correct and resubmit your verification documents"
                : "Upload required verification documents"}
            description={isRejectedSession
                ? "Your previous onboarding review was rejected. Fix the issues below and resubmit both documents."
                : "Submit both document images so your student account can move into the approval queue."}
            footer={(
                <p className="text-sm text-slate-500">
                    Signed in as{" "}
                    <span className="font-semibold text-blue-700">{session.email}</span>
                </p>
            )}
        >
            {statusLoading ? (
                <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4 text-sm text-slate-500">
                    Loading onboarding status...
                </div>
            ) : null}

            {status && isRejectedSession ? (
                <div className="space-y-3 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-4 text-sm text-amber-900">
                    <p className="font-semibold uppercase tracking-[0.12em] text-amber-700">Review feedback</p>
                    {status.reviewReason ? (
                        <p>
                            <span className="font-semibold">Reason:</span> {status.reviewReason}
                        </p>
                    ) : null}
                    {status.reviewNotes ? (
                        <p className="whitespace-pre-wrap">
                            <span className="font-semibold">Notes:</span> {status.reviewNotes}
                        </p>
                    ) : null}
                    <p>
                        <span className="font-semibold">Previous resubmissions:</span> {status.resubmissionCount}
                    </p>
                </div>
            ) : null}

            <form className="space-y-4" onSubmit={handleSubmit}>
                <label className="block space-y-2">
                    <span className="text-sm font-semibold text-slate-700">Student card image</span>
                    <input
                        type="file"
                        accept="image/*"
                        onChange={(event) => setStudentCard(event.target.files?.[0] ?? null)}
                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition file:mr-4 file:rounded-full file:border-0 file:bg-blue-600 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-white hover:file:bg-blue-700"
                    />
                </label>

                <label className="block space-y-2">
                    <span className="text-sm font-semibold text-slate-700">National ID image</span>
                    <input
                        type="file"
                        accept="image/*"
                        onChange={(event) => setNationalId(event.target.files?.[0] ?? null)}
                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition file:mr-4 file:rounded-full file:border-0 file:bg-blue-600 file:px-4 file:py-2 file:text-sm file:font-semibold file:text-white hover:file:bg-blue-700"
                    />
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
                        ? "After resubmission, your account will return to the pending review queue until an administrator reviews the corrected documents."
                        : "After upload, your account will move into the pending approval queue until an administrator reviews and activates it."}
                </div>

                <button
                    type="submit"
                    disabled={!canSubmit}
                    className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#0f5bcf_0%,#1d78ec_100%)] px-5 py-3.5 text-sm font-bold text-white shadow-[0_16px_36px_rgba(29,120,236,0.28)] transition hover:translate-y-[-1px] hover:shadow-[0_20px_44px_rgba(29,120,236,0.32)] disabled:cursor-not-allowed disabled:opacity-60 disabled:shadow-none"
                >
                    <span>{submitting ? "Uploading..." : isRejectedSession ? "Resubmit documents" : "Upload documents"}</span>
                    <span className="material-symbols-outlined text-base">cloud_upload</span>
                </button>

                {success ? (
                    <Link
                        to="/login"
                        className="inline-flex w-full items-center justify-center gap-2 rounded-2xl border border-slate-300 bg-white px-5 py-3 text-sm font-bold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50"
                    >
                        <span>Return to login</span>
                        <span className="material-symbols-outlined text-base">arrow_forward</span>
                    </Link>
                ) : null}
            </form>
        </AuthShell>
    );
}
