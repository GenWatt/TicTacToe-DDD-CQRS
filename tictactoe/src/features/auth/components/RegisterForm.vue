<template>
  <form @submit.prevent="register" class="register-form">
    <div class="form-group">
      <label for="username">Username</label>
      <input 
        type="text" 
        id="username" 
        v-model="username" 
        required 
        class="form-control"
        autocomplete="username"
      />
      <span v-if="errors.username" class="error-message">{{ errors.username }}</span>
    </div>
    
    <div class="form-group">
      <label for="password">Password</label>
      <input 
        type="password" 
        id="password" 
        v-model="password" 
        required 
        class="form-control" 
        autocomplete="new-password"
      />
      <span v-if="errors.password" class="error-message">{{ errors.password }}</span>
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
</template>

<script setup lang="ts">
import { useRegistration } from '../hooks/useRegistration';

const { username, password, isLoading, errors, register } = useRegistration();
</script>

<style scoped>
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