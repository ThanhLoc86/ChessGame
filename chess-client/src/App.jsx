import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import Lobby from './pages/Lobby';
import Game from './pages/Game';
import Profile from './pages/Profile';

function RequireAuth({ children }) {
  const token = localStorage.getItem('token');
  if (!token) return <Navigate to="/login" replace />;
  return children;
}

export default function App(){
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login/>} />
        <Route path="/register" element={<Register/>} />
        <Route path="/lobby" element={<RequireAuth><Lobby/></RequireAuth>} />
        <Route path="/game" element={<RequireAuth><Game/></RequireAuth>} />
        <Route path="/profile" element={<RequireAuth><Profile/></RequireAuth>} />
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}


