import { useState } from "react";
import Footer from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
import { changePassword } from "../../../api/authApi";
import { getApiErrorMessage } from "../../../api/apiError";

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
        <>
            <MainHeader />
            <main className="min-h-screen bg-[linear-gradient(180deg,#f4f8ff_0%,#eef3f8_44%,#f7fafc_100%)]">
                <div className="mx-auto max-w-2xl px-6 py-10">
                    <div className="rounded-[28px] border border-slate-200 bg-white p-8 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                        <div className="mb-8">
                            <span className="mb-3 inline-flex rounded-full border border-blue-200 bg-blue-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-blue-700">
                                Account Security
                            </span>
                            <h1 className="text-3xl font-extrabold tracking-tight text-slate-950">
                                Change password
                            </h1>
                            <p className="mt-3 text-sm leading-6 text-slate-500">
                                Enter your current password and choose a new password for this account.
                            </p>
                        </div>

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

                            <button
                                type="submit"
                                disabled={submitting}
                                className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#0f5bcf_0%,#1d78ec_100%)] px-5 py-3.5 text-sm font-bold text-white shadow-[0_16px_36px_rgba(29,120,236,0.28)] transition hover:translate-y-[-1px] hover:shadow-[0_20px_44px_rgba(29,120,236,0.32)] disabled:cursor-not-allowed disabled:opacity-65 disabled:shadow-none"
                            >
                                <span>{submitting ? "Updating..." : "Change password"}</span>
                                <span className="material-symbols-outlined text-base">password</span>
                            </button>
                        </form>
                    </div>
                </div>
            </main>
            <Footer />
        </>
    );
}
