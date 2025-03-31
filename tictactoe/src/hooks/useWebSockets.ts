// import { ref, onMounted, onBeforeUnmount, Ref } from 'vue';

// export interface UseWebSocketOptions {
//     url: string;
//     protocols?: string | string[];
//     reconnectAttempts?: number;
//     reconnectInterval?: number;
//     playerId?: string | null;
//     onOpen?: (event: WebSocketEventMap['open']) => void;
//     onClose?: (event: WebSocketEventMap['close']) => void;
//     onError?: (event: WebSocketEventMap['error']) => void;
//     onMessage?: (event: WebSocketEventMap['message']) => void;
// }

// interface UseWebSocketReturn {
//     socket: WebSocket | null;
//     status: 'CONNECTING' | 'OPEN' | 'CLOSING' | 'CLOSED' | 'RECONNECTING';
//     lastMessage: MessageEvent | null;
//     sendMessage: (data: string | ArrayBufferLike | Blob | ArrayBufferView) => void;
//     connect: () => void;
//     disconnect: () => void;
//     isConnected: Ref<boolean>;
//     isConnecting: Ref<boolean>;
//     error: Event | null;
// }

// // Shared global state
// const globalSocket = ref<WebSocket | null>(null);
// const globalStatus = ref<'CONNECTING' | 'OPEN' | 'CLOSING' | 'CLOSED' | 'RECONNECTING'>('CLOSED');
// const globalLastMessage = ref<MessageEvent | null>(null);
// const globalError = ref<Event | null>(null);
// const connectionCount = ref(0);
// let reconnectTimeout: number | null = null;
// let reconnectCount = 0;

// export function useWebSocket(options: UseWebSocketOptions): UseWebSocketReturn {
//     const {
//         url,
//         protocols,
//         reconnectAttempts = 5,
//         reconnectInterval = 3000,
//         playerId,
//         onOpen,
//         onClose,
//         onError,
//         onMessage,
//     } = options;

//     const isConnected = ref(false);
//     const isConnecting = ref(false);

//     const connect = (): void => {
//         if (globalSocket.value?.readyState === WebSocket.OPEN) {
//             connectionCount.value++;
//             return;
//         }

//         try {
//             if (!globalSocket.value || globalSocket.value.readyState === WebSocket.CLOSED) {
//                 isConnecting.value = true;
//                 globalStatus.value = 'CONNECTING';

//                 const fullUrl = new URL(url);

//                 if (playerId) {
//                     fullUrl.searchParams.set('playerId', playerId);
//                 }

//                 console.log('Connecting to WebSocket:', fullUrl.toString());
//                 globalSocket.value = new WebSocket(fullUrl, protocols);

//                 globalSocket.value.onopen = (event: Event) => {
//                     globalStatus.value = 'OPEN';
//                     isConnected.value = true;
//                     isConnecting.value = false;
//                     globalError.value = null;
//                     reconnectCount = 0;
//                     connectionCount.value++;
//                     onOpen?.(event as WebSocketEventMap['open']);
//                 };

//                 globalSocket.value.onclose = (event: CloseEvent) => {
//                     globalStatus.value = 'CLOSED';
//                     isConnected.value = false;
//                     isConnecting.value = false;
//                     onClose?.(event);

//                     if (reconnectCount < reconnectAttempts && connectionCount.value > 0) {
//                         globalStatus.value = 'RECONNECTING';
//                         reconnectCount++;
//                         reconnectTimeout = window.setTimeout(() => {
//                             connect();
//                         }, reconnectInterval);
//                     }
//                 };

//                 globalSocket.value.onerror = (event: Event) => {
//                     globalError.value = event;
//                     onError?.(event as WebSocketEventMap['error']);
//                 };

//                 globalSocket.value.onmessage = (event: MessageEvent) => {
//                     globalLastMessage.value = event;
//                     onMessage?.(event);
//                 };
//             } else {
//                 connectionCount.value++;
//             }
//         } catch (err) {
//             globalError.value = err as Event;
//             isConnecting.value = false;
//             globalStatus.value = 'CLOSED';
//             console.error('WebSocket connection error:', err);
//         }
//     };

//     const disconnect = (): void => {
//         connectionCount.value = Math.max(0, connectionCount.value - 1);

//         if (connectionCount.value > 0) return;

//         if (reconnectTimeout) {
//             clearTimeout(reconnectTimeout);
//             reconnectTimeout = null;
//         }

//         if (!globalSocket.value) return;

//         globalStatus.value = 'CLOSING';
//         globalSocket.value.close();
//         globalSocket.value = null;
//         reconnectCount = 0;
//     };

//     const sendMessage = (data: string | ArrayBufferLike | Blob | ArrayBufferView): void => {
//         if (!globalSocket.value || globalSocket.value.readyState !== WebSocket.OPEN) {
//             console.error('WebSocket is not connected');
//             return;
//         }

//         try {
//             globalSocket.value.send(data);
//         } catch (err) {
//             globalError.value = err as Event;
//             console.error('Error sending message:', err);
//         }
//     };

//     onMounted(() => {
//         connect();
//     });

//     onBeforeUnmount(() => {
//         disconnect();
//     });

//     return {
//         socket: globalSocket.value,
//         status: globalStatus.value,
//         lastMessage: globalLastMessage.value,
//         sendMessage,
//         connect,
//         disconnect,
//         isConnected,
//         isConnecting,
//         error: globalError.value,
//     };
// }