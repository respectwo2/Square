import axiosInstance from "./axiosInstance";
import {useAuthStore} from "../stores/auth"

export const setupAuthInterceptor = () => {
    axiosInstance.interceptors.request.use(
        async (config) => {
            const {user} = useAuthStore.getState();

            console.log("[Axios Interceptor] setupAuthInterceptor 호출됨. 현재 유저 정보:", user);

            if(user?.token){
                config.headers.Authorization = `Bearer ${user.token}`;
            }
            return config;
        },
        (error) => Promise.reject(error)
    );
};
