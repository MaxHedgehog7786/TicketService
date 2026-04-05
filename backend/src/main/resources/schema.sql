-- ================================================================
--  TicketService — PostgreSQL Schema
--  Соответствует логической модели БД из отчёта и диаграмме классов
-- ================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- ── Роли ─────────────────────────────────────────────────────
CREATE TABLE roles (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(32) NOT NULL UNIQUE
);
INSERT INTO roles (name) VALUES ('USER'), ('ADMIN');

-- ── Пользователи ─────────────────────────────────────────────
CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    login       VARCHAR(64)  NOT NULL UNIQUE,
    password    VARCHAR(64)  NOT NULL,
    name        VARCHAR(64)  NOT NULL,
    surname     VARCHAR(64)  NOT NULL,
    email       VARCHAR(128) NOT NULL UNIQUE,
    phone       VARCHAR(16),
    role_id     INTEGER      NOT NULL REFERENCES roles(id) DEFAULT 1,
    subscribed  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_users_email ON users(email);

-- ── Категории мероприятий ─────────────────────────────────────
CREATE TABLE categories (
    id   SERIAL PRIMARY KEY,
    name VARCHAR(64) NOT NULL UNIQUE
);
INSERT INTO categories (name)
VALUES ('Концерт'),('Спектакль'),('Фестиваль'),('Семейное'),('Выставка'),('Спорт');

-- ── Мероприятия ──────────────────────────────────────────────
CREATE TABLE events (
    id           SERIAL PRIMARY KEY,
    title        VARCHAR(127)   NOT NULL,
    description  TEXT,
    date_time    TIMESTAMP      NOT NULL,
    venue        VARCHAR(255)   NOT NULL,
    city         VARCHAR(64)    NOT NULL,
    category_id  INTEGER        NOT NULL REFERENCES categories(id),
    base_price   DECIMAL(10,2)  NOT NULL,
    image_url    TEXT,
    created_at   TIMESTAMP      NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_events_city     ON events(city);
CREATE INDEX idx_events_category ON events(category_id);
CREATE INDEX idx_events_date     ON events(date_time);
CREATE INDEX idx_events_title    ON events USING GIN (title gin_trgm_ops);
CREATE INDEX idx_events_desc     ON events USING GIN (description gin_trgm_ops);

-- VIEW с рейтингом (используется в запросах)
CREATE VIEW events_with_rating AS
SELECT e.*,
       COALESCE(AVG(r.rating), 0)::NUMERIC(3,2) AS avg_rating,
       COUNT(r.id)                               AS review_count
FROM events e
LEFT JOIN reviews r ON r.event_id = e.id
GROUP BY e.id;

-- ── Места в залах ─────────────────────────────────────────────
CREATE TABLE seats (
    id             SERIAL PRIMARY KEY,
    event_id       INTEGER        NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    seat_number    SMALLINT       NOT NULL,
    row_number     SMALLINT       NOT NULL,
    sector         VARCHAR(32),
    price          DECIMAL(10,2)  NOT NULL,
    status         VARCHAR(16)    NOT NULL DEFAULT 'FREE'
                       CHECK (status IN ('FREE','RESERVED','SOLD')),
    reserved_until TIMESTAMP,
    UNIQUE (event_id, row_number, seat_number)
);
CREATE INDEX idx_seats_event  ON seats(event_id);
CREATE INDEX idx_seats_status ON seats(event_id, status);

-- ── Заказы ───────────────────────────────────────────────────
CREATE TABLE orders (
    id             SERIAL PRIMARY KEY,
    user_id        INTEGER        NOT NULL REFERENCES users(id),
    created_at     TIMESTAMP      NOT NULL DEFAULT NOW(),
    payment_status BOOLEAN        NOT NULL DEFAULT FALSE,
    payment_method VARCHAR(32),
    total_amount   DECIMAL(10,2)  NOT NULL DEFAULT 0
);
CREATE INDEX idx_orders_user ON orders(user_id);

-- ── Билеты ───────────────────────────────────────────────────
CREATE TABLE tickets (
    id         SERIAL PRIMARY KEY,
    order_id   INTEGER        NOT NULL REFERENCES orders(id),
    event_id   INTEGER        NOT NULL REFERENCES events(id),
    seat_id    INTEGER        NOT NULL UNIQUE REFERENCES seats(id),
    price      DECIMAL(10,2)  NOT NULL,
    status     VARCHAR(16)    NOT NULL DEFAULT 'PAID'
                   CHECK (status IN ('PAID','REFUNDED','CANCELLED')),
    qr_code    VARCHAR(255),
    created_at TIMESTAMP      NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_tickets_order ON tickets(order_id);
CREATE INDEX idx_tickets_event ON tickets(event_id);

-- ── Отзывы ───────────────────────────────────────────────────
CREATE TABLE reviews (
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER   NOT NULL REFERENCES users(id),
    event_id    INTEGER   NOT NULL REFERENCES events(id),
    rating      SMALLINT  NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
    is_verified BOOLEAN   NOT NULL DEFAULT FALSE,
    UNIQUE (user_id, event_id)
);
CREATE INDEX idx_reviews_event ON reviews(event_id);

-- ── Избранное ────────────────────────────────────────────────
CREATE TABLE favorites (
    user_id  INTEGER   NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    event_id INTEGER   NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    added_at TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (user_id, event_id)
);

-- ── Транзакции ───────────────────────────────────────────────
CREATE TABLE transactions (
    id             SERIAL PRIMARY KEY,
    order_id       INTEGER        NOT NULL REFERENCES orders(id),
    amount         DECIMAL(10,2)  NOT NULL,
    status         VARCHAR(16)    NOT NULL DEFAULT 'PENDING'
                       CHECK (status IN ('PENDING','SUCCESS','FAILED','REFUNDED')),
    payment_method VARCHAR(32),
    external_id    VARCHAR(128),
    created_at     TIMESTAMP      NOT NULL DEFAULT NOW()
);

-- ── Тестовые данные ──────────────────────────────────────────
-- Пароль для всех: "password" → SHA-256
INSERT INTO users (login,password,name,surname,email,phone,role_id) VALUES
('admin','5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
 'Администратор','Системы','admin@ticketservice.ru','+79001234567',2),
('maxim','5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
 'Максим','Михалишин','maxim@example.ru','+79009876543',1),
('zlata','5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8',
 'Злата','Позднякова','zlata@example.ru','+79001112233',1);

INSERT INTO events (title,description,date_time,venue,city,category_id,base_price,image_url) VALUES
('Концерт Филиппа Киркорова',
 'Грандиозное шоу короля российской эстрады. Три часа лучших хитов, живой оркестр.',
 '2026-05-15 19:00','Крокус Сити Холл','Москва',1,3500.00,
 'https://images.unsplash.com/photo-1470229722913-7c0e2dbbafd3?w=800'),

('Лебединое озеро',
 'Балет Большого театра в постановке Григоровича. Незабываемое классическое искусство.',
 '2026-05-20 18:30','Большой театр','Москва',2,5000.00,
 'https://images.unsplash.com/photo-1518834107812-67b0b7c58434?w=800'),

('Фестиваль уличной еды',
 'Фудкорт под открытым небом с шефами со всей страны.',
 '2026-06-01 12:00','Парк Горького','Москва',3,500.00,
 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=800'),

('Цирк дю Солей — Alegría',
 'Легендарное шоу канадского цирка. Акробатика, поэзия движения и волшебная музыка.',
 '2026-05-25 19:00','СКК Олимпийский','Санкт-Петербург',4,4200.00,
 'https://images.unsplash.com/photo-1511882150382-421056c89033?w=800'),

('Rock Nation Festival',
 'Крупнейший рок-фестиваль сезона. 12 групп, 3 сцены, 2 дня незабываемой музыки.',
 '2026-06-10 16:00','Лужники','Москва',1,2800.00,
 'https://images.unsplash.com/photo-1493225457124-a3eb161ffa5f?w=800'),

('Выставка Айвазовского',
 'Лучшие морские пейзажи великого мариниста. 150 полотен из музеев России.',
 '2026-05-18 10:00','Третьяковская галерея','Москва',5,800.00,
 'https://images.unsplash.com/photo-1578301978693-85fa9c0320b9?w=800');

-- Места для мероприятий 1 и 2
INSERT INTO seats (event_id, seat_number, row_number, sector, price)
SELECT 1, s, r,
       CASE WHEN r<=3 THEN 'VIP' WHEN r<=8 THEN 'A' ELSE 'B' END,
       CASE WHEN r<=3 THEN 7000 WHEN r<=8 THEN 5000 ELSE 3500 END
FROM generate_series(1,20) s, generate_series(1,15) r;

INSERT INTO seats (event_id, seat_number, row_number, sector, price)
SELECT 2, s, r,
       CASE WHEN r<=5 THEN 'Партер' ELSE 'Амфитеатр' END,
       CASE WHEN r<=5 THEN 8000 ELSE 5000 END
FROM generate_series(1,15) s, generate_series(1,10) r;
