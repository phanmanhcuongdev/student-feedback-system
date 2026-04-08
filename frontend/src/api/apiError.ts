import axios from "axios";

type ErrorPayload = {
    code?: string;
    message?: string;
};

export function getApiErrorMessage(error: unknown, fallback: string): string {
    if (axios.isAxiosError(error)) {
        const message = error.response?.data?.message;
        if (typeof message === "string" && message.trim().length > 0) {
            return message;
        }
    }

    if (error instanceof Error && error.message.trim().length > 0) {
        return error.message;
    }

    return fallback;
}

export function getApiErrorPayload(error: unknown): ErrorPayload | null {
    if (!axios.isAxiosError(error)) {
        return null;
    }

    const data = error.response?.data;
    if (!data || typeof data !== "object") {
        return null;
    }

    const payload = data as ErrorPayload;
    const hasCode = typeof payload.code === "string" && payload.code.trim().length > 0;
    const hasMessage = typeof payload.message === "string" && payload.message.trim().length > 0;

    if (!hasCode && !hasMessage) {
        return null;
    }

    return payload;
}
