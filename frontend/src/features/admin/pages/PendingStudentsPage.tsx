import { useCallback, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { approveStudent, getPendingStudents, getStudentDocument, getUserManagementDepartments, rejectStudent } from "../../../api/adminApi";
import { getApiErrorMessage } from "../../../api/apiError";
import ConfirmDialog from "../../../components/ui/ConfirmDialog";
import DataTable, { type DataTableColumn } from "../../../components/data-view/DataTable";
import DataToolbar from "../../../components/data-view/DataToolbar";
import PaginationControls from "../../../components/data-view/PaginationControls";
import ResponsiveDataList from "../../../components/data-view/ResponsiveDataList";
import SearchInput from "../../../components/data-view/SearchInput";
import SelectFilter from "../../../components/data-view/SelectFilter";
import DetailPanel from "../../../components/ui/DetailPanel";
import EmptyState from "../../../components/ui/EmptyState";
import ErrorState from "../../../components/ui/ErrorState";
import LoadingState from "../../../components/ui/LoadingState";
import PageHeader from "../../../components/ui/PageHeader";
import SectionCard from "../../../components/ui/SectionCard";
import StatusBadge from "../../../components/ui/StatusBadge";
import { darkActionButtonClass, darkActionButtonStyle } from "../../../components/ui/buttonStyles";
import type { DepartmentOption, PendingStudent } from "../../../types/admin";

type ActionState = {
    studentId: number | null;
    type: "approve" | "reject" | null;
};

type ReviewDraft = {
    reviewReason: string;
    reviewNotes: string;
};

type DocumentPreview = {
    name: string;
    contentType: string;
    objectUrl: string;
};

function getInitialDraft(student: PendingStudent): ReviewDraft {
    return {
        reviewReason: student.reviewReason ?? "",
        reviewNotes: student.reviewNotes ?? "",
    };
}

export default function PendingStudentsPage() {
    const { t } = useTranslation(["admin", "validation"]);
    const [students, setStudents] = useState<PendingStudent[]>([]);
    const [departments, setDepartments] = useState<DepartmentOption[]>([]);
    const [totalElements, setTotalElements] = useState(0);
    const [totalPages, setTotalPages] = useState(0);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [feedback, setFeedback] = useState("");
    const [drafts, setDrafts] = useState<Record<number, ReviewDraft>>({});
    const [actionState, setActionState] = useState<ActionState>({ studentId: null, type: null });
    const [search, setSearch] = useState("");
    const [debouncedSearch, setDebouncedSearch] = useState("");
    const [departmentFilter, setDepartmentFilter] = useState("ALL");
    const [resubmissionFilter, setResubmissionFilter] = useState("ALL");
    const [sortBy, setSortBy] = useState("resubmissionCount");
    const [sortDir, setSortDir] = useState("desc");
    const [page, setPage] = useState(0);
    const [activeStudentId, setActiveStudentId] = useState<number | null>(null);
    const [confirmAction, setConfirmAction] = useState<"approve" | "reject" | null>(null);
    const [documentPreviews, setDocumentPreviews] = useState<{
        studentCard: DocumentPreview | null;
        nationalId: DocumentPreview | null;
    }>({
        studentCard: null,
        nationalId: null,
    });
    const [documentLoading, setDocumentLoading] = useState(false);
    const [documentError, setDocumentError] = useState("");

    const pageSize = 10;

    const loadPendingStudents = useCallback(async () => {
        try {
            setLoading(true);
            setError("");
            const response = await getPendingStudents({
                keyword: debouncedSearch || undefined,
                departmentId: departmentFilter === "ALL" ? undefined : Number(departmentFilter),
                submissionType: resubmissionFilter === "ALL" ? undefined : resubmissionFilter,
                page,
                size: pageSize,
                sortBy,
                sortDir,
            });

            if (response.items.length === 0 && response.totalPages > 0 && page >= response.totalPages) {
                setPage(response.totalPages - 1);
                return;
            }

            setStudents(response.items);
            setTotalElements(response.totalElements);
            setTotalPages(response.totalPages);
            setDrafts((prev) => {
                const next = { ...prev };
                response.items.forEach((student) => {
                    next[student.id] = prev[student.id] ?? getInitialDraft(student);
                });
                return next;
            });
            setActiveStudentId((current) => {
                if (current != null && response.items.some((student) => student.id === current)) {
                    return current;
                }
                return response.items[0]?.id ?? null;
            });
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.pendingStudents.errors.load")));
        } finally {
            setLoading(false);
        }
    }, [debouncedSearch, departmentFilter, page, resubmissionFilter, sortBy, sortDir, t]);

    useEffect(() => {
        const timeout = window.setTimeout(() => {
            setDebouncedSearch(search.trim());
        }, 300);

        return () => window.clearTimeout(timeout);
    }, [search]);

    useEffect(() => {
        void loadPendingStudents();
    }, [loadPendingStudents]);

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

    function updateDraft(studentId: number, patch: Partial<ReviewDraft>) {
        setDrafts((prev) => ({
            ...prev,
            [studentId]: { ...(prev[studentId] ?? { reviewReason: "", reviewNotes: "" }), ...patch },
        }));
    }

    async function handleApprove(studentId: number) {
        try {
            setActionState({ studentId, type: "approve" });
            setError("");
            setFeedback("");

            const response = await approveStudent(studentId, {
                reviewNotes: drafts[studentId]?.reviewNotes?.trim() || undefined,
            });

            if (!response.success) {
                setError(response.message || t("admin:admin.pendingStudents.errors.approve"));
                return;
            }

            setFeedback(response.message);
            setConfirmAction(null);
            await loadPendingStudents();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.pendingStudents.errors.approve")));
        } finally {
            setActionState({ studentId: null, type: null });
        }
    }

    async function handleReject(studentId: number) {
        const reviewReason = drafts[studentId]?.reviewReason?.trim() ?? "";
        if (!reviewReason) {
            setError(t("validation:validation.admin.pendingStudents.rejectionReasonRequired"));
            setConfirmAction(null);
            return;
        }

        try {
            setActionState({ studentId, type: "reject" });
            setError("");
            setFeedback("");

            const response = await rejectStudent(studentId, {
                reviewReason,
                reviewNotes: drafts[studentId]?.reviewNotes?.trim() || undefined,
            });

            if (!response.success) {
                setError(response.message || t("admin:admin.pendingStudents.errors.reject"));
                return;
            }

            setFeedback(response.message);
            setConfirmAction(null);
            await loadPendingStudents();
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, t("admin:admin.pendingStudents.errors.reject")));
        } finally {
            setActionState({ studentId: null, type: null });
        }
    }

    const departmentOptions = useMemo(() => [
        { label: t("admin:admin.users.filters.allDepartments"), value: "ALL" },
        ...departments.map((department) => ({ label: department.name, value: String(department.id) })),
    ], [departments, t]);

    useEffect(() => {
        setPage(0);
    }, [debouncedSearch, departmentFilter, resubmissionFilter, sortBy, sortDir]);

    const activeStudent = students.find((student) => student.id === activeStudentId) ?? students[0] ?? null;

    useEffect(() => {
        if (!activeStudent && students[0]) {
            setActiveStudentId(students[0].id);
        }
    }, [activeStudent, students]);

    const getDocumentName = useCallback((storedPath: string | null): string => {
        if (!storedPath) {
            return t("admin:admin.pendingStudents.documents.document");
        }

        const normalized = storedPath.replace(/\\/g, "/");
        return normalized.split("/").filter(Boolean).pop() || t("admin:admin.pendingStudents.documents.document");
    }, [t]);

    const createPreview = useCallback((blob: Blob | null, storedPath: string | null): DocumentPreview | null => {
        if (!blob) {
            return null;
        }

        const objectUrl = URL.createObjectURL(blob);
        return {
            name: getDocumentName(storedPath),
            contentType: blob.type || "application/octet-stream",
            objectUrl,
        };
    }, [getDocumentName]);

    useEffect(() => {
        let disposed = false;
        const objectUrls: string[] = [];

        setDocumentError("");
        setDocumentPreviews({ studentCard: null, nationalId: null });

        if (!activeStudent) {
            setDocumentLoading(false);
            return () => {
                objectUrls.forEach((url) => URL.revokeObjectURL(url));
            };
        }

        async function loadDocuments() {
            try {
                setDocumentLoading(true);

                const [studentCardBlob, nationalIdBlob] = await Promise.all([
                    activeStudent.studentCardImageUrl ? getStudentDocument(activeStudent.id, "student-card") : Promise.resolve(null),
                    activeStudent.nationalIdImageUrl ? getStudentDocument(activeStudent.id, "national-id") : Promise.resolve(null),
                ]);

                const nextState = {
                    studentCard: createPreview(studentCardBlob, activeStudent.studentCardImageUrl),
                    nationalId: createPreview(nationalIdBlob, activeStudent.nationalIdImageUrl),
                };

                if (disposed) {
                    Object.values(nextState)
                        .filter((value): value is DocumentPreview => value !== null)
                        .forEach((value) => URL.revokeObjectURL(value.objectUrl));
                    return;
                }

                if (nextState.studentCard) {
                    objectUrls.push(nextState.studentCard.objectUrl);
                }
                if (nextState.nationalId) {
                    objectUrls.push(nextState.nationalId.objectUrl);
                }
                setDocumentPreviews(nextState);
            } catch (requestError) {
                if (!disposed) {
                    setDocumentError(getApiErrorMessage(requestError, t("admin:admin.pendingStudents.errors.loadDocuments")));
                }
            } finally {
                if (!disposed) {
                    setDocumentLoading(false);
                }
            }
        }

        void loadDocuments();

        return () => {
            disposed = true;
            objectUrls.forEach((url) => URL.revokeObjectURL(url));
        };
    }, [activeStudent, createPreview]);

    function renderDocumentPreview(preview: DocumentPreview | null, emptyLabel: string) {
        if (!preview) {
            return <span className="text-slate-500">{emptyLabel}</span>;
        }

        if (preview.contentType.startsWith("image/")) {
            return (
                <div className="space-y-3">
                    <img
                        src={preview.objectUrl}
                        alt={preview.name}
                        className="max-h-80 w-full rounded-2xl border border-slate-200 bg-white object-contain"
                    />
                    <a
                        href={preview.objectUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="inline-flex text-sm font-semibold text-blue-700 hover:text-blue-800"
                    >
                        {t("admin:admin.pendingStudents.documents.openFullImage")}
                    </a>
                </div>
            );
        }

        if (preview.contentType === "application/pdf") {
            return (
                <div className="space-y-3">
                    <iframe
                        src={preview.objectUrl}
                        title={preview.name}
                        className="h-96 w-full rounded-2xl border border-slate-200 bg-white"
                    />
                    <a
                        href={preview.objectUrl}
                        target="_blank"
                        rel="noreferrer"
                        className="inline-flex text-sm font-semibold text-blue-700 hover:text-blue-800"
                    >
                        {t("admin:admin.pendingStudents.documents.openPdf")}
                    </a>
                </div>
            );
        }

        return (
            <a
                href={preview.objectUrl}
                target="_blank"
                rel="noreferrer"
                className="inline-flex text-sm font-semibold text-blue-700 hover:text-blue-800"
            >
                {t("admin:admin.pendingStudents.documents.openDocument")}
            </a>
        );
    }

    const columns: DataTableColumn<PendingStudent>[] = [
        {
            key: "student",
            header: t("admin:admin.pendingStudents.table.student"),
            render: (student) => (
                <div>
                    <p className="font-bold text-slate-950">{student.name}</p>
                    <p className="mt-1 text-sm text-slate-500">{student.email}</p>
                </div>
            ),
        },
        {
            key: "code",
            header: t("admin:admin.users.detail.fields.studentCode"),
            render: (student) => student.studentCode,
        },
        {
            key: "department",
            header: t("admin:admin.users.table.department"),
            render: (student) => student.departmentName ?? t("admin:admin.dashboard.common.unassigned"),
        },
        {
            key: "status",
            header: t("admin:admin.users.table.studentStatus"),
            render: (student) => (
                <div className="flex flex-wrap gap-2">
                    <StatusBadge kind="onboarding" value={student.status} />
                    {student.resubmissionCount > 0 ? <span className="inline-flex rounded-full border border-sky-200 bg-sky-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-sky-700">{t("admin:admin.pendingStudents.resubmitted", { count: student.resubmissionCount })}</span> : null}
                </div>
            ),
        },
        {
            key: "actions",
            header: t("admin:admin.users.table.actions"),
            className: "text-right",
            render: (student) => (
                <div className="flex justify-end">
                    <button type="button" onClick={() => setActiveStudentId(student.id)} className="inline-flex items-center rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                        {t("admin:admin.pendingStudents.buttons.review")}
                    </button>
                </div>
            ),
        },
    ];

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-2xl px-6 py-10">
                <PageHeader
                    eyebrow={t("admin:admin.pendingStudents.header.eyebrow")}
                    title={t("admin:admin.pendingStudents.header.title")}
                    description={t("admin:admin.pendingStudents.header.description")}
                    actions={<div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm">{t("admin:admin.pendingStudents.header.count", { count: totalElements })}</div>}
                />

                <div className="mt-6 space-y-6">
                    <DataToolbar
                        filters={(
                            <>
                                <SearchInput value={search} onChange={setSearch} placeholder={t("admin:admin.pendingStudents.filters.search")} />
                                <SelectFilter label={t("admin:admin.users.filters.department")} value={departmentFilter} onChange={setDepartmentFilter} options={departmentOptions} />
                                <SelectFilter label={t("admin:admin.pendingStudents.filters.submission")} value={resubmissionFilter} onChange={setResubmissionFilter} options={[{ label: t("admin:admin.pendingStudents.filters.allSubmissions"), value: "ALL" }, { label: t("admin:admin.pendingStudents.filters.resubmittedOnly"), value: "RESUBMITTED" }, { label: t("admin:admin.pendingStudents.filters.firstSubmission"), value: "FIRST_SUBMISSION" }]} />
                                <SelectFilter
                                    label={t("admin:admin.users.filters.sort")}
                                    value={`${sortBy}:${sortDir}`}
                                    onChange={(value) => {
                                        const [nextSortBy, nextSortDir] = value.split(":");
                                        setSortBy(nextSortBy);
                                        setSortDir(nextSortDir);
                                    }}
                                    options={[
                                        { label: t("admin:admin.pendingStudents.filters.resubmissionCount"), value: "resubmissionCount:desc" },
                                        { label: t("admin:admin.pendingStudents.filters.studentNameAsc"), value: "name:asc" },
                                        { label: t("admin:admin.users.filters.departmentAsc"), value: "department:asc" },
                                    ]}
                                />
                            </>
                        )}
                    />

                    {error ? <ErrorState description={error} onRetry={() => void loadPendingStudents()} /> : null}
                    {feedback ? <div className="rounded-[24px] border border-emerald-200 bg-emerald-50 px-5 py-4 text-sm font-medium text-emerald-700">{feedback}</div> : null}

                    {loading ? (
                        <LoadingState label={t("admin:admin.pendingStudents.loading")} />
                    ) : students.length === 0 ? (
                        <EmptyState title={t("admin:admin.pendingStudents.empty.title")} description={t("admin:admin.pendingStudents.empty.description")} icon="task_alt" />
                    ) : (
                        <>
                            <div className="hidden lg:block">
                                <DataTable columns={columns} items={students} getRowKey={(student) => student.id} />
                            </div>

                            <ResponsiveDataList
                                items={students}
                                getKey={(student) => student.id}
                                renderItem={(student) => (
                                    <article className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
                                        <div className="flex items-start justify-between gap-4">
                                            <div>
                                                <div className="flex flex-wrap gap-2">
                                                    <StatusBadge kind="onboarding" value={student.status} />
                                                    {student.resubmissionCount > 0 ? <span className="inline-flex rounded-full border border-sky-200 bg-sky-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-sky-700">{t("admin:admin.pendingStudents.resubmitted", { count: student.resubmissionCount })}</span> : null}
                                                </div>
                                                <h2 className="mt-3 text-xl font-bold text-slate-950">{student.name}</h2>
                                                <p className="mt-1 text-sm text-slate-500">{student.email}</p>
                                            </div>
                                        </div>
                                        <div className="mt-4 grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">{t("admin:admin.users.detail.fields.studentCode")}</span><span className="font-medium text-slate-900">{student.studentCode}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">{t("admin:admin.users.table.department")}</span><span className="font-medium text-slate-900">{student.departmentName ?? t("admin:admin.dashboard.common.unassigned")}</span></div>
                                        </div>
                                        <button type="button" onClick={() => setActiveStudentId(student.id)} className="mt-5 inline-flex w-full items-center justify-center rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                                            {t("admin:admin.pendingStudents.buttons.review")}
                                        </button>
                                    </article>
                                )}
                            />

                            <PaginationControls page={page + 1} pageCount={Math.max(totalPages, 1)} onPageChange={(nextPage) => setPage(nextPage - 1)} />

                            {activeStudent ? (
                                <SectionCard title={t("admin:admin.pendingStudents.detail.title")} description={t("admin:admin.pendingStudents.detail.description")}>
                                    <div className="grid gap-6 xl:grid-cols-[320px_minmax(0,1fr)]">
                                        <DetailPanel
                                            title={t("admin:admin.pendingStudents.detail.context")}
                                            items={[
                                                { label: t("admin:admin.pendingStudents.table.student"), value: activeStudent.name },
                                                { label: t("admin:admin.users.detail.fields.email"), value: activeStudent.email },
                                                { label: t("admin:admin.users.detail.fields.studentCode"), value: activeStudent.studentCode },
                                                { label: t("admin:admin.users.table.department"), value: activeStudent.departmentName ?? t("admin:admin.dashboard.common.unassigned") },
                                            ]}
                                        />

                                        <div className="space-y-5">
                                            <SectionCard title={t("admin:admin.pendingStudents.documents.title")} description={t("admin:admin.pendingStudents.documents.description")}>
                                                <div className="grid gap-4 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                                    {documentLoading ? <p>{t("admin:admin.pendingStudents.documents.loading")}</p> : null}
                                                    {documentError ? <p className="font-medium text-red-700">{documentError}</p> : null}
                                                    <div className="space-y-3">
                                                        <span className="font-semibold text-slate-500">{t("admin:admin.pendingStudents.documents.studentCard")}</span>
                                                        <p className="text-xs text-slate-500">{getDocumentName(activeStudent.studentCardImageUrl)}</p>
                                                        {renderDocumentPreview(documentPreviews.studentCard, t("admin:admin.pendingStudents.documents.missing"))}
                                                    </div>
                                                    <div className="space-y-3">
                                                        <span className="font-semibold text-slate-500">{t("admin:admin.pendingStudents.documents.nationalId")}</span>
                                                        <p className="text-xs text-slate-500">{getDocumentName(activeStudent.nationalIdImageUrl)}</p>
                                                        {renderDocumentPreview(documentPreviews.nationalId, t("admin:admin.pendingStudents.documents.missing"))}
                                                    </div>
                                                </div>
                                            </SectionCard>

                                            {(activeStudent.reviewReason || activeStudent.reviewNotes) ? (
                                                <SectionCard title={t("admin:admin.pendingStudents.previous.title")} description={t("admin:admin.pendingStudents.previous.description")}>
                                                    <div className="space-y-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                                        {activeStudent.reviewReason ? <p><span className="font-semibold text-slate-900">{t("admin:admin.pendingStudents.previous.reason")}</span> {activeStudent.reviewReason}</p> : null}
                                                        {activeStudent.reviewNotes ? <p className="whitespace-pre-wrap"><span className="font-semibold text-slate-900">{t("admin:admin.pendingStudents.previous.notes")}</span> {activeStudent.reviewNotes}</p> : null}
                                                    </div>
                                                </SectionCard>
                                            ) : null}

                                            <SectionCard title={t("admin:admin.pendingStudents.decision.title")} description={t("admin:admin.pendingStudents.decision.description")}>
                                                <div className="space-y-4">
                                                    <label className="block space-y-2">
                                                        <span className="text-sm font-semibold text-slate-700">{t("admin:admin.pendingStudents.decision.rejectionReason")}</span>
                                                        <input type="text" value={drafts[activeStudent.id]?.reviewReason ?? ""} onChange={(event) => updateDraft(activeStudent.id, { reviewReason: event.target.value })} placeholder={t("admin:admin.pendingStudents.decision.rejectionPlaceholder")} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5" />
                                                    </label>
                                                    <label className="block space-y-2">
                                                        <span className="text-sm font-semibold text-slate-700">{t("admin:admin.pendingStudents.decision.reviewerNotes")}</span>
                                                        <textarea value={drafts[activeStudent.id]?.reviewNotes ?? ""} onChange={(event) => updateDraft(activeStudent.id, { reviewNotes: event.target.value })} rows={5} placeholder={t("admin:admin.pendingStudents.decision.notesPlaceholder")} className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5" />
                                                    </label>
                                                    <div className="grid gap-3 md:grid-cols-2">
                                                        <button type="button" onClick={() => setConfirmAction("approve")} disabled={actionState.studentId === activeStudent.id} className={`${darkActionButtonClass} px-4 py-3 text-sm font-semibold`} style={darkActionButtonStyle}>
                                                            <span className="text-white" style={darkActionButtonStyle}>
                                                                {actionState.studentId === activeStudent.id && actionState.type === "approve" ? t("admin:admin.pendingStudents.buttons.approving") : t("admin:admin.pendingStudents.buttons.approve")}
                                                            </span>
                                                        </button>
                                                        <button type="button" onClick={() => setConfirmAction("reject")} disabled={actionState.studentId === activeStudent.id} className="inline-flex items-center justify-center rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700 transition hover:border-red-300 hover:bg-red-100 disabled:cursor-not-allowed disabled:opacity-60">
                                                            {actionState.studentId === activeStudent.id && actionState.type === "reject" ? t("admin:admin.pendingStudents.buttons.rejecting") : t("admin:admin.pendingStudents.buttons.reject")}
                                                        </button>
                                                    </div>
                                                </div>
                                            </SectionCard>
                                        </div>
                                    </div>
                                </SectionCard>
                            ) : null}
                        </>
                    )}
                </div>
            </div>

            <ConfirmDialog
                open={confirmAction != null && activeStudent != null}
                title={confirmAction === "approve" ? t("admin:admin.pendingStudents.confirm.approveTitle") : t("admin:admin.pendingStudents.confirm.rejectTitle")}
                description={confirmAction === "approve" ? t("admin:admin.pendingStudents.confirm.approveDescription", { name: activeStudent?.name }) : t("admin:admin.pendingStudents.confirm.rejectDescription", { name: activeStudent?.name })}
                confirmLabel={confirmAction === "approve" ? t("admin:admin.pendingStudents.confirm.approveLabel") : t("admin:admin.pendingStudents.confirm.rejectLabel")}
                tone={confirmAction === "reject" ? "danger" : "default"}
                busy={activeStudent != null && actionState.studentId === activeStudent.id}
                onCancel={() => setConfirmAction(null)}
                onConfirm={() => {
                    if (!activeStudent) {
                        return;
                    }
                    if (confirmAction === "approve") {
                        void handleApprove(activeStudent.id);
                    } else {
                        void handleReject(activeStudent.id);
                    }
                }}
            />
        </main>
    );
}
