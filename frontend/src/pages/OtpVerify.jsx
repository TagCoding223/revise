import React, { useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';

export default function OtpVerify() {
  const [otp, setOtp] = useState(['', '', '', '']);
  const [isVerifying, setIsVerifying] = useState(false);
  const inputRefs = useRef([]);
  const navigate = useNavigate();

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
      // You could add a toast error notification here
      return;
    }

    setIsVerifying(true);

    try {
      // MOCK API CALL: Replace with your actual Spring Boot endpoint
      // await axios.post('/api/v1/auth/verify-otp', { otp: otpCode });
      
      console.log('Verifying OTP:', otpCode);
      await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate network delay
      
      // On success, redirect to set password
      navigate('/set-password');
    } catch (error) {
      console.error('OTP Verification failed', error);
      // Handle error state here
    } finally {
      setIsVerifying(false);
    }
  };

  const handleResend = () => {
    console.log('Resending OTP...');
    // Add logic to call your resend API endpoint
  };

  return (
    <div className="flex items-center justify-center min-h-[calc(100vh-8rem)]">
      <div className="w-full max-w-md bg-white dark:bg-gray-800 rounded-2xl shadow-xl overflow-hidden border border-gray-100 dark:border-gray-700 transition-colors duration-300 p-8">
        
        <div className="text-center mb-8">
          <h2 className="text-2xl font-bold text-gray-900 dark:text-white mb-2">
            Check your email
          </h2>
          <p className="text-gray-600 dark:text-gray-400 text-sm">
            We've sent a 4-digit verification code to your email address. Enter it below to verify your account.
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

        {/* Resend Option */}
        <div className="mt-6 text-center text-sm">
          <span className="text-gray-600 dark:text-gray-400">Didn't receive the code? </span>
          <button
            type="button"
            onClick={handleResend}
            className="text-blue-600 dark:text-blue-400 font-medium hover:underline focus:outline-none"
          >
            Resend OTP
          </button>
        </div>

      </div>
    </div>
  );
}