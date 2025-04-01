import { useWebSocketService } from "./useWebsocket";

export const webSocketService = useWebSocketService(
    'ws://localhost:8080/ws/game',
);