import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children }) {
  const { user } = useAuth();
  const location = useLocation();

  // 1. Not logged in? Go to login.
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // 2. Logged in, but is a NEW user trying to access the dashboard? Force them to set password.
  if (user.newUser && location.pathname !== '/set-password') {
    return <Navigate to="/set-password" replace />;
  }

  // 3. Logged in, ALREADY set password, but trying to access set-password page? Force to dashboard.
  if (!user.newUser && location.pathname === '/set-password') {
    return <Navigate to="/dashboard" replace />;
  }

  // 4. Otherwise, render the requested page
  return children;
}