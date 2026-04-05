// src/api/index.js
// Все обращения к REST API бэкенда

import axios from 'axios';

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
});

// Автоматически добавляем JWT из localStorage
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Перехват 401 → редирект на логин
api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
    }
    return Promise.reject(err);
  }
);

// ── Auth ─────────────────────────────────────────────────────
export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
};

// ── Events ───────────────────────────────────────────────────
export const eventsApi = {
  search: (params) => api.get('/events', { params }),
  upcoming: (limit = 6) => api.get('/events/upcoming', { params: { limit } }),
  getById: (id) => api.get(`/events/${id}`),
  getSeats: (id) => api.get(`/events/${id}/seats`),
};

// ── Favorites ────────────────────────────────────────────────
export const favoritesApi = {
  list: () => api.get('/favorites'),
  add: (eventId) => api.post(`/favorites/${eventId}`),
  remove: (eventId) => api.delete(`/favorites/${eventId}`),
};

// ── Booking ──────────────────────────────────────────────────
export const bookingApi = {
  reserve: (seatIds) => api.post('/booking/reserve', { seatIds }),
  confirm: (seatIds, paymentMethod, externalPaymentId) =>
    api.post('/booking/confirm', { seatIds, paymentMethod, externalPaymentId }),
};

// ── Tickets ──────────────────────────────────────────────────
export const ticketsApi = {
  list: () => api.get('/tickets'),
  downloadPdf: (id) =>
    api.get(`/tickets/${id}/pdf`, { responseType: 'blob' }).then((res) => {
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const a = document.createElement('a');
      a.href = url;
      a.download = `ticket-${id}.pdf`;
      a.click();
      window.URL.revokeObjectURL(url);
    }),
};

// ── Reviews ──────────────────────────────────────────────────
export const reviewsApi = {
  list: (eventId) => api.get(`/reviews/${eventId}`),
  add: (eventId, rating, comment) =>
    api.post('/reviews', { eventId, rating, comment }),
  update: (id, rating, comment) =>
    api.put(`/reviews/${id}`, { rating, comment }),
  remove: (id) => api.delete(`/reviews/${id}`),
};

// ── Profile ──────────────────────────────────────────────────
export const profileApi = {
  update: (data) => api.put('/profile', data),
  subscribe: () => api.post('/profile/subscribe'),
};
