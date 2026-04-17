import { useCallback, useEffect, useMemo, useState } from "react";
import { approveStudent, getPendingStudents, getStudentDocument, rejectStudent } from "../../../api/adminApi";
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
import type { PendingStudent } from "../../../types/admin";

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
    const [students, setStudents] = useState<PendingStudent[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [feedback, setFeedback] = useState("");
    const [drafts, setDrafts] = useState<Record<number, ReviewDraft>>({});
    const [actionState, setActionState] = useState<ActionState>({ studentId: null, type: null });
    const [search, setSearch] = useState("");
    const [departmentFilter, setDepartmentFilter] = useState("ALL");
    const [resubmissionFilter, setResubmissionFilter] = useState("ALL");
    const [sortOrder, setSortOrder] = useState("resubmissions");
    const [page, setPage] = useState(1);
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

    useEffect(() => {
        async function fetchPendingStudents() {
            try {
                setLoading(true);
                setError("");
                const pendingStudents = await getPendingStudents();
                setStudents(pendingStudents);
                setDrafts(pendingStudents.reduce<Record<number, ReviewDraft>>((acc, student) => {
                    acc[student.id] = getInitialDraft(student);
                    return acc;
                }, {}));
                setActiveStudentId(pendingStudents[0]?.id ?? null);
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, "Unable to load pending students."));
            } finally {
                setLoading(false);
            }
        }

        void fetchPendingStudents();
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
                setError(response.message || "Unable to approve this student.");
                return;
            }

            setStudents((prev) => prev.filter((student) => student.id !== studentId));
            setFeedback(response.message);
            setConfirmAction(null);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to approve this student."));
        } finally {
            setActionState({ studentId: null, type: null });
        }
    }

    async function handleReject(studentId: number) {
        const reviewReason = drafts[studentId]?.reviewReason?.trim() ?? "";
        if (!reviewReason) {
            setError("A rejection reason is required before rejecting a student.");
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
                setError(response.message || "Unable to reject this student.");
                return;
            }

            setStudents((prev) => prev.filter((student) => student.id !== studentId));
            setFeedback(response.message);
            setConfirmAction(null);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to reject this student."));
        } finally {
            setActionState({ studentId: null, type: null });
        }
    }

    const departmentOptions = useMemo(() => [
        { label: "All departments", value: "ALL" },
        ...Array.from(new Set(students.map((student) => student.departmentName).filter(Boolean))).sort().map((department) => ({
            label: department as string,
            value: department as string,
        })),
    ], [students]);

    const filteredStudents = useMemo(() => {
        const normalizedSearch = search.trim().toLowerCase();
        const result = students.filter((student) => {
            if (normalizedSearch) {
                const haystack = [student.name, student.email, student.studentCode].join(" ").toLowerCase();
                if (!haystack.includes(normalizedSearch)) {
                    return false;
                }
            }
            if (departmentFilter !== "ALL" && student.departmentName !== departmentFilter) {
                return false;
            }
            if (resubmissionFilter === "RESUBMITTED" && student.resubmissionCount === 0) {
                return false;
            }
            if (resubmissionFilter === "FIRST_SUBMISSION" && student.resubmissionCount > 0) {
                return false;
            }
            return true;
        });

        result.sort((left, right) => {
            switch (sortOrder) {
                case "name":
                    return left.name.localeCompare(right.name);
                case "department":
                    return (left.departmentName || "").localeCompare(right.departmentName || "");
                default:
                    return right.resubmissionCount - left.resubmissionCount;
            }
        });

        return result;
    }, [departmentFilter, resubmissionFilter, search, sortOrder, students]);

    useEffect(() => {
        setPage(1);
    }, [search, departmentFilter, resubmissionFilter, sortOrder]);

    const pageCount = Math.max(1, Math.ceil(filteredStudents.length / pageSize));
    const pagedStudents = filteredStudents.slice((page - 1) * pageSize, page * pageSize);
    const activeStudent = filteredStudents.find((student) => student.id === activeStudentId) ?? pagedStudents[0] ?? null;

    useEffect(() => {
        if (!activeStudent && pagedStudents[0]) {
            setActiveStudentId(pagedStudents[0].id);
        }
    }, [activeStudent, pagedStudents]);

    const getDocumentName = useCallback((storedPath: string | null): string => {
        if (!storedPath) {
            return "document";
        }

        const normalized = storedPath.replace(/\\/g, "/");
        return normalized.split("/").filter(Boolean).pop() || "document";
    }, []);

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
                    setDocumentError(getApiErrorMessage(requestError, "Unable to load student documents."));
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
                        Open full image
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
                        Open PDF
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
                Open document
            </a>
        );
    }

    const columns: DataTableColumn<PendingStudent>[] = [
        {
            key: "student",
            header: "Student",
            render: (student) => (
                <div>
                    <p className="font-bold text-slate-950">{student.name}</p>
                    <p className="mt-1 text-sm text-slate-500">{student.email}</p>
                </div>
            ),
        },
        {
            key: "code",
            header: "Student code",
            render: (student) => student.studentCode,
        },
        {
            key: "department",
            header: "Department",
            render: (student) => student.departmentName ?? "Unassigned",
        },
        {
            key: "status",
            header: "Status",
            render: (student) => (
                <div className="flex flex-wrap gap-2">
                    <StatusBadge kind="onboarding" value={student.status} />
                    {student.resubmissionCount > 0 ? <span className="inline-flex rounded-full border border-sky-200 bg-sky-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-sky-700">Resubmitted {student.resubmissionCount}x</span> : null}
                </div>
            ),
        },
        {
            key: "actions",
            header: "Actions",
            className: "text-right",
            render: (student) => (
                <div className="flex justify-end">
                    <button type="button" onClick={() => setActiveStudentId(student.id)} className="inline-flex items-center rounded-xl border border-slate-300 bg-white px-3 py-2 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                        Review
                    </button>
                </div>
            ),
        },
    ];

    return (
        <main className="bg-slate-100">
            <div className="mx-auto max-w-screen-2xl px-6 py-10">
                <PageHeader
                    eyebrow="Admin Review"
                    title="Pending student approvals"
                    description="Review onboarding requests as a queue with searchable student context, reusable review notes, and explicit approve or reject decisions."
                    actions={<div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm">{filteredStudents.length} pending student{filteredStudents.length === 1 ? "" : "s"}</div>}
                />

                <div className="mt-6 space-y-6">
                    <DataToolbar
                        filters={(
                            <>
                                <SearchInput value={search} onChange={setSearch} placeholder="Search by name, email, or student code" />
                                <SelectFilter label="Department" value={departmentFilter} onChange={setDepartmentFilter} options={departmentOptions} />
                                <SelectFilter label="Submission" value={resubmissionFilter} onChange={setResubmissionFilter} options={[{ label: "All submissions", value: "ALL" }, { label: "Resubmitted only", value: "RESUBMITTED" }, { label: "First submission", value: "FIRST_SUBMISSION" }]} />
                                <SelectFilter label="Sort" value={sortOrder} onChange={setSortOrder} options={[{ label: "Resubmission count", value: "resubmissions" }, { label: "Student name", value: "name" }, { label: "Department", value: "department" }]} />
                            </>
                        )}
                    />

                    {error ? <ErrorState description={error} /> : null}
                    {feedback ? <div className="rounded-[24px] border border-emerald-200 bg-emerald-50 px-5 py-4 text-sm font-medium text-emerald-700">{feedback}</div> : null}

                    {loading ? (
                        <LoadingState label="Loading pending students..." />
                    ) : filteredStudents.length === 0 ? (
                        <EmptyState title="No pending students in this queue view" description="Adjust the search or filters to find the onboarding request you need." icon="task_alt" />
                    ) : (
                        <>
                            <div className="hidden lg:block">
                                <DataTable columns={columns} items={pagedStudents} getRowKey={(student) => student.id} />
                            </div>

                            <ResponsiveDataList
                                items={pagedStudents}
                                getKey={(student) => student.id}
                                renderItem={(student) => (
                                    <article className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-sm">
                                        <div className="flex items-start justify-between gap-4">
                                            <div>
                                                <div className="flex flex-wrap gap-2">
                                                    <StatusBadge kind="onboarding" value={student.status} />
                                                    {student.resubmissionCount > 0 ? <span className="inline-flex rounded-full border border-sky-200 bg-sky-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-sky-700">Resubmitted {student.resubmissionCount}x</span> : null}
                                                </div>
                                                <h2 className="mt-3 text-xl font-bold text-slate-950">{student.name}</h2>
                                                <p className="mt-1 text-sm text-slate-500">{student.email}</p>
                                            </div>
                                        </div>
                                        <div className="mt-4 grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Student code</span><span className="font-medium text-slate-900">{student.studentCode}</span></div>
                                            <div className="flex items-center justify-between gap-4"><span className="font-semibold text-slate-500">Department</span><span className="font-medium text-slate-900">{student.departmentName ?? "Unassigned"}</span></div>
                                        </div>
                                        <button type="button" onClick={() => setActiveStudentId(student.id)} className="mt-5 inline-flex w-full items-center justify-center rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm font-semibold text-slate-700 transition hover:border-slate-400 hover:bg-slate-50">
                                            Review
                                        </button>
                                    </article>
                                )}
                            />

                            <PaginationControls page={page} pageCount={pageCount} onPageChange={setPage} />

                            {activeStudent ? (
                                <SectionCard title="Review detail" description="Approve or reject without leaving the onboarding queue. Rejection reason remains required.">
                                    <div className="grid gap-6 xl:grid-cols-[320px_minmax(0,1fr)]">
                                        <DetailPanel
                                            title="Context"
                                            items={[
                                                { label: "Student", value: activeStudent.name },
                                                { label: "Email", value: activeStudent.email },
                                                { label: "Student code", value: activeStudent.studentCode },
                                                { label: "Department", value: activeStudent.departmentName ?? "Unassigned" },
                                            ]}
                                        />

                                        <div className="space-y-5">
                                            <SectionCard title="Submitted documents" description="Documents are fetched from the backend and rendered inline when previewable.">
                                                <div className="grid gap-4 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                                    {documentLoading ? <p>Loading submitted documents...</p> : null}
                                                    {documentError ? <p className="font-medium text-red-700">{documentError}</p> : null}
                                                    <div className="space-y-3">
                                                        <span className="font-semibold text-slate-500">Student card</span>
                                                        <p className="text-xs text-slate-500">{getDocumentName(activeStudent.studentCardImageUrl)}</p>
                                                        {renderDocumentPreview(documentPreviews.studentCard, "Missing")}
                                                    </div>
                                                    <div className="space-y-3">
                                                        <span className="font-semibold text-slate-500">National ID</span>
                                                        <p className="text-xs text-slate-500">{getDocumentName(activeStudent.nationalIdImageUrl)}</p>
                                                        {renderDocumentPreview(documentPreviews.nationalId, "Missing")}
                                                    </div>
                                                </div>
                                            </SectionCard>

                                            {(activeStudent.reviewReason || activeStudent.reviewNotes) ? (
                                                <SectionCard title="Previous review context" description="This appears when the student has been previously rejected and resubmitted.">
                                                    <div className="space-y-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                                        {activeStudent.reviewReason ? <p><span className="font-semibold text-slate-900">Reason:</span> {activeStudent.reviewReason}</p> : null}
                                                        {activeStudent.reviewNotes ? <p className="whitespace-pre-wrap"><span className="font-semibold text-slate-900">Notes:</span> {activeStudent.reviewNotes}</p> : null}
                                                    </div>
                                                </SectionCard>
                                            ) : null}

                                            <SectionCard title="Review decision" description="Provide the rejection reason before rejecting. Notes are optional for both decisions.">
                                                <div className="space-y-4">
                                                    <label className="block space-y-2">
                                                        <span className="text-sm font-semibold text-slate-700">Rejection reason</span>
                                                        <input type="text" value={drafts[activeStudent.id]?.reviewReason ?? ""} onChange={(event) => updateDraft(activeStudent.id, { reviewReason: event.target.value })} placeholder="Required if you reject this onboarding request" className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5" />
                                                    </label>
                                                    <label className="block space-y-2">
                                                        <span className="text-sm font-semibold text-slate-700">Reviewer notes</span>
                                                        <textarea value={drafts[activeStudent.id]?.reviewNotes ?? ""} onChange={(event) => updateDraft(activeStudent.id, { reviewNotes: event.target.value })} rows={5} placeholder="Optional notes recorded with the review decision" className="w-full rounded-2xl border border-slate-300 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-500 focus:ring-4 focus:ring-slate-900/5" />
                                                    </label>
                                                    <div className="grid gap-3 md:grid-cols-2">
                                                        <button type="button" onClick={() => setConfirmAction("approve")} disabled={actionState.studentId === activeStudent.id} className={`${darkActionButtonClass} px-4 py-3 text-sm font-semibold`} style={darkActionButtonStyle}>
                                                            <span className="text-white" style={darkActionButtonStyle}>
                                                                {actionState.studentId === activeStudent.id && actionState.type === "approve" ? "Approving..." : "Approve"}
                                                            </span>
                                                        </button>
                                                        <button type="button" onClick={() => setConfirmAction("reject")} disabled={actionState.studentId === activeStudent.id} className="inline-flex items-center justify-center rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700 transition hover:border-red-300 hover:bg-red-100 disabled:cursor-not-allowed disabled:opacity-60">
                                                            {actionState.studentId === activeStudent.id && actionState.type === "reject" ? "Rejecting..." : "Reject"}
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
                title={confirmAction === "approve" ? "Approve student" : "Reject student"}
                description={confirmAction === "approve" ? `Approve ${activeStudent?.name} and activate this onboarding request.` : `Reject ${activeStudent?.name}. A rejection reason will be sent back through the existing resubmission flow.`}
                confirmLabel={confirmAction === "approve" ? "Approve student" : "Reject student"}
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
