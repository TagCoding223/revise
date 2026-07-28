import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { GoogleLogin } from '@react-oauth/google';
import axios from 'axios';
import { useAuth } from '../context/AuthContext';

const BACKEND_BASE_URL = import.meta.env.VITE_BACKEND_BASE_URL || '';

// --- Zod Validation Schemas ---
const loginSchema = z.object({
  email: z.string().email({ message: "Invalid email address" }),
  password: z.string().min(1, { message: "Password is required" }),
});

const signupSchema = z.object({
  fullName: z.string().min(2, { message: "Full name must be at least 2 characters" }),
  email: z.string().email({ message: "Invalid email address" }),
  password: z.string().min(8, { message: "Password must be at least 8 characters" }).regex(/^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9]).+$/, {
    message: "Password must contain at least one uppercase, lowercase, number, and symbol",
  }),
  confirmPassword: z.string(),
}).refine((data) => data.password === data.confirmPassword, {
  message: "Passwords do not match",
  path: ["confirmPassword"],
});

export default function Auth() {
  const location = useLocation();
  const navigate = useNavigate();
  
  const { login } = useAuth();

  // Tabs state tracking
  const isLoginRoute = location.pathname === '/login';
  const [activeTab, setActiveTab] = useState(isLoginRoute ? 'login' : 'signup');
  const [authError, setAuthError] = useState(''); // To handle API errors gracefully

  // Show/Hide password toggles
  const [showLoginPassword, setShowLoginPassword] = useState(false);
  const [showSignupPassword, setShowSignupPassword] = useState(false);

  // Sync state if URL changes directly
  useEffect(() => {
    setActiveTab(location.pathname === '/login' ? 'login' : 'signup');
    setAuthError(''); // Clear errors on tab switch
  }, [location.pathname]);

  const handleTabSwitch = (tab) => {
    setActiveTab(tab);
    navigate(`/${tab}`);
  };

  // --- Form Setups ---
  const {
    register: registerLogin,
    handleSubmit: handleLoginSubmit,
    formState: { errors: loginErrors },
  } = useForm({
    resolver: zodResolver(loginSchema),
  });

  const {
    register: registerSignup,
    handleSubmit: handleSignupSubmit,
    formState: { errors: signupErrors },
  } = useForm({
    resolver: zodResolver(signupSchema),
  });

  // --- Submission Handlers ---
  const onLogin = async (data) => {
    try {
      setAuthError('');
      const response = await axios.post(`${BACKEND_BASE_URL}/api/v1/auth/login`, data);
      login(response.data);
      navigate('/dashboard')
    } catch (error) {
      setAuthError(error.response?.data?.message || 'Login failed. Please try again.');
    }
  };

  const onSignup = async (data) => {
    try {
      setAuthError('');
      await axios.post(`${BACKEND_BASE_URL}/api/v1/auth/signup`, data);
      navigate('/verify-otp');
    } catch (error) {
      setAuthError(error.response?.data?.message || 'Signup failed. Please try again.');
    }
  };

  // --- Google OAuth Handler ---
  const handleGoogleSuccess = async (credentialResponse) => {
    try {
      setAuthError('');
      // Extract the ID token from the response
      const idToken = credentialResponse.credential;

      // Send the token to your Spring Boot endpoint
      const response = await axios.post(`${BACKEND_BASE_URL}/api/v1/auth/google`, { idToken });

      console.log("Backend Auth Response:", response.data);

      // Save the session in the global AuthContext
      login(response.data);

      // Route based on user status and Redirect to the protected dashboard or set-password
      if (response.data.newUser === true) {
        navigate('/set-password');
      } else {
        navigate('/dashboard');
      }
    } catch (error) {
      console.error("Google authentication failed", error);
      setAuthError(error.response?.data?.message || 'Google authentication failed.');
    }
  };

  const handleGoogleError = () => {
    setAuthError('Google login prompt closed or failed.');
  };

  return (
    <div className="flex items-center justify-center min-h-[calc(100vh-8rem)]">
      <div className="w-full max-w-md bg-white dark:bg-gray-800 rounded-2xl shadow-xl overflow-hidden border border-gray-100 dark:border-gray-700 transition-colors duration-300">

        {/* Tab Headers */}
        <div className="flex w-full border-b border-gray-200 dark:border-gray-700 relative">
          <button
            type="button"
            onClick={() => handleTabSwitch('login')}
            className={`flex-1 py-4 text-center font-semibold text-sm transition-colors duration-300 ${activeTab === 'login'
              ? 'text-blue-600 dark:text-blue-400'
              : 'text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200'
              }`}
          >
            Log In
          </button>
          <button
            type="button"
            onClick={() => handleTabSwitch('signup')}
            className={`flex-1 py-4 text-center font-semibold text-sm transition-colors duration-300 ${activeTab === 'signup'
              ? 'text-blue-600 dark:text-blue-400'
              : 'text-gray-500 hover:text-gray-700 dark:text-gray-400 dark:hover:text-gray-200'
              }`}
          >
            Sign Up
          </button>

          <div
            className="absolute bottom-0 h-0.5 bg-blue-600 dark:bg-blue-400 transition-all duration-300 ease-in-out w-1/2"
            style={{ left: activeTab === 'login' ? '0%' : '50%' }}
          />
        </div>

        {/* Global Auth Error Display */}
        {authError && (
          <div className="mx-6 sm:mx-8 mt-6 p-3 bg-red-100 border border-red-400 text-red-700 rounded-lg text-sm">
            {authError}
          </div>
        )}

        {/* Content Area */}
        <div className="p-6 sm:p-8 relative min-h-[440px]">

          {/* LOGIN FORM */}
          <div
            className={`absolute top-8 left-8 right-8 transition-all duration-500 ease-in-out ${activeTab === 'login'
              ? 'opacity-100 translate-x-0 pointer-events-auto z-10'
              : 'opacity-0 -translate-x-8 pointer-events-none z-0'
              }`}
          >
            <form onSubmit={handleLoginSubmit(onLogin)} className="space-y-5">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Email</label>
                <input
                  type="email"
                  {...registerLogin('email')}
                  className="w-full px-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-gray-900 dark:text-white"
                  placeholder="you@example.com"
                />
                {loginErrors.email && <p className="text-red-500 text-xs mt-1">{loginErrors.email.message}</p>}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Password</label>
                <div className="relative">
                  <input
                    type={showLoginPassword ? "text" : "password"}
                    {...registerLogin('password')}
                    className="w-full px-4 py-2 pr-10 bg-gray-50 dark:bg-gray-900 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-gray-900 dark:text-white"
                    placeholder="••••••••"
                  />
                  <button
                    type="button"
                    onClick={() => setShowLoginPassword(!showLoginPassword)}
                    className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 focus:outline-none"
                  >
                    {showLoginPassword ? (
                      <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                      </svg>
                    ) : (
                      <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    )}
                  </button>
                </div>
                {loginErrors.password && <p className="text-red-500 text-xs mt-1">{loginErrors.password.message}</p>}
              </div>

              <button
                type="submit"
                className="w-full py-2.5 px-4 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors"
              >
                Log In
              </button>
            </form>

            <div className="mt-6">
              <div className="relative mb-6">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-gray-200 dark:border-gray-700"></div>
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-2 bg-white dark:bg-gray-800 text-gray-500">Or continue with</span>
                </div>
              </div>

              {/* Official Google OAuth Login Button */}
              <div className="flex justify-center w-full">
                {activeTab === 'login' && (
                  <GoogleLogin
                    onSuccess={handleGoogleSuccess}
                    onError={handleGoogleError}
                    theme="outline"
                    size="large"
                    text="signin_with"
                    width="320" // Changed from "100%" to a pixel value
                  />
                )}
              </div>
            </div>
          </div>

          {/* SIGNUP FORM */}
          <div
            className={`transition-all duration-500 ease-in-out ${activeTab === 'signup'
              ? 'opacity-100 translate-x-0 pointer-events-auto z-10'
              : 'opacity-0 translate-x-8 pointer-events-none z-0 absolute top-8 left-8 right-8'
              }`}
          >
            <form onSubmit={handleSignupSubmit(onSignup)} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Full Name</label>
                <input
                  type="text"
                  {...registerSignup('fullName')}
                  className="w-full px-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-gray-900 dark:text-white"
                  placeholder="John Doe"
                />
                {signupErrors.fullName && <p className="text-red-500 text-xs mt-1">{signupErrors.fullName.message}</p>}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Email</label>
                <input
                  type="email"
                  {...registerSignup('email')}
                  className="w-full px-4 py-2 bg-gray-50 dark:bg-gray-900 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-gray-900 dark:text-white"
                  placeholder="you@example.com"
                />
                {signupErrors.email && <p className="text-red-500 text-xs mt-1">{signupErrors.email.message}</p>}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Password</label>
                <div className="relative">
                  <input
                    type={showSignupPassword ? "text" : "password"}
                    {...registerSignup('password')}
                    className="w-full px-4 py-2 pr-10 bg-gray-50 dark:bg-gray-900 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-gray-900 dark:text-white"
                    placeholder="••••••••"
                  />
                  <button
                    type="button"
                    onClick={() => setShowSignupPassword(!showSignupPassword)}
                    className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 focus:outline-none"
                  >
                    {showSignupPassword ? (
                      <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                      </svg>
                    ) : (
                      <svg className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                    )}
                  </button>
                </div>
                {signupErrors.password && <p className="text-red-500 text-xs mt-1">{signupErrors.password.message}</p>}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 dark:text-gray-300 mb-1">Confirm Password</label>
                <div className="relative">
                  <input
                    type={showSignupPassword ? "text" : "password"}
                    {...registerSignup('confirmPassword')}
                    className="w-full px-4 py-2 pr-10 bg-gray-50 dark:bg-gray-900 border border-gray-300 dark:border-gray-600 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none text-gray-900 dark:text-white"
                    placeholder="••••••••"
                  />
                </div>
                {signupErrors.confirmPassword && <p className="text-red-500 text-xs mt-1">{signupErrors.confirmPassword.message}</p>}
              </div>

              <button
                type="submit"
                className="w-full py-2.5 px-4 bg-blue-600 hover:bg-blue-700 text-white font-medium rounded-lg transition-colors mt-2"
              >
                Sign Up
              </button>
            </form>

            <div className="mt-6">
              <div className="relative mb-6">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-gray-200 dark:border-gray-700"></div>
                </div>
                <div className="relative flex justify-center text-sm">
                  <span className="px-2 bg-white dark:bg-gray-800 text-gray-500">Or sign up with</span>
                </div>
              </div>

              {/* Official Google OAuth Signup Button */}
              <div className="flex justify-center w-full">
                {activeTab === 'signup' && (
                  <GoogleLogin
                    onSuccess={handleGoogleSuccess}
                    onError={handleGoogleError}
                    theme="outline"
                    size="large"
                    text="signup_with"
                    width="320" // Changed from "100%" to a pixel value
                  />
                )}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}