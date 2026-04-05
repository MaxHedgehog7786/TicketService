import React, { useState, useEffect, useCallback } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { eventsApi, reviewsApi, bookingApi, ticketsApi, profileApi, favoritesApi } from '../api';
import { useAuthStore, useFavoritesStore } from '../store';
import EventCard from '../components/Components';
import { SeatMap, GeneralAdmission, ReviewForm, PaymentModal } from '../components/Components';

// ── HomePage ───────────────────────────────────────────────────
const CATEGORIES = ['Все', 'Концерт', 'Спектакль', 'Фестиваль', 'Семейное', 'Выставка', 'Спорт'];
const CITIES = ['Все города', 'Москва', 'Санкт-Петербург', 'Казань', 'Екатеринбург'];

export function HomePage() {
  const navigate = useNavigate();
  const [upcoming, setUpcoming] = useState([]);
  const [events, setEvents] = useState([]);
  const [total, setTotal] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('Все');
  const [city, setCity] = useState('Все города');

  useEffect(() => {
    eventsApi.upcoming(6).then(({ data }) => setUpcoming(data));
  }, []);

  const doSearch = useCallback(async (q, cat, c, p = 0) => {
    setLoading(true);
    try {
      const params = {
        q: q || undefined,
        category: cat !== 'Все' ? cat : undefined,
        city: c !== 'Все города' ? c : undefined,
        page: p,
        size: 12,
      };
      const { data } = await eventsApi.search(params);
      if (p === 0) setEvents(data.content);
      else setEvents((prev) => [...prev, ...data.content]);
      setTotal(data.totalElements);
      setPage(p);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { doSearch(query, category, city, 0); }, [category, city]);

  return (
    <div>
      <section className="hero">
        <h1>Найдите своё мероприятие</h1>
        <p>Концерты, спектакли, фестивали — всё в одном месте</p>
        <div className="search-bar">
          <input
            className="search-input"
            placeholder="Поиск по названию или описанию..."
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && doSearch(query, category, city, 0)}
          />
          <button className="btn-gold" onClick={() => doSearch(query, category, city, 0)}>Найти</button>
        </div>
      </section>

      <div className="container">
        {upcoming.length > 0 && (
          <section className="section">
            <h2 className="section-title">🔥 Ближайшие события</h2>
            <div className="horizontal-scroll">
              {upcoming.map((e) => (
                <div key={e.id} className="mini-card" onClick={() => navigate(`/events/${e.id}`)}>
                  <img src={e.imageUrl} alt={e.title} />
                  <div className="mini-card-body">
                    <div className="mini-card-title">{e.title}</div>
                    <div className="mini-card-meta">{e.date} • {e.city}</div>
                    <div className="price-sm">от {e.price?.toLocaleString('ru-RU')} ₽</div>
                  </div>
                </div>
              ))}
            </div>
          </section>
        )}

        <div className="filters">
          {CATEGORIES.map((c) => (
            <button key={c} className={`chip ${category === c ? 'chip-active' : ''}`} onClick={() => setCategory(c)}>{c}</button>
          ))}
          <span className="divider">|</span>
          {CITIES.map((c) => (
            <button key={c} className={`chip ${city === c ? 'chip-active' : ''}`} onClick={() => setCity(c)}>{c}</button>
          ))}
        </div>

        <section className="section">
          <div className="section-header">
            <h2 className="section-title">Все мероприятия <span className="count">({total})</span></h2>
            {(query || category !== 'Все' || city !== 'Все города') && (
              <button className="btn-ghost" onClick={() => { setQuery(''); setCategory('Все'); setCity('Все города'); }}>
                Сбросить фильтры
              </button>
            )}
          </div>
          {loading && events.length === 0 ? (
            <div className="loading">Загрузка...</div>
          ) : events.length === 0 ? (
            <div className="empty">По вашему запросу ничего не найдено</div>
          ) : (
            <>
              <div className="events-grid">
                {events.map((e) => (
                  <EventCard key={e.id} event={e} onClick={() => navigate(`/events/${e.id}`)} />
                ))}
              </div>
              {events.length < total && (
                <div className="load-more">
                  <button className="btn-outline" onClick={() => doSearch(query, category, city, page + 1)} disabled={loading}>
                    {loading ? 'Загрузка...' : 'Показать ещё'}
                  </button>
                </div>
              )}
            </>
          )}
        </section>
      </div>
    </div>
  );
}

// ── Calendar helpers ───────────────────────────────────────────
function parseEventDate(dateStr) {
  // format: "15.05.2026 19:00"
  if (!dateStr) return null;
  const [datePart, timePart] = dateStr.split(' ');
  if (!datePart) return null;
  const [d, m, y] = datePart.split('.');
  const [hh, mm] = (timePart || '00:00').split(':');
  return new Date(+y, +m - 1, +d, +hh, +mm);
}

function toIcsDate(date) {
  if (!date) return '';
  const pad = (n) => String(n).padStart(2, '0');
  return `${date.getFullYear()}${pad(date.getMonth() + 1)}${pad(date.getDate())}T${pad(date.getHours())}${pad(date.getMinutes())}00`;
}

function addToGoogleCalendar(event) {
  const start = parseEventDate(event.date);
  if (!start) return;
  const end = new Date(start.getTime() + 2 * 60 * 60 * 1000);
  const fmt = (d) => toIcsDate(d).replace('T', 'T');
  const url = `https://calendar.google.com/calendar/render?action=TEMPLATE` +
    `&text=${encodeURIComponent(event.title)}` +
    `&dates=${fmt(start)}/${fmt(end)}` +
    `&location=${encodeURIComponent(event.venue + ', ' + event.city)}` +
    `&details=${encodeURIComponent('Билет куплен на TicketService')}`;
  window.open(url, '_blank');
}

function downloadIcs(event) {
  const start = parseEventDate(event.date);
  if (!start) return;
  const end = new Date(start.getTime() + 2 * 60 * 60 * 1000);
  const ics = [
    'BEGIN:VCALENDAR', 'VERSION:2.0', 'PRODID:-//TicketService//RU',
    'BEGIN:VEVENT',
    `DTSTART:${toIcsDate(start)}`,
    `DTEND:${toIcsDate(end)}`,
    `SUMMARY:${event.title}`,
    `LOCATION:${event.venue}, ${event.city}`,
    'DESCRIPTION:Билет куплен на TicketService',
    'END:VEVENT', 'END:VCALENDAR',
  ].join('\r\n');
  const blob = new Blob([ics], { type: 'text/calendar;charset=utf-8' });
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = `${event.title}.ics`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  setTimeout(() => URL.revokeObjectURL(url), 1000);
}

// ── EventPage ──────────────────────────────────────────────────
export function EventPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuthStore();
  const { isFav, toggle } = useFavoritesStore();

  const [event, setEvent] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [seats, setSeats] = useState([]);
  const [tab, setTab] = useState('seats');
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [showPayment, setShowPayment] = useState(false);
  const [paid, setPaid] = useState(false);
  const [loading, setLoading] = useState(true);
  const [editingReview, setEditingReview] = useState(null); // {id, rating, comment}
  const [showCalMenu, setShowCalMenu] = useState(false);

  useEffect(() => {
    Promise.all([
      eventsApi.getById(id),
      reviewsApi.list(id),
      eventsApi.getSeats(id),
    ]).then(([{ data: ev }, { data: rv }, { data: st }]) => {
      setEvent(ev.event || ev);
      setReviews(rv);
      setSeats(st);
      setLoading(false);
    });
  }, [id]);

  const handleBook = async () => {
    if (!isAuthenticated()) { navigate('/login'); return; }
    await bookingApi.reserve(selectedSeats.map((s) => s.id));
    setShowPayment(true);
  };

  const handlePay = async (paymentMethod) => {
    await bookingApi.confirm(selectedSeats.map((s) => s.id), paymentMethod, 'ext-' + Date.now());
    setPaid(true);
    setShowPayment(false);
    setSelectedSeats([]);
    const { data } = await eventsApi.getSeats(id);
    setSeats(data);
  };

  const handleReview = async ({ rating, comment }) => {
    const { data } = await reviewsApi.add(Number(id), rating, comment);
    setReviews((prev) => [data, ...prev]);
  };

  const handleEditReview = async () => {
    if (!editingReview) return;
    const { data } = await reviewsApi.update(editingReview.id, editingReview.rating, editingReview.comment);
    setReviews((prev) => prev.map((r) => r.id === data.id ? data : r));
    setEditingReview(null);
  };

  const handleDeleteReview = async (reviewId) => {
    await reviewsApi.remove(reviewId);
    setReviews((prev) => prev.filter((r) => r.id !== reviewId));
  };

  if (loading) return <div className="loading-page">Загрузка...</div>;
  if (!event) return <div className="error-page">Мероприятие не найдено</div>;

  const total = selectedSeats.reduce((sum, s) => sum + s.price, 0);
  const userId = user?.id;

  return (
    <div>
      <div className="detail-hero">
        <img src={event.imageUrl} alt={event.title} className="detail-img" />
        <div className="detail-overlay" />
        <div className="detail-info">
          <button className="btn-back" onClick={() => navigate(-1)}>← Назад</button>
          <div className="detail-badges">
            <span className={`badge badge-${event.category?.toLowerCase()}`}>{event.category}</span>
            {event.avgRating > 0 && <span className="rating-badge">★ {event.avgRating?.toFixed(1)}</span>}
            <button
              className={`fav-btn-detail ${isFav(event.id) ? 'fav-active' : ''}`}
              onClick={() => isAuthenticated() ? toggle(event.id) : navigate('/login')}
            >
              {isFav(event.id) ? '♥ В избранном' : '♡ В избранное'}
            </button>
          </div>
          <h1 className="detail-title">{event.title}</h1>
          <div className="detail-meta">
            <span>📍 {event.venue}, {event.city}</span>
            <span>📅 {event.date}</span>
            <span className="price-gold">от {event.price?.toLocaleString('ru-RU')} ₽</span>
          </div>
          <div className="cal-wrap">
            <button className="btn-cal" onClick={() => setShowCalMenu((v) => !v)}>
              📅 Добавить в календарь
            </button>
            {showCalMenu && (
              <div className="cal-dropdown">
                <button onClick={() => { addToGoogleCalendar(event); setShowCalMenu(false); }}>
                  Google Calendar
                </button>
                <button onClick={() => { downloadIcs(event); setShowCalMenu(false); }}>
                  Apple / iCal (.ics)
                </button>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="container detail-body">
        {paid && <div className="alert alert-success">✓ Оплата прошла успешно! Билеты доступны в личном кабинете.</div>}
        <p className="event-description">{event.description}</p>

        <div className="tabs">
          <button className={`tab ${tab === 'seats' ? 'tab-active' : ''}`} onClick={() => setTab('seats')}>Выбор мест</button>
          <button className={`tab ${tab === 'reviews' ? 'tab-active' : ''}`} onClick={() => setTab('reviews')}>
            Отзывы ({reviews.length})
          </button>
        </div>

        {tab === 'seats' && (() => {
          const GA_CATEGORIES = ['Фестиваль', 'Выставка', 'Семейное'];
          const isGA = GA_CATEGORIES.includes(event.category);
          return isGA
            ? <GeneralAdmission seats={seats} selected={selectedSeats} onSelect={setSelectedSeats} />
            : <SeatMap seats={seats} selected={selectedSeats} onSelect={setSelectedSeats} />;
        })()}

        {tab === 'seats' && selectedSeats.length > 0 && (
          <div className="booking-summary">
            <div className="booking-seats">
              {selectedSeats.map((s) => (
                <div key={s.id} className="booking-seat-row">
                  <span>Ряд {s.rowNumber}, Место {s.seatNumber} ({s.sector})</span>
                  <span>{s.price?.toLocaleString('ru-RU')} ₽</span>
                </div>
              ))}
            </div>
            <div className="booking-total">Итого: <strong>{total.toLocaleString('ru-RU')} ₽</strong></div>
            <button className="btn-gold btn-wide" onClick={handleBook}>Купить билеты</button>
          </div>
        )}

        {tab === 'reviews' && (
          <div>
            {isAuthenticated() && !reviews.find((r) => r.userId === user?.id) && (
              <ReviewForm onSubmit={handleReview} />
            )}
            {reviews.length === 0 && <div className="empty">Пока нет отзывов. Будьте первым!</div>}
            {reviews.map((r) => (
              <div key={r.id} className="review-card">
                {editingReview?.id === r.id ? (
                  <div className="review-edit-form">
                    <div className="star-picker">
                      {[1,2,3,4,5].map((n) => (
                        <button key={n}
                          className={`star-btn ${n <= editingReview.rating ? 'star-active' : ''}`}
                          onClick={() => setEditingReview((p) => ({ ...p, rating: n }))}>★</button>
                      ))}
                    </div>
                    <textarea className="inp" rows={3} value={editingReview.comment}
                      onChange={(e) => setEditingReview((p) => ({ ...p, comment: e.target.value }))} />
                    <div className="review-edit-actions">
                      <button className="btn-gold" onClick={handleEditReview}>Сохранить</button>
                      <button className="btn-ghost" onClick={() => setEditingReview(null)}>Отмена</button>
                    </div>
                  </div>
                ) : (
                  <>
                    <div className="review-header">
                      <div className="review-avatar">{r.userName?.[0]?.toUpperCase() || '?'}</div>
                      <div className="review-meta">
                        <div className="review-name">{r.userName || 'Пользователь'}</div>
                        <div className="review-date">{r.createdAt?.slice(0, 10)}</div>
                      </div>
                      <div className="review-rating">{'★'.repeat(r.rating)}{'☆'.repeat(5 - r.rating)}</div>
                      {isAuthenticated() && r.userId === user?.id && (
                        <div className="review-actions">
                          <button className="review-action-btn" onClick={() => setEditingReview({ id: r.id, rating: r.rating, comment: r.comment })}>✏</button>
                          <button className="review-action-btn review-del-btn" onClick={() => handleDeleteReview(r.id)}>✕</button>
                        </div>
                      )}
                    </div>
                    <p className="review-text">{r.comment}</p>
                  </>
                )}
              </div>
            ))}
          </div>
        )}
      </div>

      {showPayment && (
        <PaymentModal seats={selectedSeats} total={total} onPay={handlePay} onClose={() => setShowPayment(false)} />
      )}
    </div>
  );
}

// ── ProfilePage ────────────────────────────────────────────────
export function ProfilePage() {
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const { favorites } = useFavoritesStore();

  const [tab, setTab] = useState('tickets');
  const [tickets, setTickets] = useState([]);
  const [favoriteEvents, setFavoriteEvents] = useState([]);
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ name: user?.name || '', phone: '' });
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    ticketsApi.list().then(({ data }) => setTickets(data));
    favoritesApi.list().then(({ data }) => setFavoriteEvents(data));
  }, []);

  const saveProfile = async () => {
    await profileApi.update(form);
    setSaved(true);
    setEditing(false);
    setTimeout(() => setSaved(false), 3000);
  };

  return (
    <div className="container profile-page">
      <div className="profile-header">
        <div className="profile-avatar">{user?.name?.[0]}</div>
        <div className="profile-info">
          <h2>{user?.name}</h2>
          <div className="profile-email">{user?.login}</div>
        </div>
        <div className="profile-actions">
          <button className="btn-ghost" onClick={() => setEditing(!editing)}>
            {editing ? 'Отмена' : '✏ Редактировать'}
          </button>
          <button className="btn-danger" onClick={() => { logout(); navigate('/'); }}>Выйти</button>
        </div>
      </div>

      {saved && <div className="alert alert-success">✓ Данные сохранены</div>}

      {editing && (
        <div className="edit-form">
          <label>Имя</label>
          <input value={form.name} onChange={(e) => setForm((p) => ({ ...p, name: e.target.value }))} />
          <label>Телефон</label>
          <input value={form.phone} onChange={(e) => setForm((p) => ({ ...p, phone: e.target.value }))} />
          <button className="btn-gold" onClick={saveProfile}>Сохранить</button>
        </div>
      )}

      <div className="tabs">
        <button className={`tab ${tab === 'tickets' ? 'tab-active' : ''}`} onClick={() => setTab('tickets')}>
          Мои билеты ({tickets.length})
        </button>
        <button className={`tab ${tab === 'favorites' ? 'tab-active' : ''}`} onClick={() => setTab('favorites')}>
          Избранное ({favorites.length})
        </button>
      </div>

      {tab === 'tickets' && (
        tickets.length === 0 ? (
          <div className="empty">У вас пока нет купленных билетов</div>
        ) : (
          <div className="tickets-list">
            {tickets.map((t) => (
              <div key={t.id} className="ticket-card">
                <div className="ticket-info">
                  <div className="ticket-title">{t.eventTitle}</div>
                  <div className="ticket-meta">📅 {t.eventDate} &nbsp;|&nbsp; Ряд {t.rowNumber}, Место {t.seatNumber} • {t.sector}</div>
                </div>
                <div className="ticket-actions">
                  <div className="ticket-price">{t.price?.toLocaleString('ru-RU')} ₽</div>
                  <button className="btn-gold btn-sm" onClick={() => ticketsApi.downloadPdf(t.id)}>📥 PDF</button>
                </div>
              </div>
            ))}
          </div>
        )
      )}

      {tab === 'favorites' && (
        favoriteEvents.length === 0 ? (
          <div className="empty">Нет избранных мероприятий</div>
        ) : (
          <div className="events-grid">
            {favoriteEvents.map((e) => (
              <EventCard key={e.id} event={e} onClick={() => navigate(`/events/${e.id}`)} />
            ))}
          </div>
        )
      )}
    </div>
  );
}

// ── AuthPage ───────────────────────────────────────────────────
export function AuthPage({ mode: initialMode = 'login' }) {
  const navigate = useNavigate();
  const { login, register } = useAuthStore();
  const [mode, setMode] = useState(initialMode);
  const [form, setForm] = useState({
    login: '', password: '', name: '', surname: '', email: '', phone: '', subscribed: false,
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const set = (key) => (e) => setForm((p) => ({
    ...p,
    [key]: e.target.type === 'checkbox' ? e.target.checked : e.target.value,
  }));

  const submit = async () => {
    setError('');
    if (!form.login || !form.password) { setError('Заполните обязательные поля'); return; }
    setLoading(true);
    try {
      if (mode === 'login') await login({ login: form.login, password: form.password });
      else await register(form);
      navigate('/');
    } catch (err) {
      setError(err.response?.data?.error || 'Произошла ошибка');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-box">
        <div className="auth-logo">🎭 TicketService</div>
        <h2>{mode === 'login' ? 'Вход в аккаунт' : 'Регистрация'}</h2>
        {error && <div className="alert alert-error">{error}</div>}

        <label>Логин *</label>
        <input className="inp" placeholder="Ваш логин" value={form.login} onChange={set('login')} />

        {mode === 'register' && (
          <>
            <label>Имя *</label>
            <input className="inp" placeholder="Имя" value={form.name} onChange={set('name')} />
            <label>Фамилия *</label>
            <input className="inp" placeholder="Фамилия" value={form.surname} onChange={set('surname')} />
            <label>Email *</label>
            <input className="inp" type="email" placeholder="mail@example.ru" value={form.email} onChange={set('email')} />
            <label>Телефон</label>
            <input className="inp" placeholder="+7 (999) 000-00-00" value={form.phone} onChange={set('phone')} />
          </>
        )}

        <label>Пароль *</label>
        <input className="inp" type="password" placeholder="Не менее 8 символов" value={form.password} onChange={set('password')} />

        {mode === 'register' && (
          <label className="checkbox-label">
            <input type="checkbox" checked={form.subscribed} onChange={set('subscribed')} />
            Подписаться на рекомендательную рассылку
          </label>
        )}

        <button className="btn-gold btn-wide" onClick={submit} disabled={loading}>
          {loading ? 'Загрузка...' : mode === 'login' ? 'Войти' : 'Зарегистрироваться'}
        </button>

        <div className="auth-switch">
          {mode === 'login' ? 'Нет аккаунта? ' : 'Уже есть аккаунт? '}
          <button className="link-btn" onClick={() => setMode(mode === 'login' ? 'register' : 'login')}>
            {mode === 'login' ? 'Зарегистрироваться' : 'Войти'}
          </button>
        </div>
      </div>
    </div>
  );
}
