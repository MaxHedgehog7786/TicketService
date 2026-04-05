// src/App.jsx
import React, { useEffect } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Navbar } from './components/Components';
import { HomePage, EventPage, ProfilePage, AuthPage } from './pages/Pages';
import { ProtectedRoute } from './components/Components';
import { useAuthStore, useFavoritesStore } from './store';
import './index.css';

export default function App() {
  const { isAuthenticated } = useAuthStore();
  const { load } = useFavoritesStore();

  // Загружаем избранное при монтировании (если авторизованы)
  useEffect(() => { if (isAuthenticated()) load(); }, []);

  return (
    <BrowserRouter>
      <Navbar />
      <main>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/events/:id" element={<EventPage />} />
          <Route path="/login" element={<AuthPage mode="login" />} />
          <Route path="/register" element={<AuthPage mode="register" />} />
          <Route path="/profile" element={
            <ProtectedRoute><ProfilePage /></ProtectedRoute>
          } />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </main>
    </BrowserRouter>
  );
}
