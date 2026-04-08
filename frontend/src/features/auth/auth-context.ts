import { createContext } from "react";
import type { AuthSession, LoginResponse } from "../../types/auth";

export type LoginInput = {
    email: string;
    password: string;
};

export type AuthContextValue = {
    session: AuthSession | null;
    isAuthenticated: boolean;
    login: (input: LoginInput) => Promise<LoginResponse>;
    logout: () => void;
};

export const AuthContext = createContext<AuthContextValue | null>(null);
