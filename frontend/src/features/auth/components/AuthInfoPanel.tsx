const highlights = [
    "Create an account with your student information",
    "Verify your email and upload required documents",
    "Log in after approval to access assigned surveys",
];

export default function AuthInfoPanel() {
    return (
        <section className="relative overflow-hidden rounded-[28px] border border-white/60 bg-[linear-gradient(160deg,#0b1c30_0%,#123e6d_52%,#1d6ed6_100%)] p-8 text-white shadow-[0_30px_80px_rgba(8,23,44,0.28)] lg:p-10">
            <div className="absolute inset-0 bg-[radial-gradient(circle_at_top_right,rgba(255,255,255,0.22),transparent_34%),radial-gradient(circle_at_bottom_left,rgba(255,255,255,0.14),transparent_28%)]" />

            <div className="relative space-y-8">
                <div className="space-y-4">
                    <span className="inline-flex rounded-full border border-white/20 bg-white/10 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.28em] text-blue-100">
                        Student Feedback System
                    </span>

                    <div className="space-y-3">
                        <h1 className="max-w-md text-4xl font-extrabold tracking-tight text-white md:text-5xl">
                            Complete onboarding, then enter the feedback workspace.
                        </h1>
                        <p className="max-w-xl text-base leading-7 text-blue-50/88">
                            The portal now supports the full student entry path: registration, email confirmation,
                            document upload, approval waiting, and sign-in to the survey area.
                        </p>
                    </div>
                </div>

                <div className="grid gap-3">
                    {highlights.map((item) => (
                        <div
                            key={item}
                            className="flex items-center gap-3 rounded-2xl border border-white/12 bg-white/8 px-4 py-3 backdrop-blur-sm"
                        >
                            <span className="material-symbols-outlined text-xl text-blue-100">check_circle</span>
                            <span className="text-sm font-medium text-white/92">{item}</span>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
}
