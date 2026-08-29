import React, { useState, useRef, useEffect } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import api from '../api/axiosConfig';
import { useAlert } from '../context/AlertContext';
import { useAuth } from '../context/AuthContext';
import { Helmet } from 'react-helmet-async';

const BACKEND_BASE_URL = import.meta.env.VITE_BACKEND_BASE_URL || '';

export default function OtpVerify() {
  const [otp, setOtp] = useState(['', '', '', '']);
  const [isVerifying, setIsVerifying] = useState(false);
  const [isResending, setIsResending] = useState(false);

  // Timer State: 2 mins 10 secs = 130 seconds
  const [timeLeft, setTimeLeft] = useState(130);

  const inputRefs = useRef([]);
  const navigate = useNavigate();
  const location = useLocation();

  const { showAlert } = useAlert();
  const { login } = useAuth();

  // Extract the email passed from the Auth.jsx signup page
  const email = location.state?.email;

  // Fallback: If someone navigates here directly without signing up, bounce them back
  useEffect(() => {
    if (!email) {
      showAlert("No email provided. Please sign up first.", "warning");
      navigate('/signup');
    }
  }, [email, navigate, showAlert]);

  // Timer Countdown Effect
  useEffect(() => {
    // Stop counting if time runs out
    if (timeLeft <= 0) return;

    const timerId = setInterval(() => {
      setTimeLeft((prevTime) => prevTime - 1);
    }, 1000);

    // Cleanup interval on unmount
    return () => clearInterval(timerId);
  }, [timeLeft]);

  // Helper to format seconds into MM:SS
  const formatTime = (seconds) => {
    const m = Math.floor(seconds / 60);
    const s = seconds % 60;
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  // Handle individual input changes
  const handleChange = (index, e) => {
    const value = e.target.value;

    // Allow only numeric input
    if (isNaN(value)) return;

    const newOtp = [...otp];
    // Take only the last character if multiple are pasted/typed quickly
    newOtp[index] = value.substring(value.length - 1);
    setOtp(newOtp);

    // Move focus to the next input field automatically
    if (value && index < 3) {
      inputRefs.current[index + 1].focus();
    }
  };

  // Handle backspace to move focus to the previous input
  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1].focus();
    }
  };

  // Handle pasting a full 4-digit code
  const handlePaste = (e) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text').slice(0, 4).split('');

    if (pastedData.some(isNaN)) return;

    const newOtp = [...otp];
    pastedData.forEach((char, index) => {
      newOtp[index] = char;
    });
    setOtp(newOtp);

    // Focus the last filled input
    const focusIndex = pastedData.length < 4 ? pastedData.length : 3;
    inputRefs.current[focusIndex].focus();
  };

  const handleVerify = async (e) => {
    e.preventDefault();
    const otpCode = otp.join('');

    if (otpCode.length < 4) {
      showAlert("Please enter the complete 4-digit code.", "warning");
      return;
    }

    setIsVerifying(true);

    try {
      // Send parameters exactly as Spring Boot's @RequestParam expects
      const response = await api.post(`${BACKEND_BASE_URL}/api/v1/auth/verify-otp`, null, {
        params: { email, otp: otpCode }
      });

      // Save the session in context
      login(response.data);

      showAlert("Account verified successfully! Welcome to Revise.", "success", 5000);

      // Redirect to dashboard (Local signup already set a password, no need for set-password page)
      navigate('/dashboard');

    } catch (error) {
      console.error('OTP Verification failed', error);
      showAlert(error.response?.data?.message || 'Verification failed. Please check the code and try again.', 'error');
    } finally {
      setIsVerifying(false);
    }
  };

  const handleResend = async () => {
    // Double-check to prevent resending if timer hasn't finished
    if (timeLeft > 0) return;

    setIsResending(true);

    try {
      await api.post(`${BACKEND_BASE_URL}/api/v1/auth/resend-otp`, null, {
        params: { email }
      });

      showAlert("A new verification code has been sent to your email.", "info", 5000);

      // Reset the timer back to 130 seconds (2m 10s)
      setTimeLeft(130);
    } catch (error) {
      console.error('Resend failed', error);
      showAlert(error.response?.data?.message || 'Failed to resend the code. Please try again.', 'error');
    } finally {
      setIsResending(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-[calc(100vh-8rem)]">
      <Helmet>
        <title>Verify Account | Revise</title>
        <meta name="description" content="Enter your one-time password (OTP) to verify your Revise account and start your spaced repetition journey." />
      </Helmet>
      <div className="w-full max-w-md bg-white dark:bg-gray-800 rounded-2xl shadow-xl overflow-hidden border border-gray-100 dark:border-gray-700 transition-colors duration-300 p-8">

        <div className="text-center mb-8">
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
            Check your email
          </h2>
          <p className="text-gray-600 dark:text-gray-400 text-sm">
            We've sent a 4-digit verification code to <span className="font-semibold text-gray-800 dark:text-gray-200">{email}</span>. Enter it below to verify your account.
          </p>
        </div>

        <form onSubmit={handleVerify} className="space-y-8">

          {/* OTP Input Boxes */}
          <div className="flex justify-center gap-4">
            {otp.map((digit, index) => (
              <input
                key={index}
                ref={(el) => (inputRefs.current[index] = el)}
                type="text"
                inputMode="numeric"
                autoComplete="one-time-code"
                maxLength={1}
                value={digit}
                onChange={(e) => handleChange(index, e)}
                onKeyDown={(e) => handleKeyDown(index, e)}
                onPaste={handlePaste}
                className="w-14 h-16 text-center text-2xl font-bold bg-gray-50 dark:bg-gray-900 border border-gray-300 dark:border-gray-600 rounded-xl focus:ring-2 focus:ring-blue-500 outline-none text-gray-900 dark:text-white transition-all shadow-sm"
              />
            ))}
          </div>

          {/* Action Button */}
          <button
            type="submit"
            disabled={isVerifying || otp.join('').length < 4}
            className="w-full py-3 px-4 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-400 dark:disabled:bg-blue-800 disabled:cursor-not-allowed text-white font-medium rounded-lg transition-colors flex justify-center items-center"
          >
            {isVerifying ? (
              <svg className="animate-spin h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
            ) : (
              'Verify Account'
            )}
          </button>
        </form>

        {/* Resend Option with Timer */}
        <div className="mt-6 text-center text-sm">
          {timeLeft > 0 ? (
            <span className="text-gray-500 dark:text-gray-400">
              Resend code in <span className="font-semibold text-blue-600 dark:text-blue-400">{formatTime(timeLeft)}</span>
            </span>
          ) : (
            <>
              <span className="text-gray-600 dark:text-gray-400">Didn't receive the code? </span>
              <button
                type="button"
                onClick={handleResend}
                disabled={isResending}
                className="text-blue-600 dark:text-blue-400 font-medium hover:underline focus:outline-none disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {isResending ? 'Sending...' : 'Resend OTP'}
              </button>
            </>
          )}
        </div>

      </div>
    </div>
  );
}