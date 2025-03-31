import { Ref } from 'vue';
import { WebSocketMessageType } from './types';

export interface IWebSocketService {
    isConnected: Ref<boolean>;
    isConnecting: Ref<boolean>;
    lastMessage: Ref<any>;
    error: Ref<Error | Event | null>;

    connect(playerId?: string | null): void;
    disconnect(): void;
    sendMessage(data: any): void;
    registerMessageHandler(type: WebSocketMessageType, handler: (payload: any) => void): void;
}