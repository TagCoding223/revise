import React, { Suspense, lazy } from 'react';
import { Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import ProtectedRoute from './routes/ProtectedRoute';
import PublicRoute from './routes/PublicRoute';
import TopLoadingBar from './components/shared/TopLoadingBar';

// 1. Dynamically import pages using React.lazy
const Landing = lazy(() => import('./pages/Landing'));
const Auth = lazy(() => import('./pages/Auth')); // Reused for Login/Signup
const OtpVerify = lazy(() => import('./pages/OtpVerify'));
const Dashboard = lazy(() => import('./pages/Dashboard'));
const SetPassword = lazy(() => import('./pages/SetPassword'));
const Profile = lazy(() => import('./pages/Profile'));

export default function App() {
  return (
    <div className="min-h-screen">
      <Navbar />
      
      <main className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* 2. Wrap the Routes in Suspense, passing the loading bar to the fallback prop */}
        <Suspense fallback={<TopLoadingBar />}>
          <Routes>
            {/* Public Routes */}
            <Route 
              path="/" 
              element={
                <PublicRoute>
                  <Landing />
                </PublicRoute>
              } 
            />
            <Route 
              path="/login" 
              element={
                <PublicRoute>
                  <Auth />
                </PublicRoute>
              } 
            />
            <Route 
              path="/signup" 
              element={
                <PublicRoute>
                  <Auth />
                </PublicRoute>
              } 
            />
            <Route 
              path="/verify-otp" 
              element={
                <PublicRoute>
                  <OtpVerify />
                </PublicRoute>
              } 
            />

            {/* Protected Routes */}
            <Route 
              path="/dashboard" 
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/set-password" 
              element={
                <ProtectedRoute>
                  <SetPassword />
                </ProtectedRoute>
              } 
            />
            <Route 
              path="/profile" 
              element={
                <ProtectedRoute>
                  <Profile />
                </ProtectedRoute>
              } 
            />
          </Routes>
        </Suspense>
      </main>
    </div>
  );
}