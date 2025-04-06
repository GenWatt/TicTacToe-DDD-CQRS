<template>
  <div class="register-container">
    <h1>Register</h1>
    <RegisterForm />
  </div>
</template>

<script setup lang="ts">
import RegisterForm from '../features/auth/components/RegisterForm.vue';
</script>

<style scoped>
.register-container {
  max-width: 400px;
  margin: 40px auto;
  padding: 20px;
  background-color: #f9f9f920;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

h1 {
  text-align: center;
  margin-bottom: 20px;
}
</style>