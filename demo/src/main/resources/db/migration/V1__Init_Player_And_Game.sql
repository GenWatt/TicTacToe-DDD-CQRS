-- DROP TABLE IF EXISTS game_players CASCADE;
-- DROP TABLE IF EXISTS games CASCADE;
-- DROP TABLE IF EXISTS player CASCADE;
-- DROP INDEX IF EXISTS idx_games_state;
-- DROP INDEX IF EXISTS idx_player_username;
-- DROP INDEX IF EXISTS idx_game_players_game_id;
CREATE TABLE IF NOT EXISTS player (
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
    updated_at TIMESTAMP NOT NULL
);
CREATE TABLE IF NOT EXISTS game_players (
    game_id UUID NOT NULL,
    player_id UUID NOT NULL,
    PRIMARY KEY (game_id, player_id),
    FOREIGN KEY (game_id) REFERENCES games(id),
    FOREIGN KEY (player_id) REFERENCES player(id)
);
CREATE INDEX idx_games_state ON games(state);
CREATE INDEX idx_player_username ON player(username);