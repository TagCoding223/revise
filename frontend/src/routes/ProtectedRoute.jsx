import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function ProtectedRoute({ children }) {
  const { user } = useAuth();

  // If there is no authenticated user, redirect to the login page.
  // The 'replace' prop ensures this redirect doesn't add a meaningless entry 
  // to the browser's history stack, so the user's "Back" button works correctly.
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // If the user exists, render the protected component
  return children;
}