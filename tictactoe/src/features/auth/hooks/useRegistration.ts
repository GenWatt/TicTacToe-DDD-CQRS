import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { AxiosError } from 'axios';
import { RegisterUserDto, authApi } from '../api/authApi';

interface ErrorsState {
    username?: string;
    password?: string;
    [key: string]: string | undefined;
}

export function useRegistration() {
    const router = useRouter();
    const username = ref<string>('');
    const password = ref<string>('');
    const isLoading = ref<boolean>(false);
    const errors = ref<ErrorsState>({});

    const validate = (): boolean => {
        errors.value = {};

        if (username.value.length < 3) {
            errors.value.username = 'Username must be at least 3 characters';
        }

        if (password.value.length < 6) {
            errors.value.password = 'Password must be at least 6 characters';
        }

        return Object.keys(errors.value).length === 0;
    };

    const register = async (): Promise<void> => {
        if (validate()) {
            isLoading.value = true;
            try {
                const userData: RegisterUserDto = {
                    username: username.value,
                    password: password.value,
                };

                const response = await authApi.register(userData);
                console.log('Registration successful:', response);

                // Handle successful registration
                alert('Registration successful!');
                router.push('/login');
            } catch (error) {
                // Handle errors with proper TypeScript typing
                const axiosError = error as AxiosError<{ message: string }>;
                console.error('Registration failed:', axiosError);

                if (axiosError.response) {
                    // The server responded with a status code outside the 2xx range
                    const errorMsg = axiosError.response.data?.message || 'Unknown server error';
                    alert(`Registration failed: ${errorMsg}`);
                } else if (axiosError.request) {
                    // The request was made but no response was received
                    alert('Registration failed: No response from server. Please try again later.');
                } else {
                    // Something happened in setting up the request
                    alert('Registration failed. Please try again.');
                }
            } finally {
                isLoading.value = false;
            }
        }
    };

    return {
        username,
        password,
        isLoading,
        errors,
        register
    };
}