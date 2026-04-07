import axios from "./axios";

export const login = async (email: string, password: string) => {
    const res = await axios.post("/auth/login", {
        email,
        password,
    });

    return res.data;
};

export type LoginResponse = {
    userId: number;
    role: string;
    studentStatus: string | null;
    message: string;
    token?: string; // de san cho JWT sau nay
};