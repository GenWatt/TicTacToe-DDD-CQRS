<template>
    <div class="login-container">
        <div class="login-card">
            <h1>Login</h1>
            <form @submit.prevent="login">
                <div class="form-group">
                    <label for="username">Username</label>
                    <input
                        type="text"
                        id="username"
                        v-model="usernameRef"
                        placeholder="Enter your username"
                        required
                    />
                </div>

                <p class="register-link">
                    Don't have an account? <router-link to="/register">Register</router-link>
                </p>
                <button type="submit" class="login-button" :disabled="isLoading">
                    {{ isLoading ? 'Logging in...' : 'Login' }}
                </button>
                <p v-if="error" class="error-message">{{ error }}</p>
            </form>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios, { AxiosError } from 'axios';
import { useAuthStore } from '../stores/useAuthStore';

interface LoginResponse {
    id: string;
    username: string;
}

const router = useRouter();
const authStore = useAuthStore();
const usernameRef = ref('');
const error = ref<string | null>(null);
const isLoading = ref(false);

const login = async (): Promise<void> => {
    // Reset error
    error.value = null;
    
    // Validate username
    if (!usernameRef.value.trim()) {
        error.value = 'Username is required';
        return;
    }
    
    isLoading.value = true;
    
    try {
        const response = await axios.post<LoginResponse>(
            'http://localhost:8080/api/players/login',
            { username: usernameRef.value },
        );
        
        const { data } = response;
        console.log('Response:', data);
        const { id, username } = data;

        authStore.setAuth(
            id,
            username
        );
    
        router.push('/');
        
    } catch (err) {
        const axiosError = err as AxiosError<{ message: string }>;
        console.error('Login failed:', axiosError);
        
        if (axiosError.response) {
            // Server responded with an error
            error.value = axiosError.response.data?.message || 'Invalid username. Please try again.';
        } else if (axiosError.request) {
            // No response received
            error.value = 'No response from server. Please try again later.';
        } else {
            // Other error
            error.value = 'Login failed. Please try again.';
        }
    } finally {
        isLoading.value = false;
    }
};
</script>

<style scoped>
.login-container {
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
}

.login-card {
    width: 100%;
    max-width: 400px;
    padding: 2rem;
    background: white;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h1 {
    text-align: center;
    margin-bottom: 1.5rem;
    color: #333;
}

.form-group {
    margin-bottom: 1rem;
}

label {
    display: block;
    margin-bottom: 0.5rem;
    font-weight: 500;
}

input {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 1rem;
}

.login-button {
    width: 100%;
    padding: 0.75rem;
    margin-top: 1rem;
    background-color: #4CAF50;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 1rem;
    cursor: pointer;
    transition: background-color 0.3s;
}

.login-button:hover {
    background-color: #45a049;
}

.error-message {
    color: #f44336;
    margin-top: 1rem;
    text-align: center;
}
</style>