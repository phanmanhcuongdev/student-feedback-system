import type { ReactNode } from "react";
import MainFooter from "../../../components/layout/MainFooter";
import AuthInfoPanel from "./AuthInfoPanel";

type AuthShellProps = {
    eyebrow: string;
    title: string;
    description: string;
    children: ReactNode;
    footer?: ReactNode;
};

export default function AuthShell({
    eyebrow,
    title,
    description,
    children,
    footer,
}: AuthShellProps) {
    return (
        <div className="min-h-screen bg-[linear-gradient(180deg,#f3f7ff_0%,#eef3f8_44%,#f7fafc_100%)] text-slate-900">
            <main className="mx-auto flex min-h-screen max-w-7xl items-center px-6 py-10 lg:px-10">
                <div className="grid w-full gap-8 lg:grid-cols-[1.12fr_0.88fr]">
                    <AuthInfoPanel />

                    <section className="rounded-[28px] border border-slate-200/70 bg-white/92 p-8 shadow-[0_24px_60px_rgba(15,23,42,0.08)] backdrop-blur lg:p-10">
                        <div className="mx-auto max-w-md">
                            <div className="mb-8 space-y-3">
                                <span className="inline-flex rounded-full bg-blue-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.24em] text-blue-700">
                                    {eyebrow}
                                </span>
                                <div className="space-y-2">
                                    <h2 className="text-3xl font-extrabold tracking-tight text-slate-950">
                                        {title}
                                    </h2>
                                    <p className="text-sm leading-6 text-slate-500">
                                        {description}
                                    </p>
                                </div>
                            </div>

                            {children}

                            {footer ? (
                                <div className="mt-8">{footer}</div>
                            ) : null}
                        </div>
                    </section>
                </div>
            </main>

            <MainFooter />
        </div>
    );
}
