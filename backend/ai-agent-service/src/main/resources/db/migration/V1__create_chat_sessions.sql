CREATE TABLE chat_sessions (
    id              BIGSERIAL PRIMARY KEY,
    session_id      VARCHAR(36) NOT NULL UNIQUE,
    user_id         VARCHAR(255) NOT NULL,
    title           VARCHAR(500),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_activity_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_sessions_user_id ON chat_sessions(user_id);
CREATE INDEX idx_chat_sessions_last_activity ON chat_sessions(last_activity_at DESC);

CREATE TABLE chat_messages (
    id              BIGSERIAL PRIMARY KEY,
    session_id      VARCHAR(36) NOT NULL REFERENCES chat_sessions(session_id) ON DELETE CASCADE,
    role            VARCHAR(20) NOT NULL,
    content         TEXT NOT NULL,
    providers_json  TEXT,
    quick_replies_json TEXT,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_messages_session_id ON chat_messages(session_id);
CREATE INDEX idx_chat_messages_created_at ON chat_messages(session_id, created_at);
