import { useEffect, useState } from "react";
import { approveStudent, getPendingStudents, rejectStudent } from "../../../api/adminApi";
import { getApiErrorMessage } from "../../../api/apiError";
import MainFooter from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
import type { PendingStudent } from "../../../types/admin";

type ActionState = {
    studentId: number | null;
    type: "approve" | "reject" | null;
};

export default function PendingStudentsPage() {
    const [students, setStudents] = useState<PendingStudent[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [feedback, setFeedback] = useState("");
    const [actionState, setActionState] = useState<ActionState>({
        studentId: null,
        type: null,
    });

    useEffect(() => {
        async function fetchPendingStudents() {
            try {
                setLoading(true);
                setError("");
                setStudents(await getPendingStudents());
            } catch (requestError) {
                setError(getApiErrorMessage(requestError, "Unable to load pending students."));
            } finally {
                setLoading(false);
            }
        }

        fetchPendingStudents();
    }, []);

    async function handleAction(studentId: number, type: "approve" | "reject") {
        try {
            setActionState({ studentId, type });
            setError("");
            setFeedback("");

            const response = type === "approve"
                ? await approveStudent(studentId)
                : await rejectStudent(studentId);

            if (!response.success) {
                setError(response.message || "Unable to process this student.");
                return;
            }

            setStudents((prev) => prev.filter((student) => student.id !== studentId));
            setFeedback(response.message);
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to process this student."));
        } finally {
            setActionState({ studentId: null, type: null });
        }
    }

    return (
        <>
            <MainHeader />

            <main className="min-h-screen bg-[linear-gradient(180deg,#f4f8ff_0%,#eef3f8_44%,#f7fafc_100%)]">
                <div className="mx-auto max-w-screen-xl px-6 py-10">
                    <div className="mb-10 flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
                        <div className="max-w-2xl">
                            <span className="mb-3 inline-flex rounded-full border border-amber-200 bg-amber-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-amber-700">
                                Admin Review
                            </span>
                            <h1 className="text-4xl font-extrabold tracking-tight text-slate-950">
                                Pending student approvals
                            </h1>
                            <p className="mt-4 text-base leading-7 text-slate-500">
                                Review newly onboarded students, inspect submitted document paths, and decide whether each
                                account should move to ACTIVE or REJECTED.
                            </p>
                        </div>

                        <div className="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-600 shadow-sm">
                            {students.length} pending student{students.length === 1 ? "" : "s"}
                        </div>
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
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-10 text-center text-sm font-medium text-slate-500 shadow-sm">
                            Loading pending students...
                        </div>
                    ) : students.length === 0 ? (
                        <div className="rounded-[28px] border border-slate-200 bg-white px-6 py-12 text-center shadow-sm">
                            <div className="mx-auto mb-4 flex h-16 w-16 items-center justify-center rounded-full bg-emerald-50 text-emerald-600">
                                <span className="material-symbols-outlined text-[30px]">task_alt</span>
                            </div>
                            <h2 className="text-2xl font-bold text-slate-900">No pending students</h2>
                            <p className="mt-3 text-sm text-slate-500">
                                All currently onboarded students have already been reviewed.
                            </p>
                        </div>
                    ) : (
                        <div className="grid gap-6 lg:grid-cols-2">
                            {students.map((student) => {
                                const isApproving = actionState.studentId === student.id && actionState.type === "approve";
                                const isRejecting = actionState.studentId === student.id && actionState.type === "reject";
                                const isBusy = actionState.studentId === student.id;

                                return (
                                    <article
                                        key={student.id}
                                        className="rounded-[28px] border border-slate-200 bg-white p-6 shadow-[0_18px_40px_rgba(15,23,42,0.05)]"
                                    >
                                        <div className="mb-5 flex items-start justify-between gap-4">
                                            <div>
                                                <span className="inline-flex rounded-full border border-amber-200 bg-amber-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.22em] text-amber-700">
                                                    {student.status}
                                                </span>
                                                <h2 className="mt-3 text-2xl font-bold text-slate-950">{student.name}</h2>
                                                <p className="mt-1 text-sm text-slate-500">{student.email}</p>
                                            </div>
                                            <div className="rounded-2xl bg-slate-100 px-3 py-2 text-xs font-semibold uppercase tracking-[0.16em] text-slate-500">
                                                ID {student.id}
                                            </div>
                                        </div>

                                        <div className="grid gap-3 rounded-2xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
                                            <div className="flex items-center justify-between gap-4">
                                                <span className="font-semibold text-slate-500">Student code</span>
                                                <span className="text-right font-medium text-slate-900">{student.studentCode}</span>
                                            </div>
                                            <div className="flex items-center justify-between gap-4">
                                                <span className="font-semibold text-slate-500">Department</span>
                                                <span className="text-right font-medium text-slate-900">{student.departmentName ?? "Unassigned"}</span>
                                            </div>
                                            <div className="flex flex-col gap-1">
                                                <span className="font-semibold text-slate-500">Student card image</span>
                                                <span className="break-all text-slate-900">{student.studentCardImageUrl ?? "Missing"}</span>
                                            </div>
                                            <div className="flex flex-col gap-1">
                                                <span className="font-semibold text-slate-500">National ID image</span>
                                                <span className="break-all text-slate-900">{student.nationalIdImageUrl ?? "Missing"}</span>
                                            </div>
                                        </div>

                                        <div className="mt-6 flex gap-3">
                                            <button
                                                type="button"
                                                onClick={() => handleAction(student.id, "approve")}
                                                disabled={isBusy}
                                                className="inline-flex flex-1 items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#0f8f55_0%,#22c55e_100%)] px-4 py-3 text-sm font-bold text-white shadow-[0_16px_36px_rgba(34,197,94,0.22)] transition hover:translate-y-[-1px] hover:shadow-[0_18px_40px_rgba(34,197,94,0.28)] disabled:cursor-not-allowed disabled:opacity-60 disabled:shadow-none"
                                            >
                                                <span>{isApproving ? "Approving..." : "Approve"}</span>
                                                <span className="material-symbols-outlined text-[18px]">check_circle</span>
                                            </button>
                                            <button
                                                type="button"
                                                onClick={() => handleAction(student.id, "reject")}
                                                disabled={isBusy}
                                                className="inline-flex flex-1 items-center justify-center gap-2 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-bold text-red-700 transition hover:border-red-300 hover:bg-red-100 disabled:cursor-not-allowed disabled:opacity-60"
                                            >
                                                <span>{isRejecting ? "Rejecting..." : "Reject"}</span>
                                                <span className="material-symbols-outlined text-[18px]">cancel</span>
                                            </button>
                                        </div>
                                    </article>
                                );
                            })}
                        </div>
                    )}
                </div>
            </main>

            <MainFooter />
        </>
    );
}
