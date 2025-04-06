<template>
    <div class="game-container">
        <div class="game-content">
            <h1>Tic tac toe</h1>
            <p v-if="username">Welcome to the game! <b class="game-username">{{ username }}</b></p>

            <TicTacToe />
        </div>

        <Button @click="logout">
            Logout
        </Button>
    </div>
</template>

<script setup lang="ts">
import { useAuthStore } from '../stores/useAuthStore';
import { storeToRefs } from 'pinia';
import { useRouter } from 'vue-router';
import TicTacToe from '../features/game/components/TicTacToe.vue';
import { onMounted } from 'vue';
import { webSocketService } from '../gameWebsocketService';
import Button from '../components/UI/Button.vue';

const router = useRouter();
const authStore = useAuthStore();

const { username } = storeToRefs(authStore);

const logout = () => {
  authStore.clearAuth();
    webSocketService.disconnect();
  router.push('/login');
};

onMounted(() => {
    if (authStore.isAuthenticated) {
        router.push('/');
    }
});
</script>

<style scoped>
.game-container {
    display: flex;
    flex-direction: column;
    width: 100%;
    max-width: 500px;
    margin: 0 auto;
}
.game-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    padding: 20px;
    background-color: #f9f9f920;
    border-radius: 8px;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
    height: 100%;
}
.game-username {
    font-weight: bold;
    color: #4CAF50;
}
.game-username:hover {
    text-decoration: underline;
}
.game-username:active {
    color: #388E3C;
}
</style>