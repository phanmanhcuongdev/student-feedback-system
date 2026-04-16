import { useState } from "react";
import { changePassword } from "../../../api/authApi";
import { getApiErrorMessage } from "../../../api/apiError";
import ErrorState from "../../../components/ui/ErrorState";
import FormSection from "../../../components/ui/FormSection";
import PageHeader from "../../../components/ui/PageHeader";
import { darkActionButtonClass, darkActionButtonStyle } from "../../../components/ui/buttonStyles";

export default function ChangePasswordPage() {
    const [currentPassword, setCurrentPassword] = useState("");
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [submitting, setSubmitting] = useState(false);

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setError("");
        setSuccess("");

        if (newPassword.length < 6) {
            setError("New password must be at least 6 characters.");
            return;
        }

        if (newPassword !== confirmPassword) {
            setError("New password and confirmation do not match.");
            return;
        }

        setSubmitting(true);
        try {
            const response = await changePassword(currentPassword, newPassword);
            if (!response.success) {
                setError(response.message || "Unable to change password.");
                return;
            }

            setSuccess(response.message);
            setCurrentPassword("");
            setNewPassword("");
            setConfirmPassword("");
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to change password right now."));
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <div className="space-y-6">
            <PageHeader
                eyebrow="Security"
                title="Password security"
                description="Change your password from this self-service security area. Use a unique password that you do not reuse for other systems."
            />

            <div className="grid gap-6 xl:grid-cols-[1fr_320px]">
                <FormSection
                    title="Change password"
                    description="Enter your current password and choose a new password for this account."
                >
                    {error ? (
                        <ErrorState
                            title="Password update failed"
                            description={error}
                        />
                    ) : null}

                    {success ? (
                        <div className="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
                            {success}
                        </div>
                    ) : null}

                <form className="space-y-5" onSubmit={handleSubmit}>
                    <label className="block space-y-2">
                        <span className="text-sm font-semibold text-slate-700">Current password</span>
                        <input
                            type="password"
                            value={currentPassword}
                            onChange={(event) => setCurrentPassword(event.target.value)}
                            autoComplete="current-password"
                            className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3.5 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                            required
                        />
                    </label>

                    <label className="block space-y-2">
                        <span className="text-sm font-semibold text-slate-700">New password</span>
                        <input
                            type="password"
                            value={newPassword}
                            onChange={(event) => setNewPassword(event.target.value)}
                            autoComplete="new-password"
                            className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3.5 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                            required
                        />
                        <p className="text-xs font-medium text-slate-500">
                            Use at least 6 characters. Prefer a unique password not used in other systems.
                        </p>
                    </label>

                    <label className="block space-y-2">
                        <span className="text-sm font-semibold text-slate-700">Confirm new password</span>
                        <input
                            type="password"
                            value={confirmPassword}
                            onChange={(event) => setConfirmPassword(event.target.value)}
                            autoComplete="new-password"
                            className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3.5 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                            required
                        />
                    </label>

                    <button
                        type="submit"
                        disabled={submitting}
                        className={`${darkActionButtonClass} w-full px-5 py-3.5 text-sm font-bold`}
                        style={darkActionButtonStyle}
                    >
                        <span className="text-white" style={darkActionButtonStyle}>{submitting ? "Updating..." : "Change password"}</span>
                        <span className="material-symbols-outlined text-base text-white" style={darkActionButtonStyle}>password</span>
                    </button>
                </form>
                </FormSection>

                <div className="space-y-6">
                    <FormSection
                        title="Security guidance"
                        description="Keep this account separate from administrative management of other users."
                    >
                        <div className="rounded-2xl border border-slate-200 bg-slate-50 p-5 text-sm leading-6 text-slate-600">
                            <p>
                                This area is for your own authenticated account only. Managing other people&apos;s accounts remains part of Admin Users and is intentionally separated from personal self-service.
                            </p>
                        </div>
                        <div className="rounded-2xl border border-slate-200 bg-slate-50 p-5 text-sm leading-6 text-slate-600">
                            <p>
                                After a successful password change, continue using this account area for self-service security tasks rather than operational navigation.
                            </p>
                        </div>
                    </FormSection>
                </div>
            </div>
        </div>
    );
}
