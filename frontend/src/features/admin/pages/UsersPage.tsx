import { useCallback, useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
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
    { label: "All", value: "ALL" },
    { label: "Students", value: "STUDENT" },
    { label: "Lecturers", value: "TEACHER" },
    { label: "Admins", value: "ADMIN" },
] as const;

export default function UsersPage() {
    const [users, setUsers] = useState<ManagedUserSummary[]>([]);
    const [metrics, setMetrics] = useState<ManagedUserMetrics>({
        totalUsers: 0,
        totalStudents: 0,
        totalTeachers: 0,
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
                    roleTab === "TEACHER" || roleTab === "ADMIN"
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
            setError(getApiErrorMessage(requestError, "Unable to load users."));
        } finally {
            setLoading(false);
        }
    }, [debouncedQuery, departmentFilter, page, roleTab, sortBy, sortDir, statusFilter, studentStatusFilter]);

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
                setError(response.message || "Unable to update user state.");
                return;
            }
            await loadUsers();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to update user state."));
        } finally {
            setActionUserId(null);
        }
    }

    const departmentOptions = useMemo(() => [
        { label: "All departments", value: "ALL" },
        ...departments.map((department) => ({ label: department.name, value: String(department.id) })),
    ], [departments]);

    const columns: DataTableColumn<ManagedUserSummary>[] = [
        {
            key: "user",
            header: "User",
            render: (user) => (
                <div>
                    <p className="font-bold text-slate-950">{user.name}</p>
                    <p className="mt-1 text-sm text-slate-500">{user.email}</p>
                </div>
            ),
        },
        {
            key: "role",
            header: "Role",
            render: (user) => <RoleBadge value={user.role} />,
        },
        {
            key: "code",
            header: "Code",
            render: (user) => user.studentCode ?? user.teacherCode ?? "N/A",
        },
        {
            key: "account",
            header: "Account",
            render: (user) => <StatusBadge kind="account" value={user.active ? "ACTIVE" : "INACTIVE"} />,
        },
        {
            key: "department",
            header: "Department",
            render: (user) => user.departmentName ?? "N/A",
        },
        {
            key: "studentStatus",
            header: "Student Status",
            render: (user) => user.studentStatus ? <StatusBadge kind="onboarding" value={user.studentStatus} /> : "N/A",
        },
        {
            key: "actions",
            header: "Actions",
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
                            <span>{actionUserId === user.id ? "Updating..." : user.active ? "Deactivate" : "Activate"}</span>
                        </button>
                        <Link
                            to={`/admin/users/${user.id}`}
                            className="inline-flex items-center gap-2 rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50"
                        >
                            <span>View details</span>
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
                    eyebrow="Admin / Users"
                    title="User management"
                    description="Operate on user accounts through role-aware search, filters, pagination, and row-level actions instead of a long visual list."
                    actions={(
                        <div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm">
                            {totalElements} matching user{totalElements === 1 ? "" : "s"}
                        </div>
                    )}
                />

                <div className="mt-6 space-y-6">
                    <div className="grid gap-4 md:grid-cols-3 xl:grid-cols-6">
                        <StatCard label="Total users" value={metrics.totalUsers} />
                        <StatCard label="Students" value={metrics.totalStudents} tone="blue" />
                        <StatCard label="Lecturers" value={metrics.totalTeachers} tone="sky" />
                        <StatCard label="Admins" value={metrics.totalAdmins} tone="slate" />
                        <StatCard label="Inactive" value={metrics.totalInactive} tone="amber" />
                        <StatCard label="Pending" value={metrics.totalPending} tone="amber" />
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
                                {tab.label}
                            </button>
                        ))}
                    </div>

                    <DataToolbar
                        filters={(
                            <>
                                <SearchInput
                                    value={query}
                                    onChange={setQuery}
                                    placeholder="Search by name, email, student code, or lecturer code"
                                />
                                <SelectFilter
                                    label="Account"
                                    value={statusFilter}
                                    onChange={setStatusFilter}
                                    options={[
                                        { label: "All states", value: "ALL" },
                                        { label: "Active", value: "ACTIVE" },
                                        { label: "Inactive", value: "INACTIVE" },
                                    ]}
                                />
                                <SelectFilter
                                    label="Department"
                                    value={departmentFilter}
                                    onChange={setDepartmentFilter}
                                    options={departmentOptions}
                                />
                                {(roleTab === "STUDENT" || roleTab === "ALL") ? (
                                    <SelectFilter
                                        label="Student status"
                                        value={studentStatusFilter}
                                        onChange={setStudentStatusFilter}
                                        options={[
                                            { label: "All statuses", value: "ALL" },
                                            { label: "Approved", value: "ACTIVE" },
                                            { label: "Pending Review", value: "PENDING" },
                                            { label: "Email Verified", value: "EMAIL_VERIFIED" },
                                            { label: "Email Unverified", value: "EMAIL_UNVERIFIED" },
                                            { label: "Rejected", value: "REJECTED" },
                                        ]}
                                    />
                                ) : null}
                                <SelectFilter
                                    label="Sort"
                                    value={`${sortBy}:${sortDir}`}
                                    onChange={(value) => {
                                        const [nextSortBy, nextSortDir] = value.split(":");
                                        setSortBy(nextSortBy);
                                        setSortDir(nextSortDir);
                                    }}
                                    options={[
                                        { label: "Name A-Z", value: "name:asc" },
                                        { label: "Name Z-A", value: "name:desc" },
                                        { label: "Email A-Z", value: "email:asc" },
                                        { label: "Role A-Z", value: "role:asc" },
                                        { label: "Department A-Z", value: "department:asc" },
                                        { label: "Account status", value: "active:asc" },
                                        { label: "Student status", value: "status:asc" },
                                    ]}
                                />
                            </>
                        )}
                        actions={(
                            <div className="rounded-xl border border-slate-200 bg-slate-50 px-3 py-2 text-sm font-semibold text-slate-600">
                                {users.length} row{users.length === 1 ? "" : "s"} on page
                            </div>
                        )}
                    />

                    {error ? (
                        <ErrorState description={error} onRetry={() => void loadUsers()} />
                    ) : null}

                    {loading ? (
                        <LoadingState label="Loading users..." />
                    ) : users.length === 0 ? (
                        <EmptyState
                            title="No matching users"
                            description="Adjust role tabs, search, or filters to find the account records you need."
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
                                                ID {user.id}
                                            </div>
                                        </div>

                                        <div className="mt-4 grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                            <div className="flex items-center justify-between gap-4">
                                                <span className="font-semibold text-slate-500">Code</span>
                                                <span className="text-right font-medium text-slate-900">{user.studentCode ?? user.teacherCode ?? "N/A"}</span>
                                            </div>
                                            <div className="flex items-center justify-between gap-4">
                                                <span className="font-semibold text-slate-500">Department</span>
                                                <span className="text-right font-medium text-slate-900">{user.departmentName ?? "N/A"}</span>
                                            </div>
                                            <div className="flex items-center justify-between gap-4">
                                                <span className="font-semibold text-slate-500">Student status</span>
                                                <span className="text-right font-medium text-slate-900">
                                                    {user.studentStatus ? user.studentStatus.replace(/_/g, " ").toLowerCase().replace(/^\w/, (letter) => letter.toUpperCase()) : "N/A"}
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
                                                    <span>{actionUserId === user.id ? "Updating..." : user.active ? "Deactivate user" : "Activate user"}</span>
                                                </button>
                                                <Link
                                                    to={`/admin/users/${user.id}`}
                                                    className="inline-flex w-full items-center justify-center gap-2 rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50"
                                                >
                                                    <span>View details</span>
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
