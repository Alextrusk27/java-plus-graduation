CREATE TABLE IF NOT EXISTS participation_requests (
    id           BIGINT      GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    created      TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    event_id     BIGINT      NOT NULL,
    requester_id BIGINT      NOT NULL,
    status       VARCHAR(20) NOT NULL
);