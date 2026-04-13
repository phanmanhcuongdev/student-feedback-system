import { useState } from "react";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { getApiErrorMessage } from "../../../api/apiError";
import AuthShell from "../components/AuthShell";
import { getDefaultAppRoute } from "../defaultRoute";
import { useAuth } from "../useAuth";

type LocationState = {
    from?: string;
    notice?: string;
};

function getLoginErrorMessage(code: string, fallback: string): string {
    switch (code) {
        case "ACCOUNT_PENDING":
            return "Your account is pending review and cannot sign in yet.";
        case "ACCOUNT_REJECTED":
            return "Your account was rejected. Contact an administrator for the next steps.";
        case "ACCOUNT_INACTIVE":
            return "Your account is inactive. Complete verification or contact an administrator.";
        default:
            return fallback;
    }
}

export default function LoginPage() {
    const navigate = useNavigate();
    const location = useLocation();
    const { login } = useAuth();
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [submitting, setSubmitting] = useState(false);

    const targetPath = (location.state as LocationState | null)?.from;
    const notice = (location.state as LocationState | null)?.notice;

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        setSubmitting(true);
        setError("");

        try {
            const response = await login({
                email: email.trim(),
                password,
            });

            if (!response.success) {
                setError(getLoginErrorMessage(response.code, response.message || "Login failed"));
                return;
            }

            navigate(targetPath ?? getDefaultAppRoute(response.role, response.studentStatus), { replace: true });
        } catch (requestError) {
            setError(getApiErrorMessage(requestError, "Unable to sign in right now."));
        } finally {
            setSubmitting(false);
        }
    }

    return (
        <AuthShell
            eyebrow="Secure Login"
            title="Welcome back"
            description="Sign in with your registered account to access the survey workspace."
            footer={
                <div className="space-y-2 text-sm text-slate-500">
                    <p>
                        New student?
                        {" "}
                        <Link className="font-semibold text-blue-700 hover:text-blue-800" to="/register">
                            Create an account
                        </Link>
                    </p>
                    <p>
                        Forgot your password?
                        {" "}
                        <Link className="font-semibold text-blue-700 hover:text-blue-800" to="/forgot-password">
                            Reset it here
                        </Link>
                    </p>
                </div>
            }
        >
            <form className="space-y-5" onSubmit={handleSubmit}>
                {notice && (
                    <div className="rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
                        {notice}
                    </div>
                )}

                <label className="block space-y-2">
                    <span className="text-sm font-semibold text-slate-700">Email</span>
                    <input
                        type="email"
                        value={email}
                        onChange={(event) => setEmail(event.target.value)}
                        placeholder="student@example.com"
                        autoComplete="email"
                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3.5 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                        required
                    />
                </label>

                <label className="block space-y-2">
                    <span className="text-sm font-semibold text-slate-700">Password</span>
                    <input
                        type="password"
                        value={password}
                        onChange={(event) => setPassword(event.target.value)}
                        placeholder="Enter your password"
                        autoComplete="current-password"
                        className="w-full rounded-2xl border border-slate-300 bg-slate-50 px-4 py-3.5 text-sm text-slate-900 outline-none transition focus:border-blue-500 focus:bg-white focus:ring-4 focus:ring-blue-100"
                        required
                    />
                </label>

                {error && (
                    <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700">
                        {error}
                    </div>
                )}

                <button
                    type="submit"
                    disabled={submitting}
                    className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-[linear-gradient(135deg,#0f5bcf_0%,#1d78ec_100%)] px-5 py-3.5 text-sm font-bold text-white shadow-[0_16px_36px_rgba(29,120,236,0.28)] transition hover:translate-y-[-1px] hover:shadow-[0_20px_44px_rgba(29,120,236,0.32)] disabled:cursor-not-allowed disabled:opacity-65 disabled:shadow-none"
                >
                    <span>{submitting ? "Signing in..." : "Sign In"}</span>
                    <span className="material-symbols-outlined text-base">login</span>
                </button>
            </form>
        </AuthShell>
    );
}
