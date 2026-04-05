import React, { useState, useEffect } from 'react';
import { NavLink, useNavigate, Navigate } from 'react-router-dom';
import { useAuthStore, useFavoritesStore } from '../store';

// ── Theme ───────────────────────────────────────────────────────
function useTheme() {
  const [dark, setDark] = useState(() => localStorage.getItem('theme') !== 'light');
  useEffect(() => {
    document.documentElement.setAttribute('data-theme', dark ? 'dark' : 'light');
    localStorage.setItem('theme', dark ? 'dark' : 'light');
  }, [dark]);
  return [dark, () => setDark((d) => !d)];
}

// ── Navbar ─────────────────────────────────────────────────────
export function Navbar() {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuthStore();
  const [dark, toggleTheme] = useTheme();

  return (
    <nav className="navbar">
      <div className="navbar-logo" onClick={() => navigate('/')}>TicketService</div>
      <div className="navbar-links">
        <NavLink to="/" className={({ isActive }) => `nav-link ${isActive ? 'nav-active' : ''}`} end>
          Афиша
        </NavLink>
        {isAuthenticated() ? (
          <NavLink to="/profile" className={({ isActive }) => `nav-link ${isActive ? 'nav-active' : ''}`}>
            {user?.name}
          </NavLink>
        ) : (
          <>
            <NavLink to="/login" className="nav-link">Войти</NavLink>
            <NavLink to="/register" className="nav-link btn-gold-sm">Регистрация</NavLink>
          </>
        )}
        <button className="theme-toggle" onClick={toggleTheme} title={dark ? 'Светлая тема' : 'Тёмная тема'}>
          {dark ? '☀' : '☾'}
        </button>
      </div>
    </nav>
  );
}

// ── EventCard ──────────────────────────────────────────────────
export default function EventCard({ event, onClick }) {
  const navigate = useNavigate();
  const { isFav, toggle } = useFavoritesStore();
  const { isAuthenticated } = useAuthStore();

  const handleFav = (e) => {
    e.stopPropagation();
    if (!isAuthenticated()) { navigate('/login'); return; }
    toggle(event.id);
  };

  return (
    <div className="event-card" onClick={onClick}>
      <div className="card-img-wrap">
        <img src={event.imageUrl} alt={event.title} className="card-img" />
      </div>
      <div className="card-body">
        <div className="card-top">
          <span className={`badge badge-${event.category?.toLowerCase()}`}>{event.category}</span>
          {event.avgRating > 0 && (
            <span className="card-rating">★ {event.avgRating?.toFixed(1)}</span>
          )}
        </div>
        <h3 className="card-title">{event.title}</h3>
        <div className="card-meta">
          <span>📍 {event.city}</span>
          <span>📅 {event.date}</span>
        </div>
        <div className="card-footer">
          <div className="card-price-block">
            <div className="price-label">от</div>
            <div className="price">{event.price?.toLocaleString('ru-RU')} ₽</div>
          </div>
          <div className="card-actions">
            <button
              className={`fav-btn-card ${isFav(event.id) ? 'fav-active' : ''}`}
              onClick={handleFav}
              title={isFav(event.id) ? 'Убрать из избранного' : 'В избранное'}
            >
              {isFav(event.id) ? '♥' : '♡'}
            </button>
            <button className="btn-gold" onClick={onClick}>Выбрать место</button>
          </div>
        </div>
      </div>
    </div>
  );
}

// ── GeneralAdmission ──────────────────────────────────────────
export function GeneralAdmission({ seats, selected, onSelect }) {
  const sectors = [...new Set(seats.map((s) => s.sector))];
  const [activeSector, setActiveSector] = useState(sectors[0] || '');
  const [qty, setQty] = useState(1);

  const sectorSeats = seats.filter((s) => s.sector === activeSector && s.status === 'FREE');
  const sectorPrice = sectorSeats[0]?.price || 0;
  const available = sectorSeats.length;
  const maxQty = Math.min(10, available);

  const handleAdd = () => {
    const pick = sectorSeats.slice(0, qty);
    onSelect(pick);
  };

  return (
    <div className="general-admission">
      <h3 className="ga-title">Выберите зону</h3>
      <div className="ga-sectors">
        {sectors.map((sec) => {
          const free = seats.filter((s) => s.sector === sec && s.status === 'FREE').length;
          const price = seats.find((s) => s.sector === sec)?.price || 0;
          return (
            <button
              key={sec}
              className={`ga-sector-btn ${activeSector === sec ? 'ga-active' : ''}`}
              onClick={() => { setActiveSector(sec); setQty(1); onSelect([]); }}
            >
              <div className="ga-sector-name">{sec}</div>
              <div className="ga-sector-price">{price?.toLocaleString('ru-RU')} ₽</div>
              <div className="ga-sector-avail">{free} мест</div>
            </button>
          );
        })}
      </div>
      {activeSector && available > 0 && (
        <div className="ga-qty-row">
          <span className="ga-qty-label">Количество билетов:</span>
          <div className="ga-qty-ctrl">
            <button onClick={() => setQty((q) => Math.max(1, q - 1))} disabled={qty <= 1}>−</button>
            <span>{qty}</span>
            <button onClick={() => setQty((q) => Math.min(maxQty, q + 1))} disabled={qty >= maxQty}>+</button>
          </div>
          <span className="ga-total">{(sectorPrice * qty).toLocaleString('ru-RU')} ₽</span>
          <button className="btn-gold" onClick={handleAdd}>Добавить в корзину</button>
        </div>
      )}
      {available === 0 && <div className="empty">Нет свободных мест в этой зоне</div>}
    </div>
  );
}

// ── SeatMap ────────────────────────────────────────────────────
export function SeatMap({ seats, selected, onSelect }) {
  if (!seats.length) return <div className="empty">Места не загружены</div>;

  const rows = [...new Set(seats.map((s) => s.rowNumber))].sort((a, b) => a - b);
  const selectedIds = selected.map((s) => s.id);

  const toggle = (seat) => {
    if (seat.status !== 'FREE') return;
    const isSelected = selectedIds.includes(seat.id);
    onSelect(isSelected ? selected.filter((s) => s.id !== seat.id) : [...selected, seat]);
  };

  const getSeatClass = (seat) => {
    if (selectedIds.includes(seat.id)) return 'seat seat-selected';
    if (seat.status === 'SOLD') return 'seat seat-sold';
    if (seat.status === 'RESERVED') return 'seat seat-reserved';
    return 'seat seat-free';
  };

  return (
    <div className="seat-map">
      <div className="stage-label">Сцена</div>
      <div className="seat-rows">
        {rows.map((row) => (
          <div key={row} className="seat-row">
            <span className="row-label">{row}</span>
            {seats.filter((s) => s.rowNumber === row).map((seat) => (
              <button
                key={seat.id}
                className={getSeatClass(seat)}
                onClick={() => toggle(seat)}
                title={`Ряд ${seat.rowNumber}, Место ${seat.seatNumber} (${seat.sector}) — ${seat.price?.toLocaleString('ru-RU')} ₽`}
                disabled={seat.status !== 'FREE'}
              >
                {seat.seatNumber}
              </button>
            ))}
          </div>
        ))}
      </div>
      <div className="seat-legend">
        <div className="legend-item"><div className="legend-dot dot-free" /> Свободно</div>
        <div className="legend-item"><div className="legend-dot dot-selected" /> Выбрано</div>
        <div className="legend-item"><div className="legend-dot dot-reserved" /> Временно занято</div>
        <div className="legend-item"><div className="legend-dot dot-sold" /> Продано</div>
      </div>
    </div>
  );
}

// ── ReviewForm ─────────────────────────────────────────────────
export function ReviewForm({ onSubmit }) {
  const [rating, setRating] = useState(5);
  const [comment, setComment] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const submit = async () => {
    if (!comment.trim()) return;
    setSubmitting(true);
    try {
      await onSubmit({ rating, comment });
      setComment('');
      setRating(5);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="review-form">
      <h3>Оставить отзыв</h3>
      <div className="star-picker">
        {[1, 2, 3, 4, 5].map((n) => (
          <button key={n} className={`star-btn ${n <= rating ? 'star-active' : ''}`} onClick={() => setRating(n)}>★</button>
        ))}
      </div>
      <textarea
        className="inp"
        rows={3}
        placeholder="Расскажите о вашем опыте..."
        value={comment}
        onChange={(e) => setComment(e.target.value)}
      />
      <button className="btn-gold" onClick={submit} disabled={submitting || !comment.trim()}>
        {submitting ? 'Отправка...' : 'Отправить отзыв'}
      </button>
    </div>
  );
}

// ── PaymentModal ───────────────────────────────────────────────
export function PaymentModal({ seats, total, onPay, onClose }) {
  const [card, setCard] = useState('');
  const [expiry, setExpiry] = useState('');
  const [cvv, setCvv] = useState('');
  const [processing, setProcessing] = useState(false);

  const formatCard = (v) => v.replace(/\D/g, '').replace(/(.{4})/g, '$1 ').trim().slice(0, 19);
  const formatExpiry = (v) => v.replace(/\D/g, '').replace(/^(.{2})(.+)/, '$1/$2').slice(0, 5);

  const pay = async () => {
    if (card.replace(/\s/g, '').length < 16) return;
    setProcessing(true);
    try {
      await onPay('CARD');
    } finally {
      setProcessing(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-box" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Оплата билетов</h3>
          <button className="close-btn" onClick={onClose}>✕</button>
        </div>
        <div className="payment-seats">
          {seats.map((s) => (
            <div key={s.id} className="payment-row">
              <span>Ряд {s.rowNumber}, Место {s.seatNumber} ({s.sector})</span>
              <span>{s.price?.toLocaleString('ru-RU')} ₽</span>
            </div>
          ))}
          <div className="payment-total">
            <strong>Итого</strong>
            <strong>{total.toLocaleString('ru-RU')} ₽</strong>
          </div>
        </div>
        <label className="inp-label">Номер карты</label>
        <input className="inp" placeholder="0000 0000 0000 0000" value={card}
          onChange={(e) => setCard(formatCard(e.target.value))} maxLength={19} />
        <div className="inp-row">
          <div>
            <label className="inp-label">Срок действия</label>
            <input className="inp" placeholder="ММ/ГГ" value={expiry}
              onChange={(e) => setExpiry(formatExpiry(e.target.value))} maxLength={5} />
          </div>
          <div>
            <label className="inp-label">CVV</label>
            <input className="inp" type="password" placeholder="•••" value={cvv}
              onChange={(e) => setCvv(e.target.value.slice(0, 3))} maxLength={3} />
          </div>
        </div>
        <button className="btn-gold btn-wide" onClick={pay} disabled={processing}>
          {processing ? 'Обработка...' : `Оплатить ${total.toLocaleString('ru-RU')} ₽`}
        </button>
      </div>
    </div>
  );
}

// ── ProtectedRoute ─────────────────────────────────────────────
export function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuthStore();
  return isAuthenticated() ? children : <Navigate to="/login" replace />;
}
