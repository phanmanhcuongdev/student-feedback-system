import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { createSurvey } from "../../../api/adminApi";
import { getApiErrorMessage } from "../../../api/apiError";
import MainFooter from "../../../components/layout/MainFooter";
import MainHeader from "../../../components/layout/MainHeader";
import type { CreateSurveyData, CreateQuestionData } from "../../../types/survey";

export default function CreateSurveyPage() {
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    
    // Form fields
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [startDate, setStartDate] = useState("");
    const [endDate, setEndDate] = useState("");
    const [questions, setQuestions] = useState<CreateQuestionData[]>([]);

    function addQuestion() {
        setQuestions([...questions, { content: "", type: "RATING" }]);
    }

    function removeQuestion(index: number) {
        setQuestions(questions.filter((_, i) => i !== index));
    }

    function updateQuestion(index: number, field: keyof CreateQuestionData, value: string) {
        const newQuestions = [...questions];
        newQuestions[index] = { ...newQuestions[index], [field]: value };
        setQuestions(newQuestions);
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        
        if (!title.trim()) {
            setError("Title is required");
            window.scrollTo(0, 0);
            return;
        }

        if (questions.length === 0) {
            setError("At least one question is required");
            window.scrollTo(0, 0);
            return;
        }

        const hasEmptyQuestion = questions.some((q) => !q.content.trim());
        if (hasEmptyQuestion) {
            setError("All questions must have content");
            window.scrollTo(0, 0);
            return;
        }

        try {
            setLoading(true);
            setError("");
            
            const data: CreateSurveyData = {
                title,
                description: description || null,
                startDate: startDate ? new Date(startDate).toISOString() : null,
                endDate: endDate ? new Date(endDate).toISOString() : null,
                questions
            };
            
            const response = await createSurvey(data);
            if (response.success) {
                // Navigate back to admin dashboard or surveys list
                // Since we don't have an admin dashboard yet, we can go to /surveys
                navigate("/surveys");
            } else {
                setError(response.message || "Failed to create survey");
            }
        } catch (err) {
            setError(getApiErrorMessage(err, "Failed to create survey. Please try again."));
            window.scrollTo(0, 0);
        } finally {
            setLoading(false);
        }
    }

    return (
        <>
            <MainHeader />

            <main className="min-h-screen bg-[linear-gradient(180deg,#f4f8ff_0%,#eef3f8_44%,#f7fafc_100%)]">
                <div className="mx-auto max-w-screen-md px-6 py-10">
                    <div className="mb-10">
                        <span className="mb-3 inline-flex rounded-full border border-indigo-200 bg-indigo-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-indigo-700">
                            Admin / Surveys
                        </span>
                        <h1 className="text-4xl font-extrabold tracking-tight text-slate-950">
                            Create new survey
                        </h1>
                        <p className="mt-4 text-base leading-7 text-slate-500">
                            Design a new feedback survey, attach questions, and define availability dates.
                        </p>
                    </div>

                    {error ? (
                        <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700 shadow-sm">
                            {error}
                        </div>
                    ) : null}

                    <form onSubmit={handleSubmit} className="space-y-8">
                        {/* Survey Details Section */}
                        <div className="rounded-[28px] border border-slate-200 bg-white p-6 md:p-8 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                            <h2 className="mb-6 text-xl font-bold text-slate-900 border-b border-slate-100 pb-4">
                                Survey Details
                            </h2>
                            <div className="grid gap-6">
                                <div>
                                    <label className="mb-2 block text-sm font-semibold text-slate-700">
                                        Title <span className="text-red-500">*</span>
                                    </label>
                                    <input
                                        type="text"
                                        value={title}
                                        onChange={(e) => setTitle(e.target.value)}
                                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-base outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
                                        placeholder="E.g., Mid-Term Feedback 2026"
                                    />
                                </div>
                                
                                <div>
                                    <label className="mb-2 block text-sm font-semibold text-slate-700">
                                        Description
                                    </label>
                                    <textarea
                                        value={description}
                                        onChange={(e) => setDescription(e.target.value)}
                                        rows={3}
                                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-base outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
                                        placeholder="Optional description about the survey..."
                                    />
                                </div>

                                <div className="grid gap-6 md:grid-cols-2">
                                    <div>
                                        <label className="mb-2 block text-sm font-semibold text-slate-700">
                                            Start Date
                                        </label>
                                        <input
                                            type="datetime-local"
                                            value={startDate}
                                            onChange={(e) => setStartDate(e.target.value)}
                                            className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-base outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
                                        />
                                    </div>
                                    <div>
                                        <label className="mb-2 block text-sm font-semibold text-slate-700">
                                            End Date
                                        </label>
                                        <input
                                            type="datetime-local"
                                            value={endDate}
                                            onChange={(e) => setEndDate(e.target.value)}
                                            className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3 text-base outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
                                        />
                                    </div>
                                </div>
                            </div>
                        </div>

                        {/* Questions Section */}
                        <div className="rounded-[28px] border border-slate-200 bg-white p-6 md:p-8 shadow-[0_18px_40px_rgba(15,23,42,0.05)]">
                            <div className="mb-6 flex flex-col md:flex-row md:items-center justify-between border-b border-slate-100 pb-4 gap-4">
                                <div>
                                    <h2 className="text-xl font-bold text-slate-900">Questions Form</h2>
                                    <p className="mt-1 text-sm text-slate-500">Design the questions for your survey.</p>
                                </div>
                                <button
                                    type="button"
                                    onClick={addQuestion}
                                    className="inline-flex items-center gap-2 rounded-xl bg-slate-100 px-4 py-2.5 text-sm font-bold text-slate-700 transition hover:bg-slate-200"
                                >
                                    <span className="material-symbols-outlined text-[18px]">add</span>
                                    Add Question
                                </button>
                            </div>

                            {questions.length === 0 ? (
                                <div className="rounded-2xl border border-dashed border-slate-300 bg-slate-50 py-12 text-center text-slate-500">
                                    <p className="text-sm">No questions added yet.</p>
                                    <p className="mt-1 text-sm text-slate-400">Click "Add Question" to start building your survey.</p>
                                </div>
                            ) : (
                                <div className="grid gap-4">
                                    {questions.map((question, index) => (
                                        <div key={index} className="group relative rounded-2xl border border-slate-200 bg-white p-5 pr-14 shadow-sm transition hover:border-slate-300">
                                            <div className="flex flex-col gap-4 md:flex-row">
                                                <div className="flex-1">
                                                    <label className="mb-2 block text-xs font-semibold uppercase tracking-wider text-slate-500">
                                                        Question {index + 1}
                                                    </label>
                                                    <input
                                                        type="text"
                                                        value={question.content}
                                                        onChange={(e) => updateQuestion(index, "content", e.target.value)}
                                                        className="w-full rounded-xl border border-slate-300 bg-slate-50 px-4 py-2.5 outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-2 focus:ring-indigo-500/10"
                                                        placeholder="What did you think of the course?"
                                                        required
                                                    />
                                                </div>
                                                <div className="w-full md:w-48">
                                                    <label className="mb-2 block text-xs font-semibold uppercase tracking-wider text-slate-500">
                                                        Type
                                                    </label>
                                                    <select
                                                        value={question.type}
                                                        onChange={(e) => updateQuestion(index, "type", e.target.value)}
                                                        className="w-full appearance-none rounded-xl border border-slate-300 bg-slate-50 px-4 py-2.5 outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-2 focus:ring-indigo-500/10"
                                                    >
                                                        <option value="RATING">Rating (1-5)</option>
                                                        <option value="TEXT">Free Text</option>
                                                    </select>
                                                </div>
                                            </div>
                                            <button
                                                type="button"
                                                onClick={() => removeQuestion(index)}
                                                className="absolute right-4 top-1/2 -translate-y-1/2 flex h-8 w-8 items-center justify-center rounded-full bg-red-50 text-red-500 transition hover:bg-red-100 hover:text-red-600"
                                                title="Remove Question"
                                            >
                                                <span className="material-symbols-outlined text-[20px]">delete</span>
                                            </button>
                                        </div>
                                    ))}
                                </div>
                            )}
                        </div>

                        {/* Submit */}
                        <div className="flex justify-end pt-4">
                            <button
                                type="submit"
                                disabled={loading}
                                className="inline-flex w-full md:w-auto items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#4f46e5_0%,#3b82f6_100%)] px-8 py-4 text-base font-bold text-white shadow-[0_16px_36px_rgba(79,70,229,0.22)] transition hover:translate-y-[-1px] hover:shadow-[0_18px_40px_rgba(79,70,229,0.28)] disabled:cursor-not-allowed disabled:opacity-60 disabled:shadow-none"
                            >
                                <span>{loading ? "Creating..." : "Launch Survey"}</span>
                                <span className="material-symbols-outlined text-[20px]">rocket_launch</span>
                            </button>
                        </div>
                    </form>
                </div>
            </main>

            <MainFooter />
        </>
    );
}
