-- БД исторических событий. order_id хранится как обычный BIGINT (без FK),
-- потому что таблица orders живёт в ДРУГОЙ БД — внешний ключ невозможен.
-- Это типичный компромисс при шардировании по предметным областям.
CREATE TABLE IF NOT EXISTS order_events (
    id        BIGSERIAL PRIMARY KEY,
    order_id  BIGINT NOT NULL,
    action    TEXT   NOT NULL,
    at        TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_order_events_order_id ON order_events(order_id);
