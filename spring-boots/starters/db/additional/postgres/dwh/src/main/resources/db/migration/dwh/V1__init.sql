-- ====================================================================
-- Миграция основной БД dwh.
--
-- Замечания:
--   * BIGSERIAL — простой автоинкремент. В современных проектах вместо него
--     обычно используют GENERATED ALWAYS AS IDENTITY (стандарт SQL), но
--     BIGSERIAL остаётся читабельнее для учебной демонстрации.
--   * created_at TIMESTAMPTZ — храним в UTC. Без зоны хранение «локального»
--     времени — типичный источник багов в DWH.
--   * Индекс по customer полезен для типичной выборки «заказы клиента».
-- ====================================================================
CREATE TABLE IF NOT EXISTS orders (
    id          BIGSERIAL PRIMARY KEY,
    customer    TEXT        NOT NULL,
    currency    CHAR(3)     NOT NULL,
    amount      NUMERIC(18,4) NOT NULL CHECK (amount >= 0),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_orders_customer ON orders(customer);
