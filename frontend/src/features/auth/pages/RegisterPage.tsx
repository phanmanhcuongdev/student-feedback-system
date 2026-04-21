import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { getApiErrorMessage } from "../../../api/apiError";
import { registerStudent } from "../../../api/authApi";
import type { RegisterStudentRequest } from "../../../types/auth";
import AuthShell from "../components/AuthShell";

export default function RegisterPage() {
    const { t } = useTranslation(["auth", "validation"]);
    const navigate = useNavigate();
    const [form, setForm] = useState<RegisterStudentRequest>({
        name: "",
        email: "",
        password: "",
        studentCode: "",
        departmentName: "",
    });
    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [submitting, setSubmitting] = useState(false);

    function updateField<K extends keyof RegisterStudentRequest>(field: K, value: RegisterStudentRequest[K]) {
        setForm((prev) => ({
            ...prev,
            [field]: value,
        }));
    }

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setSubmitting(true);
        setError("");
        setSuccess("");

        try {
            const response = await registerStudent(form);

            if (!response.success) {
                setError(response.message || t("auth:auth.register.errors.failed"));
                return;
            }

            setSuccess(response.message);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("auth:auth.register.errors.unavailable")));
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <AuthShell
            eyebrow={t("auth:auth.register.eyebrow")}
            title={t("auth:auth.register.title")}
            description={t("auth:auth.register.description")}
            footer={
                <p className="text-sm text-slate-500">
                    {t("auth:auth.register.footer.alreadyRegistered")}
                    {" "}
                    <Link className="font-semibold text-blue-700 hover:text-blue-800" to="/login">
                        {t("auth:auth.register.links.backToLogin")}
                    </Link>
                </p>
            }
        >
            <form className="space-y-4" onSubmit={handleSubmit}>
                <label className="block space-y-2">
                    <span className="text-sm font-semibold text-slate-700">{t("auth:auth.register.fields.fullName")}</span>
                    <input
                        type="text"
                        value={form.name}
                        onChange={(event) => updateField("name", event.target.value)}
                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                        required
                    />
                </label>
                <label className="block space-y-2">
                    <span className="text-sm font-semibold text-slate-700">{t("auth:auth.register.fields.email")}</span>
                    <input
                        type="email"
                        value={form.email}
                        onChange={(event) => updateField("email", event.target.value)}
                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                        required
                    />
                </label>
                <label className="block space-y-2">
                    <span className="text-sm font-semibold text-slate-700">{t("auth:auth.register.fields.password")}</span>
                    <input
                        type="password"
                        value={form.password}
                        onChange={(event) => updateField("password", event.target.value)}
                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                        required
                    />
                </label>
                <label className="block space-y-2">
                    <span className="text-sm font-semibold text-slate-700">{t("auth:auth.register.fields.studentCode")}</span>
                    <input
                        type="text"
                        value={form.studentCode}
                        onChange={(event) => updateField("studentCode", event.target.value)}
                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                        required
                    />
                </label>
                <label className="block space-y-2">
                    <span className="text-sm font-semibold text-slate-700">{t("auth:auth.register.fields.departmentName")}</span>
                    <input
                        type="text"
                        value={form.departmentName}
                        onChange={(event) => updateField("departmentName", event.target.value)}
                        placeholder={t("auth:auth.register.placeholders.departmentName")}
                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
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
                    <span>{submitting ? t("auth:auth.register.buttons.submitting") : t("auth:auth.register.buttons.submit")}</span>
                    <span className="material-symbols-outlined text-base">person_add</span>
                </button>

                <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-500">
                    {t("auth:auth.register.emailVerificationNotice")}
                </div>

                {success ? (
                    <button
                        type="button"
                        onClick={() => navigate("/login")}
                        className="inline-flex w-full items-center justify-center gap-2 rounded-2xl border border-blue-200 bg-blue-50 px-5 py-3 text-sm font-bold text-blue-700 transition hover:border-blue-300 hover:bg-blue-100"
                    >
                        <span>{t("auth:auth.register.links.backToLogin")}</span>
                        <span className="material-symbols-outlined text-base">arrow_forward</span>
                    </button>
                ) : null}
            </form>
        </AuthShell>
    );
}
