import axios, { AxiosHeaders } from "axios";
import { clearStoredSession, readStoredSession } from "../features/auth/authStorage";

const instance = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || "/api",
});

instance.interceptors.request.use((config) => {
    const session = readStoredSession();

    if (session?.accessToken) {
        config.headers = AxiosHeaders.from(config.headers);
        config.headers.set("Authorization", `Bearer ${session.accessToken}`);
    }

    return config;
});

instance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401) {
            clearStoredSession();
            window.dispatchEvent(new Event("auth:session-cleared"));

            if (window.location.pathname !== "/login") {
                window.location.assign("/login");
            }
        }

        return Promise.reject(error);
    }
);

export default instance;
