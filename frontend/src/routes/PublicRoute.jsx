import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function PublicRoute({ children }) {
  const { user } = useAuth();

  // If the user is already logged in, redirect them to their dashboard
  if (user) {
    // Intercept brand new Google users and send them to set-password
    if (user.newUser === true) {
      return <Navigate to="/set-password" replace />;
    }
    
    // Everyone else goes straight to the dashboard
    return <Navigate to="/dashboard" replace />;
  }

  // If no user is logged in, allow them to view the public page
  return children;
}