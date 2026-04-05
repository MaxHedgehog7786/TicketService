# 🎭 TicketService — Веб-сервис подбора и покупки билетов

Проект выполнен по ТЗ в соответствии с ГОСТ 34.602-2020.

## Стек технологий

| Слой | Технология |
|------|-----------|
| Frontend | React 18, React Router 6, Zustand, Axios |
| Backend | Java 17, Spring Boot 3, Spring Security (JWT) |
| База данных | PostgreSQL 14+ |
| PDF | iText 7 |
| Rate Limiting | Bucket4j |
| Email | Spring Mail |

## Структура проекта

```
ticketservice/
├── backend/                    ← Spring Boot
│   ├── pom.xml
│   └── src/main/
│       ├── java/ru/ticketservice/
│       │   ├── TicketServiceApplication.java
│       │   ├── config/SecurityConfig.java    ← JWT, CORS, Rate Limit
│       │   ├── security/JwtService.java
│       │   ├── entity/Entities.java          ← User, Event, Seat, Order, Ticket...
│       │   ├── repository/Repositories.java  ← Spring Data JPA
│       │   ├── service/AuthService.java       ← Регистрация/Авторизация
│       │   ├── service/Services.java          ← Event, Booking, Review, PDF, Email
│       │   └── controller/Controllers.java   ← REST API
│       └── resources/
│           ├── application.properties
│           └── schema.sql                    ← DDL + тестовые данные
└── frontend/                   ← React
    ├── package.json
    └── src/
        ├── App.jsx              ← Роутер
        ├── index.css            ← Тёмная театральная тема
        ├── api/index.js         ← Axios API-слой
        ├── store/index.js       ← Zustand (auth, favorites)
        ├── pages/Pages.jsx      ← HomePage, EventPage, ProfilePage, AuthPage
        └── components/          ← Navbar, EventCard, SeatMap, PaymentModal, ReviewForm
```

## Быстрый старт

### 1. База данных
```bash
psql -U postgres -c "CREATE DATABASE ticketservice;"
psql -U postgres -d ticketservice -f backend/src/main/resources/schema.sql
```

### 2. Backend
```bash
cd backend
# Отредактируйте src/main/resources/application.properties
mvn spring-boot:run
# Сервер запустится на http://localhost:8080/api
```

### 3. Frontend
```bash
cd frontend
npm install
npm start
# Откроется http://localhost:3000
```

## REST API

| Метод | URL | Описание | Auth |
|-------|-----|----------|------|
| POST | /api/auth/register | Регистрация | — |
| POST | /api/auth/login | Авторизация → JWT | — |
| GET | /api/events | Поиск и фильтрация | — |
| GET | /api/events/upcoming | Ближайшие | — |
| GET | /api/events/{id} | Детали мероприятия | — |
| GET | /api/events/{id}/seats | Схема мест | — |
| POST | /api/favorites/{id} | Добавить в избранное | USER |
| DELETE | /api/favorites/{id} | Удалить из избранного | USER |
| GET | /api/favorites | Список избранного | USER |
| POST | /api/booking/reserve | Временное бронирование | USER |
| POST | /api/booking/confirm | Подтвердить оплату | USER |
| GET | /api/tickets | Купленные билеты | USER |
| GET | /api/tickets/{id}/pdf | Скачать PDF | USER |
| POST | /api/reviews | Оставить отзыв | USER |
| GET | /api/reviews/{eventId} | Отзывы мероприятия | — |
| PUT | /api/profile | Обновить профиль | USER |
| POST | /api/profile/subscribe | Подписка на рассылку | USER |

## Тестовые учётные данные

| Логин | Пароль | Роль |
|-------|--------|------|
| admin | password | ADMIN |
| maxim | password | USER |
| zlata | password | USER |

## Соответствие требованиям из матрицы

| Req | Реализация |
|-----|-----------|
| 1.1 Регистрация | `POST /auth/register` + `AuthService.register()` |
| 1.2 Авторизация | `POST /auth/login` + JWT |
| 1.3 Поиск | `GET /events?q=` + JPQL LIKE + pg_trgm index |
| 1.4 Фильтрация | `GET /events?category=&city=&from=&to=` |
| 1.5 Просмотр | `GET /events/{id}` + VIEW с рейтингом |
| 1.6 Избранное | `POST/DELETE /favorites/{id}` |
| 1.7 Покупка | `POST /booking/reserve` → `confirm` + @Transactional |
| 1.8 Мои билеты | `GET /tickets` |
| 1.9 PDF | `GET /tickets/{id}/pdf` + iText 7 |
| 1.10 Отзывы | `POST /reviews` |
| 1.11 Просмотр отзывов | `GET /reviews/{eventId}` |
| 1.13 Рассылка | `@Scheduled` + Spring Mail |
| 1.14 Календарь | Google Calendar deep link на фронте |
| 1.15 Редактирование | `PUT /profile` |
| 1.17 Ближайшие | `GET /events/upcoming` |
| 1.18 Обновление мест | @Scheduled `releaseExpiredReservations()` |
| 2.2 Производительность | JVM + pg indexes + connection pool |
| 2.5 Хэширование | SHA-256 в `AuthService.sha256()` |
| 2.6 Локализация | русский язык в CSS/HTML |
| 2.7 ФЗ-152 | разграничение доступа + HTTPS |
| 2.8 HTTPS | TLS на уровне инфраструктуры |
| 2.9 XSS | Spring Security headers + React escaping |
| 2.10 Rate Limiting | Bucket4j (100 req/min per IP) |
