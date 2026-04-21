import { useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { forgotPassword } from "../../../api/authApi";
import { getApiErrorMessage } from "../../../api/apiError";
import AuthShell from "../components/AuthShell";

export default function ForgotPasswordPage() {
    const { t } = useTranslation(["auth"]);
    const [email, setEmail] = useState("");
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [submitting, setSubmitting] = useState(false);

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setSubmitting(true);
        setError("");
        setSuccess("");

        try {
            const response = await forgotPassword(email.trim());
            if (!response.success) {
                setError(response.message || t("auth:auth.forgotPassword.errors.failed"));
                return;
            }

            setSuccess(response.message);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("auth:auth.forgotPassword.errors.unavailable")));
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <AuthShell
            eyebrow={t("auth:auth.forgotPassword.eyebrow")}
            title={t("auth:auth.forgotPassword.title")}
            description={t("auth:auth.forgotPassword.description")}
            footer={(
                <p className="text-sm text-slate-500">
                    {t("auth:auth.forgotPassword.footer.backTo")}
                    {" "}
                    <Link className="font-semibold text-blue-700 hover:text-blue-800" to="/login">
                        {t("auth:auth.forgotPassword.links.login")}
                    </Link>
                </p>
            )}
        >
            <form className="space-y-5" onSubmit={handleSubmit}>
                <label className="block space-y-2">
                    <span className="text-sm font-semibold text-slate-700">{t("auth:auth.forgotPassword.fields.email")}</span>
                    <input
                        type="email"
                        value={email}
                        onChange={(event) => setEmail(event.target.value)}
                        placeholder={t("auth:auth.forgotPassword.placeholders.email")}
                        autoComplete="email"
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
                    <span>{submitting ? t("auth:auth.forgotPassword.buttons.submitting") : t("auth:auth.forgotPassword.buttons.submit")}</span>
                    <span className="material-symbols-outlined text-base">mail</span>
                </button>
            </form>
        </AuthShell>
    );
}
