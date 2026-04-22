import { useState } from "react";
import { useTranslation } from "react-i18next";
import { changePassword } from "../../../api/authApi";
import { getApiErrorMessage } from "../../../api/apiError";
import ErrorState from "../../../components/ui/ErrorState";
import FormSection from "../../../components/ui/FormSection";
import PageHeader from "../../../components/ui/PageHeader";
import { darkActionButtonClass, darkActionButtonStyle } from "../../../components/ui/buttonStyles";

export default function ChangePasswordPage() {
    const { t } = useTranslation(["auth", "validation"]);
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
            setError(t("validation:validation.auth.passwordMinLength", { count: 6 }));
            return;
        }

        if (newPassword !== confirmPassword) {
            setError(t("validation:validation.auth.passwordConfirmationMismatch"));
            return;
        }

        setSubmitting(true);
        try {
            const response = await changePassword(currentPassword, newPassword);
            if (!response.success) {
                setError(response.message || t("auth:auth.changePassword.errors.failed"));
                return;
            }

            setSuccess(response.message);
            setCurrentPassword("");
            setNewPassword("");
            setConfirmPassword("");
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("auth:auth.changePassword.errors.unavailable")));
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <div className="space-y-6">
            <PageHeader
                eyebrow={t("auth:auth.changePassword.eyebrow")}
                title={t("auth:auth.changePassword.title")}
                description={t("auth:auth.changePassword.description")}
            />

            <div className="grid gap-6 xl:grid-cols-[1fr_320px]">
                <FormSection
                    title={t("auth:auth.changePassword.form.title")}
                    description={t("auth:auth.changePassword.form.description")}
                >
                    {error ? (
                        <ErrorState
                            title={t("auth:auth.changePassword.errors.title")}
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
                        <span className="text-sm font-semibold text-slate-700">{t("auth:auth.changePassword.fields.currentPassword")}</span>
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
                        <span className="text-sm font-semibold text-slate-700">{t("auth:auth.changePassword.fields.newPassword")}</span>
                        <input
                            type="password"
                            value={newPassword}
                            onChange={(event) => setNewPassword(event.target.value)}
                            autoComplete="new-password"
                            className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3.5 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                            required
                        />
                        <p className="text-xs font-medium text-slate-500">
                            {t("auth:auth.changePassword.help.password")}
                        </p>
                    </label>

                    <label className="block space-y-2">
                        <span className="text-sm font-semibold text-slate-700">{t("auth:auth.changePassword.fields.confirmPassword")}</span>
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
                        <span className="text-white" style={darkActionButtonStyle}>{submitting ? t("auth:auth.changePassword.buttons.submitting") : t("auth:auth.changePassword.buttons.submit")}</span>
                        <span className="material-symbols-outlined text-base text-white" style={darkActionButtonStyle}>password</span>
                    </button>
                </form>
                </FormSection>

                <div className="space-y-6">
                    <FormSection
                        title={t("auth:auth.changePassword.guidance.title")}
                        description={t("auth:auth.changePassword.guidance.description")}
                    >
                        <div className="rounded-2xl border border-slate-200 bg-slate-50 p-5 text-sm leading-6 text-slate-600">
                            <p>
                                {t("auth:auth.changePassword.guidance.accountScope")}
                            </p>
                        </div>
                        <div className="rounded-2xl border border-slate-200 bg-slate-50 p-5 text-sm leading-6 text-slate-600">
                            <p>
                                {t("auth:auth.changePassword.guidance.afterSuccess")}
                            </p>
                        </div>
                    </FormSection>
                </div>
            </div>
        </div>
    );
}
