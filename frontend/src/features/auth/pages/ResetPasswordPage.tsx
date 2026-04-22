import { useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getApiErrorMessage } from "../../../api/apiError";
import { resetPassword } from "../../../api/authApi";
import AuthShell from "../components/AuthShell";

export default function ResetPasswordPage() {
    const { t } = useTranslation(["auth", "validation"]);
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token") ?? "";
    const [newPassword, setNewPassword] = useState("");
    const [confirmPassword, setConfirmPassword] = useState("");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [submitting, setSubmitting] = useState(false);

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setError("");
        setSuccess("");

        if (!token) {
            setError(t("validation:validation.auth.resetTokenMissing"));
            return;
        }

        if (newPassword.length < 6) {
            setError(t("validation:validation.auth.passwordMinLength", { count: 6 }));
            return;
        }

        if (newPassword !== confirmPassword) {
            setError(t("validation:validation.auth.passwordMismatch"));
            return;
        }

        setSubmitting(true);
        try {
            const response = await resetPassword(token, newPassword);
            if (!response.success) {
                setError(response.message || t("auth:auth.resetPassword.errors.failed"));
                return;
            }

            setSuccess(response.message);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("auth:auth.resetPassword.errors.unavailable")));
        } finally {
            setSubmitting(false);
        }
    }

    function goToLogin() {
        navigate("/login", {
            replace: true,
            state: { notice: t("auth:auth.resetPassword.successNotice") },
        });
    }

    return (
        <AuthShell
            eyebrow={t("auth:auth.resetPassword.eyebrow")}
            title={t("auth:auth.resetPassword.title")}
            description={t("auth:auth.resetPassword.description")}
            footer={(
                <p className="text-sm text-slate-500">
                    {t("auth:auth.resetPassword.footer.backTo")}
                    {" "}
                    <Link className="font-semibold text-blue-700 hover:text-blue-800" to="/login">
                        {t("auth:auth.resetPassword.links.login")}
                    </Link>
                </p>
            )}
        >
            <form className="space-y-5" onSubmit={handleSubmit}>
                <label className="block space-y-2">
                    <span className="text-sm font-semibold text-slate-700">{t("auth:auth.resetPassword.fields.newPassword")}</span>
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
                    <span className="text-sm font-semibold text-slate-700">{t("auth:auth.resetPassword.fields.confirmPassword")}</span>
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
                    <span>{submitting ? t("auth:auth.resetPassword.buttons.submitting") : t("auth:auth.resetPassword.buttons.submit")}</span>
                    <span className="material-symbols-outlined text-base">lock_reset</span>
                </button>

                {success ? (
                    <button
                        type="button"
                        onClick={goToLogin}
                        className="inline-flex w-full items-center justify-center gap-2 rounded-2xl border border-blue-200 bg-blue-50 px-5 py-3 text-sm font-bold text-blue-700 transition hover:border-blue-300 hover:bg-blue-100"
                    >
                        <span>{t("auth:auth.resetPassword.buttons.continueToLogin")}</span>
                        <span className="material-symbols-outlined text-base">arrow_forward</span>
                    </button>
                ) : null}
            </form>
        </AuthShell>
    );
}
