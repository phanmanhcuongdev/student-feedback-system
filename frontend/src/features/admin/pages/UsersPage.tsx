import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getUsers } from "../../../api/adminApi";
import { getApiErrorMessage } from "../../../api/apiError";
import MainFooter from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
import type { ManagedUserSummary } from "../../../types/admin";

export default function UsersPage() {
    const [users, setUsers] = useState<ManagedUserSummary[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        async function loadUsers() {
            try {
                setLoading(true);
                setError("");
                setUsers(await getUsers());
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, "Unable to load users."));
            } finally {
                setLoading(false);
            }
        }

        loadUsers();
    }, []);

    return (
        <>
            <MainHeader />
            <main className="min-h-screen bg-[linear-gradient(180deg,#f7f8fb_0%,#eef1f5_44%,#fafbfd_100%)]">
                <div className="mx-auto max-w-screen-xl px-6 py-10">
                    <div className="mb-10 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
                        <div className="max-w-3xl">
                            <span className="mb-3 inline-flex rounded-full border border-slate-200 bg-slate-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-slate-700">
                                Admin / Users
                            </span>
                            <h1 className="text-4xl font-extrabold tracking-tight text-slate-950">
                                User management
                            </h1>
                            <p className="mt-4 text-base leading-7 text-slate-500">
                                Review all accounts, open user details, and update basic profile information safely.
                            </p>
                        </div>
                        <div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm">
                            {users.length} user{users.length === 1 ? "" : "s"}
                        </div>
                    </div>

                    {error ? (
                        <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                            {error}
                        </div>
                    ) : null}

                    {loading ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-10 text-sm font-medium text-slate-500 shadow-sm">
                            Loading users...
                        </div>
                    ) : (
                        <div className="grid gap-6 lg:grid-cols-2">
                            {users.map((user) => (
                                <article
                                    key={user.id}
                                    className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]"
                                >
                                    <div className="mb-5 flex items-start justify-between gap-4">
                                        <div>
                                            <div className="flex flex-wrap items-center gap-2">
                                                <span className="rounded-full border border-sky-200 bg-sky-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-sky-700">
                                                    {user.role}
                                                </span>
                                                <span className={`rounded-full border px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] ${user.active ? "border-emerald-200 bg-emerald-50 text-emerald-700" : "border-red-200 bg-red-50 text-red-700"}`}>
                                                    {user.active ? "Active" : "Inactive"}
                                                </span>
                                            </div>
                                            <h2 className="mt-3 text-2xl font-bold text-slate-950">{user.name}</h2>
                                            <p className="mt-1 text-sm text-slate-500">{user.email}</p>
                                        </div>
                                        <div className="rounded-2xl bg-slate-100 px-3 py-2 text-xs font-semibold uppercase tracking-[0.16em] text-slate-500">
                                            ID {user.id}
                                        </div>
                                    </div>

                                    <div className="grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                        <div className="flex items-center justify-between gap-4">
                                            <span className="font-semibold text-slate-500">Department</span>
                                            <span className="text-right font-medium text-slate-900">{user.departmentName ?? "N/A"}</span>
                                        </div>
                                        <div className="flex items-center justify-between gap-4">
                                            <span className="font-semibold text-slate-500">Student status</span>
                                            <span className="text-right font-medium text-slate-900">{user.studentStatus ?? "N/A"}</span>
                                        </div>
                                    </div>

                                    <div className="mt-6">
                                        <Link
                                            to={`/admin/users/${user.id}`}
                                            className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#0f5bcf_0%,#1d78ec_100%)] px-4 py-3 text-sm font-bold text-white shadow-[0_16px_36px_rgba(29,120,236,0.24)] transition hover:translate-y-[-1px] hover:shadow-[0_18px_40px_rgba(29,120,236,0.3)]"
                                        >
                                            <span>Manage user</span>
                                            <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
                                        </Link>
                                    </div>
                                </article>
                            ))}
                        </div>
                    )}
                </div>
            </main>
            <MainFooter />
        </>
    );
}
