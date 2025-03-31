import { defineStore } from 'pinia';
import { ref } from 'vue';
import { Board, PlayerDto, WebSocketMatchFoundMessage, GameState, Move, WebSocketPlayMovePayload } from '../types';

export const useGameStore = defineStore('game', () => {
    // State
    const gameId = ref<string | null>(null);
    const gameState = ref<GameState | null>(null);
    const player = ref<PlayerDto | null>(null);
    const opponent = ref<PlayerDto | null>(null);
    const board = ref<Board | null>(null);
    const moves = ref<Move[]>([]);
    const yourTurn = ref<boolean>(false);

    function getOpponent(): PlayerDto | null {
        return opponent.value;
    }

    function getPlayer(): PlayerDto | null {
        return player.value;
    }

    function getBoard(): Board | null {
        return board.value;
    }

    function isYourTurn(): boolean {
        return yourTurn.value;
    }

    function getGameState(): GameState | null {
        return gameState.value;
    }

    function setGame(message: WebSocketMatchFoundMessage) {
        const payload = message.payload;

        console.log("Player:", payload.you);
        console.log("Opponent:", payload.opponent);

        gameId.value = payload.gameId;
        gameState.value = payload.gameState;
        player.value = payload.you;
        opponent.value = payload.opponent;
        board.value = payload.board;
        moves.value = payload.moves;
        yourTurn.value = payload.yourTurn;
    }

    function resetGame() {
        gameId.value = null;
        gameState.value = null;
        player.value = null;
        opponent.value = null;
        board.value = null;
        moves.value = [];
        yourTurn.value = false;
    }

    function playMove(payload: WebSocketPlayMovePayload) {
        const { lastMove, board: newBoard, gameState: newGameState, nextPlayer } = payload;
        moves.value.push(lastMove);
        board.value = newBoard;
        gameState.value = newGameState;
        yourTurn.value = nextPlayer.id === player.value?.playerId;
    }

    return {
        gameId,
        gameState,
        player,
        opponent,
        board,
        moves,
        yourTurn,
        getOpponent,
        getPlayer,
        getBoard,
        isYourTurn,
        getGameState,
        setGame,
        resetGame,
        playMove,
    };
});