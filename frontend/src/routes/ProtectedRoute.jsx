import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children }) {
  const { user } = useAuth();

  // If there is no authenticated user, redirect to the login page.
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // If the user exists, render the protected component
  return children;
}