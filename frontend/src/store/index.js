// src/store/index.js
// Глобальное состояние приложения (Zustand)

import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import { authApi, favoritesApi } from '../api';

export const useAuthStore = create(
  persist(
    (set, get) => ({
      user: null,
      token: null,

      login: async (credentials) => {
        const { data } = await authApi.login(credentials);
        localStorage.setItem('token', data.token);
        set({ user: { id: data.id, name: data.name, login: data.login, role: data.role }, token: data.token });
      },

      register: async (userData) => {
        const { data } = await authApi.register(userData);
        localStorage.setItem('token', data.token);
        set({ user: { id: data.id, name: data.name, login: data.login, role: data.role }, token: data.token });
      },

      logout: () => {
        localStorage.removeItem('token');
        set({ user: null, token: null });
      },

      isAuthenticated: () => !!get().token,
    }),
    { name: 'auth-storage', partialize: (s) => ({ user: s.user, token: s.token }) }
  )
);

export const useFavoritesStore = create((set, get) => ({
  favorites: [],        // массив eventId
  loaded: false,

  load: async () => {
    if (!useAuthStore.getState().isAuthenticated()) return;
    try {
      const { data } = await favoritesApi.list();
      set({ favorites: data.map((e) => e.id), loaded: true });
    } catch {}
  },

  toggle: async (eventId) => {
    const favs = get().favorites;
    const isFav = favs.includes(eventId);
    // Оптимистичное обновление
    set({ favorites: isFav ? favs.filter((id) => id !== eventId) : [...favs, eventId] });
    try {
      if (isFav) await favoritesApi.remove(eventId);
      else await favoritesApi.add(eventId);
    } catch {
      // Откат при ошибке
      set({ favorites: favs });
    }
  },

  isFav: (eventId) => get().favorites.includes(eventId),
}));
