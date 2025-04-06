import { ref, computed, watch, Ref } from 'vue';
import { useAuthStore } from '../../../stores/useAuthStore';
import { useGameStore } from '../../../stores/useGameStore';
import {
  WebSocketMessageType,
  WebSocketMatchFoundMessage,
  WebSocketPlayMovePayload,
  GameEndedPayload,
  WebSocketInQueueMessage,
  WebSocketErrorMessagePayload,
  GameState
} from '../../../types';
import { webSocketService } from '../../../gameWebsocketService';

export interface GameViewModel {
  displayMessage: Ref<string>;
  inQueue: Ref<boolean>;
  errorMessage: Ref<string>;
  gameActive: Ref<boolean>;

  isConnected: Ref<boolean>;
  isConnecting: Ref<boolean>;

  matchmakingButtonText: Ref<string>;
  reconnectButtonText: Ref<string>;
  disconnectButtonText: Ref<string>;
  connectedText: Ref<string>;

  initialize: () => void;
  connect: () => void;
  disconnect: () => void;
  handleMatchmaking: () => void;
}

export function useGameViewModel(): GameViewModel {
  const gameStore = useGameStore();
  const authStore = useAuthStore();

  const displayMessage = ref<string>('');
  const inQueue = ref<boolean>(false);
  const errorMessage = ref<string>('');

  const gameActive = computed(() => gameStore.getGameState() === GameState.IN_PROGRESS);

  const matchmakingButtonText = computed(() => {
    if (webSocketService.isConnecting.value) return 'Connecting...';
    if (inQueue.value) return 'Looking for opponent...';
    if (gameActive.value) return 'In Game';
    return `Find Match`;
  });

  const reconnectButtonText = computed(() => 'Reconnect');
  const disconnectButtonText = computed(() => 'Disconnect');

  const connectedText = computed(() => {
    return webSocketService.isConnected.value ? 'Connected to server' : 'Not connected to server';
  });

  const initialize = (): void => {
    registerWebSocketHandlers();
    registerDisconnectionHandler();
    connect();
    gameStore.resetGame();
  };

  const connect = (): void => {
    console.log('Connecting to WebSocket...');
    webSocketService.connect(authStore.playerId, authStore.token);
  };

  const disconnect = (): void => {
    console.log('Disconnecting from WebSocket...');
    webSocketService.disconnect();

    if (gameActive.value) {
      handleGameDisconnect();
    }
  };

  const handleGameDisconnect = (): void => {
    if (gameActive.value) {
      displayMessage.value = 'Game over! You lost due to connection problems.';
      gameStore.resetGame();
      inQueue.value = false;
    }
  };

  const registerDisconnectionHandler = (): void => {
    webSocketService.registerDisconnectHandler(() => {
      if (gameActive.value) {
        handleGameDisconnect();
      }
    });

    watch(webSocketService.isConnected, (connected, prevConnected) => {
      if (prevConnected && !connected && gameActive.value) {
        handleGameDisconnect();
      }
    });
  };

  const handleMatchmaking = (): void => {
    if (!webSocketService.isConnected.value || gameActive.value) return;

    inQueue.value = true;

    const message = {
      type: 'JOIN_MATCHMAKING',
      playerId: authStore.playerId
    };

    webSocketService.sendMessage(message);
  };

  const setupErrorHandling = (): void => {
    watch(webSocketService.error, (error) => {
      if (error) {
        errorMessage.value = error instanceof Error
          ? error.message
          : 'Connection error. Please try again later.';
        console.error('WebSocket error in GameViewModel:', error);
      } else {
        errorMessage.value = '';
      }
    });
  };

  const registerWebSocketHandlers = (): void => {
    webSocketService.registerMessageHandler(
      WebSocketMessageType.IN_QUEUE,
      handleInQueueMessage
    );

    webSocketService.registerMessageHandler(
      WebSocketMessageType.MATCH_FOUND,
      handleMatchFoundMessage
    );

    webSocketService.registerMessageHandler(
      WebSocketMessageType.ALREADY_IN_QUEUE,
      handleAlreadyInQueueMessage
    );

    webSocketService.registerMessageHandler(
      WebSocketMessageType.PLAY_MOVE,
      handlePlayMoveMessage
    );

    webSocketService.registerMessageHandler(
      WebSocketMessageType.GAME_ENDED,
      handleGameEndedMessage
    );

    webSocketService.registerMessageHandler(
      WebSocketMessageType.ERROR,
      (payload: WebSocketErrorMessagePayload) => {
        errorMessage.value = payload.message;
        console.error('WebSocket error:', payload.message);
      }
    )
  };

  const handleInQueueMessage = (payload: WebSocketInQueueMessage['payload']): void => {
    displayMessage.value = payload.message;
  };

  const handleMatchFoundMessage = (payload: any): void => {
    const matchFoundMessage = {
      type: WebSocketMessageType.MATCH_FOUND,
      payload
    } as WebSocketMatchFoundMessage;

    inQueue.value = false;
    gameStore.setGame(matchFoundMessage);
    displayMessage.value = `Match found! Your opponent is ${gameStore.getOpponent()?.username}`;

    if (gameStore.isYourTurn()) {
      displayMessage.value += ". It's your turn!";
    } else {
      displayMessage.value += ". Waiting for opponent's move...";
    }
  };

  const handleAlreadyInQueueMessage = (): void => {
    displayMessage.value = 'You are already in the matchmaking queue.';
  };

  const handlePlayMoveMessage = (payload: WebSocketPlayMovePayload): void => {
    gameStore.playMove(payload);

    if (gameStore.isYourTurn()) {
      displayMessage.value = "It's your turn!";
    } else {
      displayMessage.value = "Waiting for opponent's move...";
    }
  };

  const handleGameEndedMessage = (payload: GameEndedPayload): void => {
    const winnerId = payload.winner?.id;

    if (!winnerId) {
      displayMessage.value = 'Game over! It\'s a draw!';
    } else if (winnerId === authStore.playerId) {
      displayMessage.value = 'Game over! You win!';
    } else {
      displayMessage.value = 'Game over! You lose!';
    }

    gameStore.resetGame();

    inQueue.value = false;
  };

  setupErrorHandling();

  return {
    displayMessage,
    inQueue,
    errorMessage,
    gameActive,

    isConnected: webSocketService.isConnected,
    isConnecting: webSocketService.isConnecting,

    matchmakingButtonText,
    reconnectButtonText,
    disconnectButtonText,
    connectedText,

    initialize,
    connect,
    disconnect,
    handleMatchmaking,
  };
}