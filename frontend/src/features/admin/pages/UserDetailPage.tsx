import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { useTranslation } from "react-i18next";
import { activateUser, deactivateUser, getUserDetail, updateUser } from "../../../api/adminApi";
import { getApiErrorMessage } from "../../../api/apiError";
import type { ManagedUserDetail } from "../../../types/admin";

export default function UserDetailPage() {
    const { t } = useTranslation(["admin"]);
    const { id } = useParams();
    const userId = Number(id);
    const [user, setUser] = useState<ManagedUserDetail | null>(null);
    const [email, setEmail] = useState("");
    const [name, setName] = useState("");
    const [departmentId, setDepartmentId] = useState("");
    const [studentCode, setStudentCode] = useState("");
    const [lecturerCode, setLecturerCode] = useState("");
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [toggling, setToggling] = useState(false);
    const [error, setError] = useState("");
    const [feedback, setFeedback] = useState("");

    useEffect(() => {
        async function loadUser() {
            try {
                setLoading(true);
                setError("");
                const detail = await getUserDetail(userId);
                setUser(detail);
                setEmail(detail.email);
                setName(detail.name);
                setDepartmentId(detail.departmentId != null ? String(detail.departmentId) : "");
                setStudentCode(detail.studentCode ?? "");
                setLecturerCode(detail.lecturerCode ?? "");
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, t("admin:admin.users.detail.errors.load")));
            } finally {
                setLoading(false);
            }
        }

        if (Number.isFinite(userId)) {
            loadUser();
        } else {
            setLoading(false);
            setError(t("admin:admin.users.detail.errors.invalidId"));
        }
    }, [userId, t]);

    async function handleSave(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        if (!user) {
            return;
        }

        try {
            setSaving(true);
            setError("");
            setFeedback("");
            const response = await updateUser(user.id, {
                email: email.trim(),
                name: name.trim(),
                departmentId: departmentId ? Number(departmentId) : null,
                studentCode: user.role === "STUDENT" ? studentCode.trim() : null,
                lecturerCode: user.role === "LECTURER" ? lecturerCode.trim() : null,
            });

            if (!response.success) {
                setError(response.message || t("admin:admin.users.detail.errors.update"));
                return;
            }

            setFeedback(response.message);
            const detail = await getUserDetail(user.id);
            setUser(detail);
            setDepartmentId(detail.departmentId != null ? String(detail.departmentId) : "");
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.users.detail.errors.update")));
        } finally {
            setSaving(false);
        }
    }

    async function handleToggleActive() {
        if (!user) {
            return;
        }

        try {
            setToggling(true);
            setError("");
            setFeedback("");
            const response = user.active ? await deactivateUser(user.id) : await activateUser(user.id);
            if (!response.success) {
                setError(response.message || t("admin:admin.users.errors.updateState"));
                return;
            }

            setFeedback(response.message);
            const detail = await getUserDetail(user.id);
            setUser(detail);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.users.errors.updateState")));
        } finally {
            setToggling(false);
        }
    }

    return (
        <main className="bg-[linear-gradient(180deg,#f7f8fb_0%,#eef1f5_44%,#fafbfd_100%)]">
            <div className="mx-auto max-w-screen-lg px-6 py-10">
                    <div className="mb-8 flex items-center justify-between gap-4">
                        <div>
                            <span className="mb-3 inline-flex rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-slate-700">
                                {t("admin:admin.users.detail.header.eyebrow")}
                            </span>
                            <h1 className="text-4xl font-extrabold tracking-tight text-slate-950">
                                {t("admin:admin.users.detail.header.title")}
                            </h1>
                        </div>
                        <Link
                            to="/admin/users"
                            className="rounded-full border border-slate-200 bg-white px-4 py-2 text-sm font-bold text-slate-700 transition hover:border-slate-300 hover:bg-slate-50"
                        >
                            {t("admin:admin.users.detail.buttons.backToUsers")}
                        </Link>
                    </div>

                    {error ? (
                        <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                            {error}
                        </div>
                    ) : null}

                    {feedback ? (
                        <div className="mb-6 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
                            {feedback}
                        </div>
                    ) : null}

                    {loading ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-10 text-sm font-medium text-slate-500 shadow-sm">
                            {t("admin:admin.users.detail.loading")}
                        </div>
                    ) : !user ? null : (
                        <div className="grid gap-6 lg:grid-cols-[0.95fr_1.05fr]">
                            <section className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                                <div className="flex flex-wrap items-center gap-2">
                                    <span className="rounded-full border border-sky-200 bg-sky-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-sky-700">
                                        {user.role}
                                    </span>
                                    <span className={`rounded-full border px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] ${user.active ? "border-emerald-200 bg-emerald-50 text-emerald-700" : "border-red-200 bg-red-50 text-red-700"}`}>
                                        {user.active ? t("admin:admin.users.filters.active") : t("admin:admin.users.filters.inactive")}
                                    </span>
                                    {user.studentStatus ? (
                                        <span className="rounded-full border border-amber-200 bg-amber-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-amber-700">
                                            {user.studentStatus}
                                        </span>
                                    ) : null}
                                </div>

                                <h2 className="mt-5 text-3xl font-extrabold tracking-tight text-slate-950">
                                    {user.name}
                                </h2>
                                <p className="mt-2 text-sm text-slate-500">{user.email}</p>

                                <div className="mt-6 grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                    <div className="flex items-center justify-between gap-4">
                                        <span className="font-semibold text-slate-500">{t("admin:admin.users.detail.fields.userId")}</span>
                                        <span className="font-medium text-slate-900">{user.id}</span>
                                    </div>
                                    <div className="flex items-center justify-between gap-4">
                                        <span className="font-semibold text-slate-500">{t("admin:admin.users.table.department")}</span>
                                        <span className="font-medium text-slate-900">{user.departmentName ?? t("admin:admin.users.common.notAvailable")}</span>
                                    </div>
                                    <div className="flex items-center justify-between gap-4">
                                        <span className="font-semibold text-slate-500">{t("admin:admin.users.detail.fields.studentCode")}</span>
                                        <span className="font-medium text-slate-900">{user.studentCode ?? t("admin:admin.users.common.notAvailable")}</span>
                                    </div>
                                    <div className="flex items-center justify-between gap-4">
                                        <span className="font-semibold text-slate-500">{t("admin:admin.users.detail.fields.lecturerCode")}</span>
                                        <span className="font-medium text-slate-900">{user.lecturerCode ?? t("admin:admin.users.common.notAvailable")}</span>
                                    </div>
                                </div>

                                <button
                                    type="button"
                                    onClick={handleToggleActive}
                                    disabled={toggling}
                                    className={`mt-6 inline-flex w-full items-center justify-center gap-2 rounded-2xl px-4 py-3 text-sm font-bold transition disabled:cursor-not-allowed disabled:opacity-60 ${user.active ? "border border-red-200 bg-red-50 text-red-700 hover:border-red-300 hover:bg-red-100" : "bg-[linear-gradient(135deg,#0f8f55_0%,#22c55e_100%)] text-white shadow-[0_16px_36px_rgba(34,197,94,0.22)] hover:translate-y-[-1px] hover:shadow-[0_18px_40px_rgba(34,197,94,0.28)]"}`}
                                >
                                    <span>{toggling ? t("admin:admin.users.buttons.updating") : user.active ? t("admin:admin.users.buttons.deactivateUser") : t("admin:admin.users.buttons.activateUser")}</span>
                                    <span className="material-symbols-outlined text-[18px]">
                                        {user.active ? "block" : "check_circle"}
                                    </span>
                                </button>
                            </section>

                            <section className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                                <h2 className="text-2xl font-bold text-slate-950">{t("admin:admin.users.detail.edit.title")}</h2>
                                <p className="mt-2 text-sm text-slate-500">
                                    {t("admin:admin.users.detail.edit.description")}
                                </p>

                                <form className="mt-6 space-y-5" onSubmit={handleSave}>
                                    <label className="block space-y-2">
                                        <span className="text-sm font-semibold text-slate-700">{t("admin:admin.users.detail.fields.email")}</span>
                                        <input
                                            type="email"
                                            value={email}
                                            onChange={(event) => setEmail(event.target.value)}
                                            className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                                        />
                                    </label>

                                    <label className="block space-y-2">
                                        <span className="text-sm font-semibold text-slate-700">{t("admin:admin.users.detail.fields.name")}</span>
                                        <input
                                            type="text"
                                            value={name}
                                            onChange={(event) => setName(event.target.value)}
                                            className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                                        />
                                    </label>

                                    {user.role !== "ADMIN" ? (
                                        <label className="block space-y-2">
                                            <span className="text-sm font-semibold text-slate-700">{t("admin:admin.users.detail.fields.departmentId")}</span>
                                            <input
                                                type="number"
                                                value={departmentId}
                                                onChange={(event) => setDepartmentId(event.target.value)}
                                                className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                                            />
                                        </label>
                                    ) : null}

                                    {user.role === "STUDENT" ? (
                                        <label className="block space-y-2">
                                            <span className="text-sm font-semibold text-slate-700">{t("admin:admin.users.detail.fields.studentCode")}</span>
                                            <input
                                                type="text"
                                                value={studentCode}
                                                onChange={(event) => setStudentCode(event.target.value)}
                                                className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                                            />
                                        </label>
                                    ) : null}

                                    {user.role === "LECTURER" ? (
                                        <label className="block space-y-2">
                                            <span className="text-sm font-semibold text-slate-700">{t("admin:admin.users.detail.fields.lecturerCode")}</span>
                                            <input
                                                type="text"
                                                value={lecturerCode}
                                                onChange={(event) => setLecturerCode(event.target.value)}
                                                className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                                            />
                                        </label>
                                    ) : null}

                                    <button
                                        type="submit"
                                        disabled={saving}
                                        className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#0f5bcf_0%,#1d78ec_100%)] px-5 py-3 text-sm font-bold text-white shadow-[0_16px_36px_rgba(29,120,236,0.24)] transition hover:translate-y-[-1px] hover:shadow-[0_18px_40px_rgba(29,120,236,0.3)] disabled:cursor-not-allowed disabled:opacity-60 disabled:shadow-none"
                                    >
                                        <span>{saving ? t("admin:admin.surveys.form.buttons.saving") : t("admin:admin.users.detail.buttons.saveChanges")}</span>
                                        <span className="material-symbols-outlined text-[18px]">save</span>
                                    </button>
                                </form>
                            </section>
                        </div>
                    )}
            </div>
        </main>
    );
}
