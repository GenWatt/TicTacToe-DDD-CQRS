import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { AxiosError } from 'axios';
import { useAuthStore } from '../../../stores/useAuthStore';
import { authApi } from '../api/authApi';

export function useLogin() {
    const router = useRouter();
    const authStore = useAuthStore();
    const username = ref('');
    const password = ref('');
    const error = ref<string | null>(null);
    const isLoading = ref(false);

    const login = async (): Promise<void> => {
        error.value = null;

        if (!username.value.trim()) {
            error.value = 'Username is required';
            return;
        }

        if (!password.value.trim()) {
            error.value = 'Password is required';
            return;
        }

        isLoading.value = true;

        try {
            const data = await authApi.login(username.value, password.value);
            console.log('Response:', data);

            authStore.setAuth(
                data.id,
                data.username,
                data.token
            );

            router.push('/');

        } catch (err) {
            const axiosError = err as AxiosError<{ message: string }>;
            console.error('Login failed:', axiosError);

            if (axiosError.response) {
                error.value = axiosError.response.data?.message || 'Invalid username or password. Please try again.';
            } else if (axiosError.request) {
                error.value = 'No response from server. Please try again later.';
            } else {
                error.value = 'Login failed. Please try again.';
            }
        } finally {
            isLoading.value = false;
        }
    };

    return {
        username,
        password,
        error,
        isLoading,
        login
    };
}