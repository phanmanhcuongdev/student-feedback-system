import axios from "axios";

const instance = axios.create({
    // dev baseURL: "http://localhost:8080/api",
    baseURL: "/api",
});

instance.interceptors.request.use((config) => {
    const token = localStorage.getItem("token");

    if(token){
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

export default instance;