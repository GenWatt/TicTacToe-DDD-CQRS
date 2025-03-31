export enum WebSocketMessageType {
    IN_QUEUE = 'IN_QUEUE',
    MATCH_FOUND = 'MATCH_FOUND',
    ALREADY_IN_QUEUE = 'ALREADY_IN_QUEUE',
    PLAY_MOVE = 'PLAY_MOVE',
    GAME_ENDED = 'GAME_ENDED',
}

export type WebSocketInQueueMessage = {
    type: WebSocketMessageType.IN_QUEUE;
}

export interface Player {
    id: string;
    username: string;
    playerType: string | null;
    score: number;
}

export interface Board {
    board: ({ id: string } | null)[][];
    full: boolean;
}

export interface Move {
    x: number;
    y: number;
}

export interface PlayerDto {
    playerId: string;
    username: string;
    playerType: string;
}

export enum GameState {
    CREATED = 'CREATED',
    IN_PROGRESS = 'IN_PROGRESS',
    ENDED = 'ENDED',
}

export interface MatchFoundPayload {
    gameId: string;
    gameState: GameState;
    you: PlayerDto;
    opponent: PlayerDto;
    board: Board;
    moves: Move[];
    yourTurn: boolean;
}

export interface WebSocketMatchFoundMessage {
    type: WebSocketMessageType.MATCH_FOUND;
    payload: MatchFoundPayload;
}

export interface WebSocketPlayMovePayload {
    board: Board;
    lastMove: Move;
    nextPlayer: { id: string };
    gameState: GameState;
}

export interface WebSocketPlayMoveMessage {
    type: WebSocketMessageType.PLAY_MOVE;
    payload: WebSocketPlayMovePayload
}

export type GameDto = {
    id: string;
    currentPlayerType: string;
    gameStatus: string;
    winner: string;
    board: Board;
}

export type WebSocketAlreadyInQueueMessage = {
    type: WebSocketMessageType.ALREADY_IN_QUEUE;
}

export type WebSocketMessage = WebSocketInQueueMessage | WebSocketMatchFoundMessage | WebSocketAlreadyInQueueMessage;

export interface WebSocketGenericMessage {
    type: string;
    payload: string;
}

export interface WebSocketGameEndedMessage {
    type: WebSocketMessageType.GAME_ENDED;
    payload: GameEndedPayload;
}

export type GameEndedPayload = {
    board: Board;
    winner: { id: string };
    gameState: GameState;
}