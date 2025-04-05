import axios, { AxiosError } from 'axios';

const BASE_URL = 'http://localhost:8080/api';

export interface RegisterUserDto {
    username: string;
    password: string;
}

export interface RegisterResponse {
    success: boolean;
    message: string;
    userId?: string;
}

export interface LoginResponse {
    id: string;
    username: string;
    token: string;
}

export const authApi = {
    register: async (userData: RegisterUserDto): Promise<RegisterResponse> => {
        try {
            const response = await axios.post<RegisterResponse>(
                `${BASE_URL}/players`,
                userData
            );
            return response.data;
        } catch (error) {
            throw error as AxiosError<{ message: string }>;
        }
    },

    login: async (username: string, password: string): Promise<LoginResponse> => {
        try {
            const response = await axios.post<LoginResponse>(
                `${BASE_URL}/players/login`,
                { username, password }
            );
            return response.data;
        } catch (error) {
            throw error as AxiosError<{ message: string }>;
        }
    }
};