CREATE TABLE IF NOT EXISTS players (
    id UUID PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    player_type VARCHAR(1),
    UNIQUE (username)
);
CREATE TABLE IF NOT EXISTS games (
    id UUID PRIMARY KEY,
    state VARCHAR(20) NOT NULL,
    board TEXT,
    moves TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    current_player_move_id UUID NOT NULL
);
CREATE TABLE IF NOT EXISTS game_players (
    game_id UUID NOT NULL,
    player_id UUID NOT NULL,
    PRIMARY KEY (game_id, player_id),
    FOREIGN KEY (game_id) REFERENCES games(id),
    FOREIGN KEY (player_id) REFERENCES players(id)
);
CREATE INDEX idx_games_state ON games(state);
CREATE INDEX idx_player_username ON players(username);