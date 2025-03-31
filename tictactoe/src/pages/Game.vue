<template>
    <div class="game-container">
        <h1>Game</h1>
        <div class="game-content">
            <p v-if="username">Welcome to the game! <b>{{ username }}</b></p>

            <TicTacToe />
        </div>

        <button @click="logout" class="logout-button">
            Logout
        </button>
    </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/useAuthStore';
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';
import TicTacToe from '../components/TicTacToe.vue';
import { onMounted } from 'vue';

const router = useRouter();
const authStore = useAuthStore();
// Using storeToRefs makes the properties reactive
const { username } = storeToRefs(authStore);

const logout = () => {
  authStore.clearAuth();
  router.push('/login');
};

onMounted(() => {
    // If already authenticated, redirect to game
    if (authStore.isAuthenticated) {
        router.push('/');
    }
});
</script>