<template>
    <div class="game-info" v-if="board">
        <div class="players">
            <div class="player" :class="{ 'active': isYourTurn }">
                You ({{ player?.playerType }}): {{ player?.username }}
            </div>
            <div class="player" :class="{ 'active': !isYourTurn }">
                Opponent ({{ opponent?.playerType }}): {{ opponent?.username }}
            </div>
        </div>
    </div>

    <div v-if="board" class="board">
        <div v-for="i in board.board.length" :key="i" class="row">
            <div 
                v-for="j in board.board.length" 
                :key="`${i-1}-${j-1}`" 
                class="cell"
                :data-index="`${i-1}-${j-1}`"
                :class="{ 
                    'clickable': isYourTurn && isEmpty(i-1, j-1),
                    'cell-x': getCellContent(i-1, j-1) === 'X',
                    'cell-o': getCellContent(i-1, j-1) === 'O'
                }"
                @click="handleCellClick(i-1, j-1)"
            >
                <div class="cell-content">
                    {{ getCellContent(i-1, j-1) }}
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { storeToRefs } from 'pinia';
import { useGameStore } from '../stores/useGameStore';
import { useAuthStore } from '../stores/useAuthStore';
import { ref } from 'vue';
import { webSocketService } from '../gameWebsocketService';

const gameStore = useGameStore();
const authStore = useAuthStore();
const pendingMove = ref<{row: number, col: number} | null>(null);

const { board, player, opponent, yourTurn: isYourTurn } = storeToRefs(gameStore);

const getCellContent = (row: number, col: number): string => {
    if (!board.value || !board.value.board[row] || board.value.board[row][col] === null) {
        return '';
    }

    const value = board.value.board[row][col]?.id;
    console.log('getCellContent', value, player.value, opponent.value);
    if (value === player.value?.playerId) {
        return player.value.playerType;
    } else if (value === opponent.value?.playerId) {
        return opponent.value.playerType;
    }

    return '';
};

const isEmpty = (row: number, col: number): boolean => {
    return getCellContent(row, col) === '';
};

const handleCellClick = (row: number, col: number) => {
    if (!isYourTurn.value || !isEmpty(row, col) || !gameStore.gameId) return;
    
    // Visual feedback while sending via WebSocket
    pendingMove.value = {row, col};
    
    // Make the move
    makeMove(row, col);
};

const makeMove = async (row: number, col: number) => {
    if (!isYourTurn.value || !isEmpty(row, col) || !gameStore.gameId) return;
    
    try {
        console.log(`Sending move via WebSocket: row=${row}, col=${col}`);
        
        // Add visual indication
        const playerMark = player.value?.playerType || '';
        if (board.value && board.value.board[row] && playerMark) {
            document.querySelector(`[data-index="${row}-${col}"]`)?.classList.add('pending-move');
        }
        
        // Create the move message
        const moveMessage = {
            type: 'PLAY_MOVE',
            gameId: gameStore.gameId,
            playerId: authStore.playerId,
            x: row,
            y: col
        };
        
        // Send the move through WebSocket
        webSocketService.sendMessage(moveMessage);
        
    } catch (error) {
        console.error('Failed to make move:', error);
        // Remove pending state if there was an error
        document.querySelector(`[data-index="${row}-${col}"]`)?.classList.remove('pending-move');
    } finally {
        pendingMove.value = null;
    }
};
</script>

<style scoped>
.game-info {
    margin-bottom: 20px;
}

.players {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
}

.player {
    padding: 8px 16px;
    border-radius: 4px;
    background-color: rgba(255, 255, 255, 0.1);
    transition: background-color 0.3s;
}

.player.active {
    background-color: rgba(76, 175, 80, 0.2);
    font-weight: bold;
}

.row {
    display: flex;
    justify-content: center;
}

.cell {
    width: 80px;
    height: 80px;
    border: 1px solid #555;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 2rem;
    font-weight: bold;
    background-color: rgba(255, 255, 255, 0.05);
    transition: all 0.3s ease;
}

.cell.clickable {
    cursor: pointer;
}

.cell.clickable:hover {
    background-color: rgba(255, 255, 255, 0.15);
    transform: scale(1.02);
}

.cell-content {
    width: 100%;
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
}

.board {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin: 20px 0;
}

.cell-x {
    color: #e74c3c;
}

.cell-o {
    color: #3498db;
}

.pending-move {
    animation: pulse 1s infinite;
    background-color: rgba(255, 255, 255, 0.3);
}

@keyframes pulse {
    0% {
        opacity: 0.6;
    }
    50% {
        opacity: 1;
    }
    100% {
        opacity: 0.6;
    }
}
</style>