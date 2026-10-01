CREATE TABLE reservation_funnel_events (
    event_sequence BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    session_id UUID NOT NULL,
    reservation_attempt_id UUID NOT NULL,
    event_type VARCHAR(32) NOT NULL CHECK (event_type IN ('RESERVATION_STARTED', 'SCREEN_VIEWED', 'RESERVATION_CONFIRMED')),
    screen VARCHAR(20) NOT NULL CHECK (screen IN ('home', 'results', 'hotel', 'checkout', 'confirmation', 'trips')),
    hotel_id VARCHAR(80),
    room_type_id VARCHAR(100),
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX reservation_funnel_events_attempt_idx
    ON reservation_funnel_events (session_id, reservation_attempt_id, event_sequence DESC);

CREATE VIEW reservation_funnel_abandonments AS
WITH attempt_summaries AS (
    SELECT
        session_id,
        reservation_attempt_id,
        MIN(occurred_at) FILTER (WHERE event_type = 'RESERVATION_STARTED') AS started_at,
        MAX(occurred_at) AS last_event_at,
        (ARRAY_AGG(screen ORDER BY event_sequence DESC)
            FILTER (WHERE event_type IN ('RESERVATION_STARTED', 'SCREEN_VIEWED')))[1] AS last_screen,
        (ARRAY_AGG(hotel_id ORDER BY event_sequence DESC)
            FILTER (WHERE hotel_id IS NOT NULL))[1] AS hotel_id,
        (ARRAY_AGG(room_type_id ORDER BY event_sequence DESC)
            FILTER (WHERE room_type_id IS NOT NULL))[1] AS room_type_id,
        BOOL_OR(event_type = 'RESERVATION_CONFIRMED') AS completed
    FROM reservation_funnel_events
    GROUP BY session_id, reservation_attempt_id
)
SELECT session_id, reservation_attempt_id, started_at, last_event_at, last_screen, hotel_id, room_type_id
FROM attempt_summaries
WHERE started_at IS NOT NULL
  AND NOT completed
  AND last_event_at <= CURRENT_TIMESTAMP - INTERVAL '30 minutes';
