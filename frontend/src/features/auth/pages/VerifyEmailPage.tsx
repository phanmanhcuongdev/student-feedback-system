import { useEffect, useState } from "react";
import { Link, useNavigate, useSearchParams } from "react-router-dom";
import { getApiErrorMessage } from "../../../api/apiError";
import { verifyEmail } from "../../../api/authApi";
import type { VerifyEmailResponse } from "../../../types/auth";
import AuthShell from "../components/AuthShell";

type VerificationState =
    | { status: "loading"; result: null; error: "" }
    | { status: "success"; result: VerifyEmailResponse; error: "" }
    | { status: "error"; result: null; error: string };

const verificationRequests = new Map<string, Promise<VerifyEmailResponse>>();
const verificationResults = new Map<string, VerifyEmailResponse>();

export default function VerifyEmailPage() {
    const navigate = useNavigate();
    const [searchParams] = useSearchParams();
    const token = searchParams.get("token");
    const [verificationState, setVerificationState] = useState<VerificationState>({
        status: "loading",
        result: null,
        error: "",
    });

    useEffect(() => {
        let cancelled = false;

        async function runVerification() {
            if (!token) {
                setVerificationState({
                    status: "error",
                    result: null,
                    error: "Missing verification token.",
                });
                return;
            }

            if (verificationResults.has(token)) {
                const cachedResult = verificationResults.get(token)!;
                setVerificationState(
                    cachedResult.success
                        ? { status: "success", result: cachedResult, error: "" }
                        : {
                            status: "error",
                            result: null,
                            error: cachedResult.message || "Email verification failed.",
                        }
                );
                return;
            }

            setVerificationState({
                status: "loading",
                result: null,
                error: "",
            });

            try {
                let request = verificationRequests.get(token);
                if (!request) {
                    request = verifyEmail(token);
                    verificationRequests.set(token, request);
                }

                const response = await request;
                verificationResults.set(token, response);
                verificationRequests.delete(token);

                if (cancelled) {
                    return;
                }

                setVerificationState(
                    response.success
                        ? { status: "success", result: response, error: "" }
                        : {
                            status: "error",
                            result: null,
                            error: response.message || "Email verification failed.",
                        }
                );
            } catch (requestError) {
                const message = getApiErrorMessage(requestError, "Unable to verify email right now.");
                verificationRequests.delete(token);

                if (!cancelled) {
                    setVerificationState({
                        status: "error",
                        result: null,
                        error: message,
                    });
                }
            }
        }

        runVerification();

        return () => {
            cancelled = true;
        };
    }, [token]);

    function continueToLogin() {
        navigate("/login");
    }

    return (
        <AuthShell
            eyebrow="Email Verification"
            title="Verify your student account"
            description="We confirm your email before allowing you to sign in and upload your verification documents."
            footer={
                <p className="text-sm text-slate-500">
                    Back to
                    {" "}
                    <Link className="font-semibold text-blue-700 hover:text-blue-800" to="/login">
                        login
                    </Link>
                </p>
            }
        >
            <div className="space-y-4">
                {verificationState.status === "loading" ? (
                    <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4 text-sm font-medium text-slate-600">
                        Verifying your email...
                    </div>
                ) : null}

                {verificationState.status === "error" ? (
                    <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-4 text-sm font-medium text-red-700">
                        {verificationState.error}
                    </div>
                ) : null}

                {verificationState.status === "success" ? (
                    <div className="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-4 text-sm font-medium text-emerald-700">
                        {verificationState.result.message}
                    </div>
                ) : null}

                {verificationState.status === "success" ? (
                    <div className="rounded-2xl border border-slate-200 bg-slate-50 px-4 py-4 text-sm text-slate-500">
                        Email verification is complete. Your next step is to sign in, upload your student card image and national ID image, then wait for administrator approval.
                    </div>
                ) : null}

                <button
                    type="button"
                    onClick={continueToLogin}
                    disabled={verificationState.status !== "success"}
                    className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#0f5bcf_0%,#1d78ec_100%)] px-5 py-3.5 text-sm font-bold text-white shadow-[0_16px_36px_rgba(29,120,236,0.28)] transition hover:translate-y-[-1px] hover:shadow-[0_20px_44px_rgba(29,120,236,0.32)] disabled:cursor-not-allowed disabled:opacity-60 disabled:shadow-none"
                >
                    <span>Continue to login</span>
                    <span className="material-symbols-outlined text-base">login</span>
                </button>
            </div>
        </AuthShell>
    );
}
