import { ref, onUnmounted, Ref } from 'vue';
import { WebSocketMessageType, WebSocketGenericMessage } from './types';

export interface WebSocketServiceState {
    isConnected: Ref<boolean>;
    isConnecting: Ref<boolean>;
    lastMessage: Ref<any>;
    error: Ref<Error | Event | null>;
    disconnected: Ref<boolean>;
}

export interface WebSocketService extends WebSocketServiceState {
    connect: (playerId?: string | null) => void;
    disconnect: () => void;
    sendMessage: (data: any) => void;
    registerMessageHandler: (type: WebSocketMessageType, handler: (payload: any) => void) => void;
    registerDisconnectHandler: (handler: () => void) => void;
}

export function useWebSocketService(
    url: string,
    options: {
        reconnectAttempts?: number;
        reconnectInterval?: number;
        autoCleanup?: boolean
    } = {}
): WebSocketService {
    const socket = ref<WebSocket | null>(null);
    const reconnectAttempts = options.reconnectAttempts || 5;
    const reconnectInterval = options.reconnectInterval || 3000;
    const autoCleanup = options.autoCleanup !== false;

    let reconnectCount = 0;
    let reconnectTimeout: number | null = null;
    let manuallyDisconnected = false;

    const isConnected = ref(false);
    const isConnecting = ref(false);
    const lastMessage = ref<any>(null);
    const error = ref<Error | Event | null>(null);
    const disconnected = ref(false);

    const messageHandlers: Map<WebSocketMessageType, (payload: any) => void> = new Map();
    const disconnectHandlers: Array<() => void> = [];

    const connect = (playerId?: string | null): void => {
        console.log('Connecting to WebSocket...');
        if (socket.value && (socket.value.readyState === WebSocket.OPEN || socket.value.readyState === WebSocket.CONNECTING)) {
            console.warn('WebSocket is already connected or connecting');
            return;
        }

        manuallyDisconnected = false;

        try {
            isConnecting.value = true;
            const fullUrl = buildWebSocketUrl(url, playerId);

            console.log('Connecting to WebSocket:', fullUrl.toString());
            socket.value = new WebSocket(fullUrl);

            setupSocketEventHandlers();
        } catch (err) {
            handleConnectionError(err);
        }
    };

    const disconnect = (): void => {
        console.log('Manual disconnect requested');

        manuallyDisconnected = true;

        if (reconnectTimeout) {
            clearTimeout(reconnectTimeout);
            reconnectTimeout = null;
        }

        if (!socket.value) return;

        socket.value.close();
        socket.value = null;
        reconnectCount = 0;
        disconnected.value = true;
    };

    const registerDisconnectHandler = (handler: () => void): void => {
        disconnectHandlers.push(handler);
    };

    const sendMessage = (data: any): void => {
        if (!socket.value || socket.value.readyState !== WebSocket.OPEN) {
            error.value = new Error('WebSocket is not connected');
            console.error('WebSocket is not connected');
            return;
        }

        try {
            const messageString = typeof data === 'string' ? data : JSON.stringify(data);
            socket.value.send(messageString);
        } catch (err) {
            error.value = err as Error;
            console.error('Error sending message:', err);
        }
    };

    const registerMessageHandler = (type: WebSocketMessageType, handler: (payload: any) => void): void => {
        messageHandlers.set(type, handler);
    };

    const buildWebSocketUrl = (baseUrl: string, playerId?: string | null): URL => {
        const fullUrl = new URL(baseUrl);
        if (playerId) {
            fullUrl.searchParams.set('playerId', playerId);
        }
        return fullUrl;
    };

    const setupSocketEventHandlers = (): void => {
        if (!socket.value) return;

        socket.value.onopen = handleOpen;
        socket.value.onclose = handleClose;
        socket.value.onerror = handleError;
        socket.value.onmessage = handleMessage;
    };

    const handleConnectionError = (err: unknown): void => {
        error.value = err instanceof Error ? err : new Error(String(err));
        isConnecting.value = false;
        console.error('WebSocket connection error:', err);
        disconnected.value = true;
        notifyDisconnectHandlers();
    };

    const handleOpen = (event: Event): void => {
        isConnected.value = true;
        isConnecting.value = false;
        error.value = null;
        reconnectCount = 0;
    };

    // Handle WebSocket close event
    const handleClose = (event: CloseEvent): void => {
        const wasConnected = isConnected.value;
        isConnected.value = false;
        isConnecting.value = false;
        disconnected.value = true;

        if (wasConnected) {
            notifyDisconnectHandlers();
        }

        if (!manuallyDisconnected && reconnectCount < reconnectAttempts) {
            console.log(`Automatic reconnect attempt ${reconnectCount + 1}/${reconnectAttempts}`);
            reconnectCount++;
            reconnectTimeout = window.setTimeout(() => {
                console.log('Reconnecting to WebSocket...');
                connect();
            }, reconnectInterval);
        } else {
            console.log('Socket closed, no reconnection will be attempted');
        }
    };

    const notifyDisconnectHandlers = (): void => {
        disconnectHandlers.forEach(handler => {
            try {
                handler();
            } catch (error) {
                console.error('Error in disconnect handler:', error);
            }
        });
    };

    const handleError = (event: Event): void => {
        console.error('WebSocket error:', event);
        error.value = event;
    };

    const handleMessage = (event: MessageEvent): void => {
        try {
            const genericMessage = JSON.parse(event.data) as WebSocketGenericMessage;
            const typeToEnum = WebSocketMessageType[genericMessage.type as keyof typeof WebSocketMessageType];

            lastMessage.value = { type: typeToEnum, payload: genericMessage.payload };

            // Parse payload if it's a string
            console.log('Received message:', genericMessage.payload);

            const payload = typeof genericMessage.payload === 'string' ?
                JSON.parse(genericMessage.payload) :
                genericMessage.payload;

            console.log('Parsed payload:', payload, typeof genericMessage.payload === 'string');
            // Call registered handler for this message type
            const handler = messageHandlers.get(typeToEnum);

            if (handler) {
                handler(payload);
            } else {
                console.log('No handler for message type:', typeToEnum);
            }
        } catch (error) {
            console.error('Error parsing WebSocket message:', error);
            error.value = error instanceof Error ? error : new Error('Failed to parse WebSocket message');
        }
    };

    onUnmounted(() => {
        if (socket.value) {
            disconnect();
        }
    });

    return {
        isConnected,
        isConnecting,
        lastMessage,
        error,
        disconnected,
        registerDisconnectHandler,
        connect,
        disconnect,
        sendMessage,
        registerMessageHandler
    };
}