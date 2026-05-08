-- Справочная БД: код валюты как PRIMARY KEY (естественный ключ).
CREATE TABLE IF NOT EXISTS currencies (
    code  CHAR(3) PRIMARY KEY,
    name  TEXT NOT NULL
);

-- Seed-данные. ON CONFLICT DO NOTHING — идемпотентность миграции (если данные
-- уже есть, перезапуск не упадёт).
INSERT INTO currencies(code, name) VALUES
    ('USD', 'US Dollar'),
    ('EUR', 'Euro'),
    ('RUB', 'Russian Ruble')
ON CONFLICT (code) DO NOTHING;
