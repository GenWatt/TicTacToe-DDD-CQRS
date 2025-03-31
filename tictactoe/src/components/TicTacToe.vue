<template>
  <div class="game-status">{{ displayMessage }}</div>

  <Board v-if="gameActive" />

  <div class="game-controls">
    <button @click="handleMatchmaking" class="btn-matchmaking" :disabled="isConnecting">
      {{ matchmakingButtonText }}
    </button>

    <p class="connection-status">
      {{ connectedText }}
    </p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, watch, computed } from 'vue';
import { useWebSocket, UseWebSocketOptions } from '../hooks/useWebSockets';
import { useAuthStore } from '../stores/useAuthStore';
import { WebSocketMessageType, WebSocketMatchFoundMessage, WebSocketGenericMessage, WebSocketPlayMoveMessage, WebSocketPlayMovePayload, GameEndedPayload } from '../types';
import { useGameStore } from '../stores/useGameStore';
import Board from './Board.vue';

const gameStore = useGameStore();
const { playerId, username } = useAuthStore();

const options: UseWebSocketOptions = {
    url: 'ws://localhost:8080/ws/game',
    playerId,
    onOpen: (event) => {
        console.log('WebSocket connection opened:', event);
    },
    onMessage: (event: MessageEvent<string>) => {
        console.log('WebSocket message received:', event.data);

        try {
            console.log('Raw message:', event);
            const genericMessage = JSON.parse(event.data) as WebSocketGenericMessage;
            
            console.log('Message type:', genericMessage.payload);
            const typeToEnum = WebSocketMessageType[genericMessage.type as keyof typeof WebSocketMessageType]; 
            
            switch (typeToEnum) {
                case WebSocketMessageType.IN_QUEUE:
                    displayMessage.value = 'You are in the queue for matchmaking.';
                    break;
                case WebSocketMessageType.MATCH_FOUND:
                    const parsedPayload = JSON.parse(genericMessage.payload);
                    const matchFoundMessage = {
                        type: WebSocketMessageType.MATCH_FOUND,
                        payload: parsedPayload
                    } as WebSocketMatchFoundMessage;

                    console.log('Match found:', matchFoundMessage);
                    gameStore.setGame(matchFoundMessage);
                    displayMessage.value = `Match found! Your opponent is ${gameStore.getOpponent()?.username}`;
                    
                    // Add turn information
                    if (gameStore.isYourTurn()) {
                        displayMessage.value += ". It's your turn!";
                    } else {
                        displayMessage.value += ". Waiting for opponent's move...";
                    }
                    break;
                case WebSocketMessageType.ALREADY_IN_QUEUE:
                    displayMessage.value = 'You are already in the matchmaking queue.';
                    break;
                case WebSocketMessageType.PLAY_MOVE:
                    const movePayload = JSON.parse(genericMessage.payload) as WebSocketPlayMovePayload;
                    console.log('Opponent move:', movePayload);
                    gameStore.playMove(movePayload);
                    break;
                case WebSocketMessageType.GAME_ENDED:
                    const gameOverMessage = JSON.parse(genericMessage.payload) as GameEndedPayload;
                    console.log('Game over:', gameOverMessage);
                    // gameStore.pl;
                    displayMessage.value = `Game over! ${!gameOverMessage.winner.id  ? 'Draw!' : gameOverMessage.winner.id === playerId ? 'You win!' : 'You lose!'}`;
                    break;
                default:
                    console.log('Unknown message type:', genericMessage.type);
            }
        } catch (error) {
            console.error('Error parsing WebSocket message:', error);
        }
    },
    onError: (event) => {
        console.error('WebSocket error:', event);
    },
    onClose: (event) => {
        console.log('WebSocket connection closed:', event);
    },
};

const { connect, sendMessage, isConnected, isConnecting } = useWebSocket(options);

const displayMessage = ref<string>('');
const connectedText = ref<string>('Not connected');
const inQueue = ref<boolean>(false);

const gameActive = computed(() => {
    return gameStore.board !== null;
});

const matchmakingButtonText = computed(() => {
    if (isConnecting.value) return 'Connecting...';
    if (inQueue.value) return 'Looking for opponent...';
    if (gameActive.value) return 'In Game';
    return `Find Match (${username})`;
});

watch(isConnected, (newValue) => {
    console.log('isConnected:', newValue);
    connectedText.value = newValue ? 'Connected to server' : 'Not connected to server';
});

const handleMatchmaking = () => {
    if (!isConnected.value || gameActive.value) return;
    
    inQueue.value = true;
    
    const message = {
        type: 'JOIN_MATCHMAKING',
        playerId
    };

    sendMessage(JSON.stringify(message));
};

onMounted(() => {
    connect();
    gameStore.resetGame();
});
</script>

<style scoped>
.game-status {
    margin: 1rem 0;
    padding: 0.5rem;
    background-color: rgba(255, 255, 255, 0.1);
    border-radius: 4px;
    text-align: center;
    min-height: 2.5rem;
}

.game-controls {
    margin-top: 1.5rem;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.btn-matchmaking {
    padding: 0.75rem 1.5rem;
    background-color: #4CAF50;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
    font-size: 1rem;
    transition: background-color 0.3s;
}

.btn-matchmaking:hover:not(:disabled) {
    background-color: #45a049;
}

.btn-matchmaking:disabled {
    background-color: #cccccc;
    cursor: not-allowed;
}

.connection-status {
    margin-top: 0.5rem;
    font-size: 0.9rem;
    color: #999;
}
</style>