<template>
  <div class="game-container">
    <div class="game-status">{{ viewModel.displayMessage }}</div>
    
    <div v-if="viewModel.errorMessage.value" class="error-message">
      {{ viewModel.errorMessage }}
    </div>

    <Board v-if="viewModel.gameActive.value" />

    <div class="game-controls">
      <Button 
        @click="viewModel.handleMatchmaking" 
        class="btn-matchmaking" 
        :disabled="viewModel.isConnecting.value || viewModel.gameActive.value || !viewModel.isConnected.value">
        {{ viewModel.matchmakingButtonText }}
      </Button>

      <Button 
        @click="viewModel.connect" 
        class="btn-matchmaking" 
        :disabled="viewModel.isConnecting.value || viewModel.isConnected.value">
        {{ viewModel.reconnectButtonText }}
      </Button>

      <Button 
        @click="viewModel.disconnect" 
        class="btn-matchmaking" 
        :disabled="viewModel.isConnecting.value || !viewModel.isConnected.value">
        {{ viewModel.disconnectButtonText }}
      </Button>
      <p class="connection-status">
        {{ viewModel.connectedText }}
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import {  onMounted } from 'vue';
import Board from './Board.vue';
import {  useGameViewModel } from '../features/game/viewModels/GameViewModel';
import Button from './UI/Button.vue';

const viewModel = useGameViewModel();

onMounted(() => {
  viewModel.initialize();
});

</script>

<style scoped>
.game-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
}

.game-status {
  margin: 1rem 0;
  padding: 0.5rem;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
  text-align: center;
  min-height: 2.5rem;
}

.error-message {
  margin: 0.5rem 0;
  padding: 0.5rem;
  background-color: rgba(255, 0, 0, 0.2);
  color: #ff6b6b;
  border-radius: 4px;
  text-align: center;
}

.game-controls {
  margin-top: 1.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.connection-status {
  margin-top: 0.5rem;
  font-size: 0.9rem;
  color: #999;
}
</style>