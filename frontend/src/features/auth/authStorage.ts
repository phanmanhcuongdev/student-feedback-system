import type { AuthSession } from "../../types/auth";

const AUTH_STORAGE_KEY = "student-feedback.auth-session";

function isAllowedStudentSession(session: AuthSession): boolean {
    if (session.role !== "STUDENT") {
        return true;
    }

    return session.studentStatus === "ACTIVE"
        || session.studentStatus === "EMAIL_VERIFIED"
        || session.studentStatus === "REJECTED";
}

function decodeBase64Url(value: string): string | null {
    try {
        const normalized = value.replace(/-/g, "+").replace(/_/g, "/");
        const padding = normalized.length % 4;
        const padded = padding === 0 ? normalized : normalized + "=".repeat(4 - padding);
        return atob(padded);
    } catch {
        return null;
    }
}

function isTokenExpired(token: string): boolean {
    const [, payload] = token.split(".");
    if (!payload) {
        return true;
    }

    const decoded = decodeBase64Url(payload);
    if (!decoded) {
        return true;
    }

    try {
        const parsed = JSON.parse(decoded) as { exp?: number };
        if (typeof parsed.exp !== "number") {
            return false;
        }

        return parsed.exp * 1000 <= Date.now();
    } catch {
        return true;
    }
}

export function readStoredSession(): AuthSession | null {
    const raw = window.localStorage.getItem(AUTH_STORAGE_KEY);
    if (!raw) {
        return null;
    }

    try {
        const parsed = JSON.parse(raw) as AuthSession;
        if (!parsed.accessToken || isTokenExpired(parsed.accessToken) || !isAllowedStudentSession(parsed)) {
            clearStoredSession();
            return null;
        }

        return parsed;
    } catch {
        clearStoredSession();
        return null;
    }
}

export function storeSession(session: AuthSession): void {
    window.localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(session));
}

export function clearStoredSession(): void {
    window.localStorage.removeItem(AUTH_STORAGE_KEY);
}
