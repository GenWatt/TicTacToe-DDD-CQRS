<template>
  <form @submit.prevent="login" class="login-form">
    <div class="form-group">
      <label for="username">Username</label>
      <input
        type="text"
        id="username"
        v-model="username"
        placeholder="Enter your username"
        required
        autocomplete="username"
      />
    </div>

    <div class="form-group">
      <label for="password">Password</label>
      <input
        type="password"
        id="password"
        v-model="password"
        placeholder="Enter your password"
        required
        autocomplete="current-password"
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
</template>

<script setup lang="ts">
import { useLogin } from '../hooks/useLogin';

const { username, password, error, isLoading, login } = useLogin();
</script>

<style scoped>
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

.register-link {
  text-align: center;
  margin-top: 1rem;
  font-size: 14px;
}

.register-link a {
  color: #4CAF50;
  text-decoration: none;
}

.register-link a:hover {
  text-decoration: underline;
}
</style>