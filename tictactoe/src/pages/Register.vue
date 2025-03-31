<template>
    <div class="register-container">
        <h1>Register</h1>
        <form @submit.prevent="register" class="register-form">
            <div class="form-group">
                <label for="username">Username</label>
                <input 
                    type="text" 
                    id="username" 
                    v-model="username" 
                    required 
                    class="form-control"
                />
                <span v-if="errors.username" class="error-message">{{ errors.username }}</span>
            </div>
            
            <button 
                type="submit" 
                class="btn-register" 
                :disabled="isLoading"
            >
                {{ isLoading ? 'Registering...' : 'Register' }}
            </button>
            
            <p class="login-link">
                Already have an account? <router-link to="/login">Login</router-link>
            </p>
        </form>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import axios, { AxiosError } from 'axios';

// Define interfaces
interface ErrorsState {
    username?: string;
    [key: string]: string | undefined;
}

interface RegisterResponse {
    success: boolean;
    message: string;
    userId?: string;
    // Add any other fields your API returns
}

const router = useRouter();
const username = ref<string>('');
const isLoading = ref<boolean>(false);
const errors = ref<ErrorsState>({});

const validate = (): boolean => {
    errors.value = {};
    
    if (username.value.length < 3) {
        errors.value.username = 'Username must be at least 3 characters';
    }
    
    return Object.keys(errors.value).length === 0;
};

const register = async (): Promise<void> => {
    if (validate()) {
        isLoading.value = true;
        try {
            // Create the data object to send
            const userData = {
                username: username.value,
            };
            
            // Send POST request to your API endpoint
            const response = await axios.post<RegisterResponse>(
                'http://localhost:8080/api/players',
                userData
            );
            
            console.log('Registration successful:', response.data);
            
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
</script>


<style scoped>
.register-container {
    max-width: 400px;
    margin: 40px auto;
    padding: 20px;
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h1 {
    text-align: center;
    color: #333;
    margin-bottom: 20px;
}

.form-group {
    margin-bottom: 15px;
    width: 100%;
}

label {
    display: block;
    margin-bottom: 5px;
    font-weight: 500;
}

.form-control {
    width: 100%;
    padding: 10px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 16px;
}

.error-message {
    color: #dc3545;
    font-size: 14px;
    margin-top: 4px;
    display: block;
}

.btn-register {
    width: 100%;
    padding: 10px;
    background-color: #4CAF50;
    color: white;
    border: none;
    border-radius: 4px;
    font-size: 16px;
    cursor: pointer;
    margin-top: 10px;
}

.btn-register:hover {
    background-color: #45a049;
}

.login-link {
    text-align: center;
    margin-top: 16px;
    font-size: 14px;
}

.login-link a {
    color: #4CAF50;
    text-decoration: none;
}

.login-link a:hover {
    text-decoration: underline;
}
</style>