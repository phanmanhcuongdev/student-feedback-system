import { useEffect, useMemo, useState, type ReactNode } from "react";
import { login as loginRequest } from "../../api/authApi";
import type { AuthSession, LoginResponse } from "../../types/auth";
import { clearStoredSession, readStoredSession, storeSession } from "./authStorage";
import { AuthContext, type AuthContextValue, type LoginInput } from "./auth-context";

function canPersistSession(role: string | null, studentStatus: string | null): boolean {
    if (role !== "STUDENT") {
        return true;
    }

    return studentStatus === "ACTIVE" || studentStatus === "EMAIL_VERIFIED";
}

export function AuthProvider({ children }: { children: ReactNode }) {
    const [session, setSession] = useState<AuthSession | null>(() => readStoredSession());

    useEffect(() => {
        function syncSession() {
            setSession(readStoredSession());
        }

        window.addEventListener("storage", syncSession);
        window.addEventListener("auth:session-cleared", syncSession);

        return () => {
            window.removeEventListener("storage", syncSession);
            window.removeEventListener("auth:session-cleared", syncSession);
        };
    }, []);

    async function login(input: LoginInput): Promise<LoginResponse> {
        const response = await loginRequest(input.email, input.password);

        if (
            response.success
            && response.userId !== null
            && response.role
            && response.accessToken
            && canPersistSession(response.role, response.studentStatus)
        ) {
            const nextSession: AuthSession = {
                userId: response.userId,
                email: input.email,
                role: response.role,
                studentStatus: response.studentStatus,
                accessToken: response.accessToken,
            };

            storeSession(nextSession);
            setSession(nextSession);
        }

        return response;
    }

    function logout() {
        clearStoredSession();
        setSession(null);
    }

    const value = useMemo<AuthContextValue>(() => ({
        session,
        isAuthenticated: session !== null,
        login,
        logout,
    }), [session]);

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
