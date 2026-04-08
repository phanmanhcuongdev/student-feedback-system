export default function SurveyHero() {
    return (
        <div className="max-w-2xl">
            <span className="mb-3 inline-flex rounded-full border border-blue-200 bg-blue-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-blue-700">
                Student Workspace
            </span>
            <h1 className="text-4xl font-extrabold tracking-tight text-slate-950 md:text-5xl">
                Available surveys
            </h1>
            <p className="mt-4 max-w-xl text-base leading-7 text-slate-500">
                Review the surveys assigned to your account, track their current status, and complete open forms
                through a single authenticated workflow.
            </p>
        </div>
    );
}
