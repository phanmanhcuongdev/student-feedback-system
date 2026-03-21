import axios from "./axios";

export const login = async (username: string, password: string) => {
    const res = await axios.post("/auth/login", {
        username,
        password,
    });

    return res.data;
};