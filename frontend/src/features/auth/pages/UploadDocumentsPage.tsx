import { useMemo, useState } from "react";
import { Link, Navigate, useNavigate } from "react-router-dom";
import { getApiErrorMessage } from "../../../api/apiError";
import { uploadDocuments } from "../../../api/authApi";
import AuthShell from "../components/AuthShell";
import { useAuth } from "../useAuth";

export default function UploadDocumentsPage() {
    const navigate = useNavigate();
    const { session, logout } = useAuth();
    const [studentCard, setStudentCard] = useState<File | null>(null);
    const [nationalId, setNationalId] = useState<File | null>(null);
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const canSubmit = useMemo(
        () => studentCard !== null && nationalId !== null && !submitting,
        [nationalId, studentCard, submitting]
    );

    if (!session) {
        return <Navigate to="/login" replace />;
    }

    if (session.role !== "STUDENT") {
        return <Navigate to="/" replace />;
    }

    if (session.studentStatus !== "EMAIL_VERIFIED") {
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
                    notice: "Documents uploaded successfully. Your account is now pending administrator approval.",
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
            eyebrow="Document Upload"
            title="Upload required verification documents"
            description="Submit both document images so your student account can move into the approval queue."
            footer={
                <p className="text-sm text-slate-500">
                    Signed in as
                    {" "}
                    <span className="font-semibold text-blue-700">{session.email}</span>
                </p>
            }
        >
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
                    After upload, your account will move into the pending approval queue until an administrator reviews and activates it.
                </div>

                <button
                    type="submit"
                    disabled={!canSubmit}
                    className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#0f5bcf_0%,#1d78ec_100%)] px-5 py-3.5 text-sm font-bold text-white shadow-[0_16px_36px_rgba(29,120,236,0.28)] transition hover:translate-y-[-1px] hover:shadow-[0_20px_44px_rgba(29,120,236,0.32)] disabled:cursor-not-allowed disabled:opacity-60 disabled:shadow-none"
                >
                    <span>{submitting ? "Uploading..." : "Upload documents"}</span>
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
