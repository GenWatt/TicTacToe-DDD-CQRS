import { ref, computed, watch, Ref } from 'vue';
import { useAuthStore } from '../../../stores/useAuthStore';
import { useGameStore } from '../../../stores/useGameStore';
import {
  WebSocketMessageType,
  WebSocketMatchFoundMessage,
  WebSocketPlayMovePayload,
  GameEndedPayload,
  WebSocketInQueueMessage
} from '../../../types';
import { IWebSocketService } from '../../../IWebSocketService';


export class GameViewModel {
  private gameStore = useGameStore();
  private authStore = useAuthStore();
  private webSocketService: IWebSocketService;

  // UI state
  public displayMessage = ref<string>('');
  public inQueue = ref<boolean>(false);
  public errorMessage = ref<string>('');

  // Game state
  public gameActive = computed(() => this.gameStore.board !== null);

  // UI computed properties
  public matchmakingButtonText = computed(() => {
    if (this.webSocketService.isConnecting.value) return 'Connecting...';
    if (this.inQueue.value) return 'Looking for opponent...';
    if (this.gameActive.value) return 'In Game';
    return `Find Match (${this.authStore.username})`;
  });

  public connectedText = computed(() => {
    return this.webSocketService.isConnected.value ? 'Connected to server' : 'Not connected to server';
  });

  constructor(webSocketService: IWebSocketService) {
    this.webSocketService = webSocketService;

    // Setup error handling from WebSocketService
    this.setupErrorHandling();
  }

  public initialize(): void {
    this.registerWebSocketHandlers();
    this.connect();
    this.gameStore.resetGame();
  }

  public connect(): void {
    this.webSocketService.connect(this.authStore.playerId);
  }

  public handleMatchmaking = () => {
    if (!this.webSocketService.isConnected.value || this.gameActive.value) return;

    this.inQueue.value = true;

    const message = {
      type: 'JOIN_MATCHMAKING',
      playerId: this.authStore.playerId
    };

    this.webSocketService.sendMessage(message);
  }

  private setupErrorHandling(): void {
    // Watch for WebSocketService errors and propagate them to our ViewModel
    watch(this.webSocketService.error, (error) => {
      if (error) {
        this.errorMessage.value = error instanceof Error
          ? error.message
          : 'Connection error. Please try again later.';
        console.error('WebSocket error in GameViewModel:', error);
      } else {
        this.errorMessage.value = '';
      }
    });
  }

  private registerWebSocketHandlers(): void {
    // Handle "in queue" messages
    this.webSocketService.registerMessageHandler(
      WebSocketMessageType.IN_QUEUE,
      this.handleInQueueMessage.bind(this)
    );

    // Handle "match found" messages
    this.webSocketService.registerMessageHandler(
      WebSocketMessageType.MATCH_FOUND,
      this.handleMatchFoundMessage.bind(this)
    );

    // Handle "already in queue" messages
    this.webSocketService.registerMessageHandler(
      WebSocketMessageType.ALREADY_IN_QUEUE,
      this.handleAlreadyInQueueMessage.bind(this)
    );

    // Handle "play move" messages
    this.webSocketService.registerMessageHandler(
      WebSocketMessageType.PLAY_MOVE,
      this.handlePlayMoveMessage.bind(this)
    );

    // Handle "game ended" messages
    this.webSocketService.registerMessageHandler(
      WebSocketMessageType.GAME_ENDED,
      this.handleGameEndedMessage.bind(this)
    );
  }

  // Individual message handlers for better separation of concerns
  private handleInQueueMessage(payload: WebSocketInQueueMessage['payload']): void {
    this.displayMessage.value = payload.message;
  }

  private handleMatchFoundMessage(payload: any): void {
    const matchFoundMessage = {
      type: WebSocketMessageType.MATCH_FOUND,
      payload
    } as WebSocketMatchFoundMessage;

    this.inQueue.value = false;
    this.gameStore.setGame(matchFoundMessage);
    this.displayMessage.value = `Match found! Your opponent is ${this.gameStore.getOpponent()?.username}`;

    if (this.gameStore.isYourTurn()) {
      this.displayMessage.value += ". It's your turn!";
    } else {
      this.displayMessage.value += ". Waiting for opponent's move...";
    }
  }

  private handleAlreadyInQueueMessage(): void {
    this.displayMessage.value = 'You are already in the matchmaking queue.';
  }

  private handlePlayMoveMessage(payload: WebSocketPlayMovePayload): void {
    this.gameStore.playMove(payload);

    // Update display message based on whose turn it is
    if (this.gameStore.isYourTurn()) {
      this.displayMessage.value = "It's your turn!";
    } else {
      this.displayMessage.value = "Waiting for opponent's move...";
    }
  }

  private handleGameEndedMessage(payload: GameEndedPayload): void {
    const winnerId = payload.winner?.id;

    if (!winnerId) {
      this.displayMessage.value = 'Game over! It\'s a draw!';
    } else if (winnerId === this.authStore.playerId) {
      this.displayMessage.value = 'Game over! You win!';
    } else {
      this.displayMessage.value = 'Game over! You lose!';
    }

    // Reset queue state
    this.inQueue.value = false;
  }
}