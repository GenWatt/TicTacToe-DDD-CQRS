import { ref } from 'vue';
import { IWebSocketService } from './IWebSocketService';
import { WebSocketMessageType, WebSocketGenericMessage } from './types';

export class WebSocketService implements IWebSocketService {
    private socket: WebSocket | null = null;
    private url: string;
    private reconnectAttempts: number;
    private reconnectInterval: number;
    private reconnectCount = 0;
    private reconnectTimeout: number | null = null;

    public isConnected = ref(false);
    public isConnecting = ref(false);
    public lastMessage = ref<any>(null);
    public error = ref<Error | Event | null>(null);

    // Message handlers map
    private messageHandlers: Map<WebSocketMessageType, (payload: any) => void> = new Map();

    constructor(url: string, options: { reconnectAttempts?: number; reconnectInterval?: number } = {}) {
        this.url = url;
        this.reconnectAttempts = options.reconnectAttempts || 5;
        this.reconnectInterval = options.reconnectInterval || 3000;
    }

    public registerMessageHandler(type: WebSocketMessageType, handler: (payload: any) => void): void {
        this.messageHandlers.set(type, handler);
    }

    public connect(playerId?: string | null): void {
        if (this.socket) {
            console.warn('WebSocket is already connected or connecting');
            return;
        }

        try {
            this.isConnecting.value = true;
            const fullUrl = this.buildWebSocketUrl(playerId);

            console.log('Connecting to WebSocket:', fullUrl.toString());
            this.socket = new WebSocket(fullUrl);

            this.setupSocketEventHandlers();
        } catch (err) {
            this.handleConnectionError(err);
        }
    }

    public disconnect(): void {
        if (this.reconnectTimeout) {
            clearTimeout(this.reconnectTimeout);
            this.reconnectTimeout = null;
        }

        if (!this.socket) return;

        this.socket.close();
        this.socket = null;
        this.reconnectCount = 0;
    }

    public sendMessage(data: any): void {
        if (!this.socket || this.socket.readyState !== WebSocket.OPEN) {
            this.error.value = new Error('WebSocket is not connected');
            console.error('WebSocket is not connected');
            return;
        }

        try {
            const messageString = typeof data === 'string' ? data : JSON.stringify(data);
            this.socket.send(messageString);
        } catch (err) {
            this.error.value = err as Error;
            console.error('Error sending message:', err);
        }
    }

    private buildWebSocketUrl(playerId?: string | null): URL {
        const fullUrl = new URL(this.url);
        if (playerId) {
            fullUrl.searchParams.set('playerId', playerId);
        }
        return fullUrl;
    }

    private setupSocketEventHandlers(): void {
        if (!this.socket) return;

        this.socket.onopen = this.handleOpen.bind(this);
        this.socket.onclose = this.handleClose.bind(this);
        this.socket.onerror = this.handleError.bind(this);
        this.socket.onmessage = this.handleMessage.bind(this);
    }

    private handleConnectionError(err: unknown): void {
        this.error.value = err instanceof Error ? err : new Error(String(err));
        this.isConnecting.value = false;
        console.error('WebSocket connection error:', err);
    }

    private handleOpen(event: Event): void {
        this.isConnected.value = true;
        this.isConnecting.value = false;
        this.error.value = null;
        this.reconnectCount = 0;
    }

    private handleClose(event: CloseEvent): void {
        this.isConnected.value = false;
        this.isConnecting.value = false;

        if (this.reconnectCount < this.reconnectAttempts) {
            this.reconnectCount++;
            this.reconnectTimeout = window.setTimeout(() => {
                this.connect();
            }, this.reconnectInterval);
        }
    }

    private handleError(event: Event): void {
        console.error('WebSocket error:', event);
        this.error.value = event;
    }

    private handleMessage(event: MessageEvent): void {
        try {
            const genericMessage = JSON.parse(event.data) as WebSocketGenericMessage;
            const typeToEnum = WebSocketMessageType[genericMessage.type as keyof typeof WebSocketMessageType];

            this.lastMessage.value = { type: typeToEnum, payload: genericMessage.payload };

            // Parse payload if it's a string
            console.log('Received message:', this.lastMessage.value);
            const payload = typeof genericMessage.payload === 'string' ?
                JSON.parse(genericMessage.payload) :
                genericMessage.payload;

            // Call registered handler for this message type
            const handler = this.messageHandlers.get(typeToEnum);
            if (handler) {
                handler(payload);
            } else {
                console.log('No handler for message type:', typeToEnum);
            }
        } catch (error) {
            console.error('Error parsing WebSocket message:', error);
            this.error.value = error instanceof Error ? error : new Error('Failed to parse WebSocket message');
        }
    }
}