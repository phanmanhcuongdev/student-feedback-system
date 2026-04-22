import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { useTranslation } from "react-i18next";
import {
    activateUser,
    deactivateUser,
    getUserManagementDepartments,
    getUsers,
} from "../../../api/adminApi";
import { getApiErrorMessage } from "../../../api/apiError";
import DataTable, { type DataTableColumn } from "../../../components/data-view/DataTable";
import DataToolbar from "../../../components/data-view/DataToolbar";
import PaginationControls from "../../../components/data-view/PaginationControls";
import ResponsiveDataList from "../../../components/data-view/ResponsiveDataList";
import SearchInput from "../../../components/data-view/SearchInput";
import SelectFilter from "../../../components/data-view/SelectFilter";
import EmptyState from "../../../components/ui/EmptyState";
import ErrorState from "../../../components/ui/ErrorState";
import LoadingState from "../../../components/ui/LoadingState";
import PageHeader from "../../../components/ui/PageHeader";
import RoleBadge from "../../../components/ui/RoleBadge";
import StatCard from "../../../components/ui/StatCard";
import StatusBadge from "../../../components/ui/StatusBadge";
import type { DepartmentOption, ManagedUserMetrics, ManagedUserSummary } from "../../../types/admin";

const ROLE_TABS = [
    { labelKey: "admin:admin.users.roleTabs.all", value: "ALL" },
    { labelKey: "admin:admin.users.roleTabs.students", value: "STUDENT" },
    { labelKey: "admin:admin.users.roleTabs.lecturers", value: "LECTURER" },
    { labelKey: "admin:admin.users.roleTabs.admins", value: "ADMIN" },
] as const;

export default function UsersPage() {
    const { t } = useTranslation(["admin"]);
    const [users, setUsers] = useState<ManagedUserSummary[]>([]);
    const [metrics, setMetrics] = useState<ManagedUserMetrics>({
        totalUsers: 0,
        totalStudents: 0,
        totalLecturers: 0,
        totalAdmins: 0,
        totalInactive: 0,
        totalPending: 0,
    });
    const [departments, setDepartments] = useState<DepartmentOption[]>([]);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [query, setQuery] = useState("");
    const [debouncedQuery, setDebouncedQuery] = useState("");
    const [roleTab, setRoleTab] = useState("ALL");
    const [statusFilter, setStatusFilter] = useState("ALL");
    const [studentStatusFilter, setStudentStatusFilter] = useState("ALL");
    const [departmentFilter, setDepartmentFilter] = useState("ALL");
    const [sortBy, setSortBy] = useState("name");
    const [sortDir, setSortDir] = useState("asc");
    const [page, setPage] = useState(0);
    const [actionUserId, setActionUserId] = useState<number | null>(null);

    const pageSize = 20;

    const loadUsers = useCallback(async () => {
        try {
            setLoading(true);
            setError("");
            const usersResponse = await getUsers({
                role: roleTab === "ALL" ? undefined : roleTab,
                keyword: debouncedQuery || undefined,
                active: statusFilter === "ALL" ? undefined : statusFilter === "ACTIVE",
                studentStatus:
                    roleTab === "LECTURER" || roleTab === "ADMIN"
                        ? undefined
                        : studentStatusFilter === "ALL"
                            ? undefined
                            : studentStatusFilter,
                departmentId: departmentFilter === "ALL" ? undefined : Number(departmentFilter),
                page,
                size: pageSize,
                sortBy,
                sortDir,
            });
            setUsers(usersResponse.items);
            setMetrics(usersResponse.metrics);
            setTotalElements(usersResponse.totalElements);
            setTotalPages(usersResponse.totalPages);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.users.errors.load")));
        } finally {
            setLoading(false);
        }
    }, [debouncedQuery, departmentFilter, page, roleTab, sortBy, sortDir, statusFilter, studentStatusFilter, t]);

    useEffect(() => {
        const timeout = window.setTimeout(() => {
            setDebouncedQuery(query.trim());
        }, 300);

        return () => window.clearTimeout(timeout);
    }, [query]);

    useEffect(() => {
        void loadUsers();
    }, [loadUsers]);

    useEffect(() => {
        async function loadDepartments() {
            try {
                setDepartments(await getUserManagementDepartments());
            } catch {
                setDepartments([]);
            }
        }

        void loadDepartments();
    }, []);

    useEffect(() => {
        setPage(0);
    }, [debouncedQuery, roleTab, statusFilter, studentStatusFilter, departmentFilter, sortBy, sortDir]);

    async function handleToggleActive(user: ManagedUserSummary) {
        try {
            setActionUserId(user.id);
            setError("");
            const response = user.active ? await deactivateUser(user.id) : await activateUser(user.id);
            if (!response.success) {
                setError(response.message || t("admin:admin.users.errors.updateState"));
                return;
            }
            await loadUsers();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.users.errors.updateState")));
        } finally {
            setActionUserId(null);
        }
    }

    const departmentOptions = useMemo(() => [
        { label: t("admin:admin.users.filters.allDepartments"), value: "ALL" },
        ...departments.map((department) => ({ label: department.name, value: String(department.id) })),
    ], [departments, t]);

    const columns: DataTableColumn<ManagedUserSummary>[] = [
        {
            key: "user",
            header: t("admin:admin.users.table.user"),
            render: (user) => (
                <div>
                    <p className="font-bold text-slate-950">{user.name}</p>
                    <p className="mt-1 text-sm text-slate-500">{user.email}</p>
                </div>
            ),
        },
        {
            key: "role",
            header: t("admin:admin.users.table.role"),
            render: (user) => <RoleBadge value={user.role} />,
        },
        {
            key: "code",
            header: t("admin:admin.users.table.code"),
            render: (user) => user.studentCode ?? user.lecturerCode ?? t("admin:admin.users.common.notAvailable"),
        },
        {
            key: "account",
            header: t("admin:admin.users.table.account"),
            render: (user) => <StatusBadge kind="account" value={user.active ? "ACTIVE" : "INACTIVE"} />,
        },
        {
            key: "department",
            header: t("admin:admin.users.table.department"),
            render: (user) => user.departmentName ?? t("admin:admin.users.common.notAvailable"),
        },
        {
            key: "studentStatus",
            header: t("admin:admin.users.table.studentStatus"),
            render: (user) => user.studentStatus ? <StatusBadge kind="onboarding" value={user.studentStatus} /> : t("admin:admin.users.common.notAvailable"),
        },
        {
            key: "actions",
            header: t("admin:admin.users.table.actions"),
            className: "text-right",
            render: (user) => (
                <div className="flex justify-end">
                    <div className="flex items-center gap-2">
                        <button
                            type="button"
                            onClick={() => void handleToggleActive(user)}
                            disabled={actionUserId === user.id}
                            className="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                            <span>{actionUserId === user.id ? t("admin:admin.users.buttons.updating") : user.active ? t("admin:admin.users.buttons.deactivate") : t("admin:admin.users.buttons.activate")}</span>
                        </button>
                        <Link
                            to={`/admin/users/${user.id}`}
                            className="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50"
                        >
                            <span>{t("admin:admin.users.buttons.viewDetails")}</span>
                            <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
                        </Link>
                    </div>
                </div>
            ),
        },
    ];

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-xl px-6 py-10">
                <PageHeader
                    eyebrow={t("admin:admin.users.header.eyebrow")}
                    title={t("admin:admin.users.header.title")}
                    description={t("admin:admin.users.header.description")}
                    actions={(
                        <div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm">
                            {t("admin:admin.users.header.matchingCount", { count: totalElements })}
                        </div>
                    )}
                />

                <div className="mt-6 space-y-6">
                    <div className="grid gap-4 md:grid-cols-3 xl:grid-cols-6">
                        <StatCard label={t("admin:admin.users.stats.totalUsers")} value={metrics.totalUsers} />
                        <StatCard label={t("admin:admin.users.stats.students")} value={metrics.totalStudents} tone="blue" />
                        <StatCard label={t("admin:admin.users.stats.lecturers")} value={metrics.totalLecturers} tone="sky" />
                        <StatCard label={t("admin:admin.users.stats.admins")} value={metrics.totalAdmins} tone="slate" />
                        <StatCard label={t("admin:admin.users.stats.inactive")} value={metrics.totalInactive} tone="amber" />
                        <StatCard label={t("admin:admin.users.stats.pending")} value={metrics.totalPending} tone="amber" />
                    </div>

                    <div className="flex flex-wrap items-center gap-2 rounded-[24px] border border-slate-200 bg-white p-2 shadow-sm">
                        {ROLE_TABS.map((tab) => (
                            <button
                                key={tab.value}
                                type="button"
                                onClick={() => setRoleTab(tab.value)}
                                className={[
                                    "rounded-2xl px-4 py-2.5 text-sm font-semibold transition",
                                    roleTab === tab.value
                                        ? "bg-slate-900 text-white"
                                        : "text-slate-600 hover:bg-slate-100 hover:text-slate-950",
                                ].join(" ")}
                            >
                                {t(tab.labelKey)}
                            </button>
                        ))}
                    </div>

                    <DataToolbar
                        filters={(
                            <>
                                <SearchInput
                                    value={query}
                                    onChange={setQuery}
                                    placeholder={t("admin:admin.users.filters.search")}
                                />
                                <SelectFilter
                                    label={t("admin:admin.users.filters.account")}
                                    value={statusFilter}
                                    onChange={setStatusFilter}
                                    options={[
                                        { label: t("admin:admin.users.filters.allStates"), value: "ALL" },
                                        { label: t("admin:admin.users.filters.active"), value: "ACTIVE" },
                                        { label: t("admin:admin.users.filters.inactive"), value: "INACTIVE" },
                                    ]}
                                />
                                <SelectFilter
                                    label={t("admin:admin.users.filters.department")}
                                    value={departmentFilter}
                                    onChange={setDepartmentFilter}
                                    options={departmentOptions}
                                />
                                {(roleTab === "STUDENT" || roleTab === "ALL") ? (
                                    <SelectFilter
                                        label={t("admin:admin.users.filters.studentStatus")}
                                        value={studentStatusFilter}
                                        onChange={setStudentStatusFilter}
                                        options={[
                                            { label: t("admin:admin.users.filters.allStatuses"), value: "ALL" },
                                            { label: t("admin:admin.users.filters.approved"), value: "ACTIVE" },
                                            { label: t("admin:admin.users.filters.pendingReview"), value: "PENDING" },
                                            { label: t("admin:admin.users.filters.emailVerified"), value: "EMAIL_VERIFIED" },
                                            { label: t("admin:admin.users.filters.emailUnverified"), value: "EMAIL_UNVERIFIED" },
                                            { label: t("admin:admin.users.filters.rejected"), value: "REJECTED" },
                                        ]}
                                    />
                                ) : null}
                                <SelectFilter
                                    label={t("admin:admin.users.filters.sort")}
                                    value={`${sortBy}:${sortDir}`}
                                    onChange={(value) => {
                                        const [nextSortBy, nextSortDir] = value.split(":");
                                        setSortBy(nextSortBy);
                                        setSortDir(nextSortDir);
                                    }}
                                    options={[
                                        { label: t("admin:admin.users.filters.nameAsc"), value: "name:asc" },
                                        { label: t("admin:admin.users.filters.nameDesc"), value: "name:desc" },
                                        { label: t("admin:admin.users.filters.emailAsc"), value: "email:asc" },
                                        { label: t("admin:admin.users.filters.roleAsc"), value: "role:asc" },
                                        { label: t("admin:admin.users.filters.departmentAsc"), value: "department:asc" },
                                        { label: t("admin:admin.users.filters.accountStatus"), value: "active:asc" },
                                        { label: t("admin:admin.users.filters.studentStatus"), value: "status:asc" },
                                    ]}
                                />
                            </>
                        )}
                        actions={(
                            <div className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-semibold text-slate-600">
                                {t("admin:admin.users.rowsOnPage", { count: users.length })}
                            </div>
                        )}
                    />

                    {error ? (
                        <ErrorState description={error} onRetry={() => void loadUsers()} />
                    ) : null}

                    {loading ? (
                        <LoadingState label={t("admin:admin.users.loading")} />
                    ) : users.length === 0 ? (
                        <EmptyState
                            title={t("admin:admin.users.empty.title")}
                            description={t("admin:admin.users.empty.description")}
                            icon="group"
                        />
                    ) : (
                        <>
                            <div className="hidden lg:block">
                                <DataTable
                                    columns={columns}
                                    items={users}
                                    getRowKey={(user) => user.id}
                                />
                            </div>

                            <ResponsiveDataList
                                items={users}
                                getKey={(user) => user.id}
                                renderItem={(user) => (
                                    <article className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
                                        <div className="flex items-start justify-between gap-4">
                                            <div>
                                                <div className="flex flex-wrap items-center gap-2">
                                                    <RoleBadge value={user.role} />
                                                    <StatusBadge kind="account" value={user.active ? "ACTIVE" : "INACTIVE"} />
                                                </div>
                                                <h2 className="mt-3 text-xl font-bold text-slate-950">{user.name}</h2>
                                                <p className="mt-1 text-sm text-slate-500">{user.email}</p>
                                            </div>
                                            <div className="rounded-2xl bg-slate-100 px-3 py-2 text-xs font-semibold uppercase tracking-[0.16em] text-slate-500">
                                                {t("admin:admin.users.card.id", { id: user.id })}
                                            </div>
                                        </div>

                                        <div className="mt-4 grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                            <div className="flex items-center justify-between gap-4">
                                                <span className="font-semibold text-slate-500">{t("admin:admin.users.table.code")}</span>
                                                <span className="text-right font-medium text-slate-900">{user.studentCode ?? user.lecturerCode ?? t("admin:admin.users.common.notAvailable")}</span>
                                            </div>
                                            <div className="flex items-center justify-between gap-4">
                                                <span className="font-semibold text-slate-500">{t("admin:admin.users.table.department")}</span>
                                                <span className="text-right font-medium text-slate-900">{user.departmentName ?? t("admin:admin.users.common.notAvailable")}</span>
                                            </div>
                                            <div className="flex items-center justify-between gap-4">
                                                <span className="font-semibold text-slate-500">{t("admin:admin.users.table.studentStatus")}</span>
                                                <span className="text-right font-medium text-slate-900">
                                                    {user.studentStatus ? user.studentStatus.replace(/_/g, " ").toLowerCase().replace(/^\w/, (letter) => letter.toUpperCase()) : t("admin:admin.users.common.notAvailable")}
                                                </span>
                                            </div>
                                        </div>

                                        <div className="mt-5">
                                            <div className="grid gap-3">
                                                <button
                                                    type="button"
                                                    onClick={() => void handleToggleActive(user)}
                                                    disabled={actionUserId === user.id}
                                                    className="inline-flex w-full items-center justify-center gap-2 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50 disabled:cursor-not-allowed disabled:opacity-60"
                                                >
                                                    <span>{actionUserId === user.id ? t("admin:admin.users.buttons.updating") : user.active ? t("admin:admin.users.buttons.deactivateUser") : t("admin:admin.users.buttons.activateUser")}</span>
                                                </button>
                                                <Link
                                                    to={`/admin/users/${user.id}`}
                                                    className="inline-flex w-full items-center justify-center gap-2 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50"
                                                >
                                                    <span>{t("admin:admin.users.buttons.viewDetails")}</span>
                                                    <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
                                                </Link>
                                            </div>
                                        </div>
                                    </article>
                                )}
                            />

                            <PaginationControls
                                page={page + 1}
                                pageCount={Math.max(totalPages, 1)}
                                onPageChange={(nextPage) => setPage(nextPage - 1)}
                            />
                        </>
                    )}
                </div>
            </div>
        </main>
    );
}
