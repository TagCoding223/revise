import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axiosConfig';
import TopLoadingBar from '../components/shared/TopLoadingBar';
import { useAlert } from '../context/AlertContext';

const BACKEND_BASE_URL = import.meta.env.VITE_BACKEND_BASE_URL || '';

export default function Profile() {
    const [profileData, setProfileData] = useState(null);
    const [isLoading, setIsLoading] = useState(true);
    const navigate = useNavigate();
    const { showAlert } = useAlert();

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const response = await api.get(`${BACKEND_BASE_URL}/api/v1/users/me`);
                setProfileData(response.data);
            } catch (error) {
                console.error("Failed to load profile:", error);
                showAlert(error.response?.data?.message || "Failed to load profile details.", "error");
            } finally {
                setIsLoading(false);
            }
        };

        fetchProfile();
    }, [showAlert]);

    if (isLoading) {
        return <TopLoadingBar />;
    }

    return (
        <div className="pb-10 relative animate-in fade-in duration-300">
            {/* Header with Back Button */}
            <header className="mb-8">
                <button
                    onClick={() => navigate('/dashboard')}
                    className="inline-flex items-center text-sm font-medium text-gray-500 hover:text-blue-600 dark:text-gray-400 dark:hover:text-blue-400 transition-colors cursor-pointer"
                >
                    <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
                    </svg>
                    Back to Dashboard
                </button>
            </header>

            {/* Profile Card */}
            <div className="max-w-2xl mx-auto bg-white dark:bg-gray-800 rounded-2xl shadow-xl overflow-hidden border border-gray-100 dark:border-gray-700">
                <div className="p-8 sm:p-12 text-center">
                    
                    {/* Big Profile SVG Icon */}
                    <div className="mx-auto flex items-center justify-center h-32 w-32 rounded-full bg-blue-50 dark:bg-gray-700 border-4 border-white dark:border-gray-800 shadow-md mb-6 text-blue-600 dark:text-blue-400">
                        <svg className="h-16 w-16" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                        </svg>
                    </div>

                    {/* User Details */}
                    {profileData ? (
                        <>
                            <h1 className="text-3xl font-bold text-gray-900 dark:text-white mb-2">
                                {profileData.fullName || "Student"}
                            </h1>
                            
                            <div className="flex items-center justify-center gap-2 mb-8">
                                <p className="text-gray-500 dark:text-gray-400 font-medium text-lg">
                                    {profileData.email}
                                </p>
                                
                                {/* Verification Badge */}
                                {profileData.emailVerified ? (
                                    <div className="flex items-center text-blue-500" title="Email Verified">
                                        {/* Blue Tick Badge */}
                                        <svg className="w-6 h-6" viewBox="0 0 24 24" fill="currentColor">
                                            <path d="M12 2C6.5 2 2 6.5 2 12s4.5 10 10 10 10-4.5 10-10S17.5 2 12 2zm-1.9 14.7L6 12.6l1.5-1.5 2.6 2.6 6.4-6.4 1.5 1.5-8.1 7.9z" />
                                        </svg>
                                    </div>
                                ) : (
                                    <div className="flex items-center text-amber-500 bg-amber-50 dark:bg-amber-900/20 px-2.5 py-1 rounded-full text-xs font-semibold" title="Email Unverified">
                                        <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                                        </svg>
                                        Unverified
                                    </div>
                                )}
                            </div>
                        </>
                    ) : (
                        <div className="animate-pulse space-y-4 max-w-sm mx-auto mb-8">
                            <div className="h-8 bg-gray-200 dark:bg-gray-700 rounded w-3/4 mx-auto"></div>
                            <div className="h-4 bg-gray-200 dark:bg-gray-700 rounded w-1/2 mx-auto"></div>
                        </div>
                    )}

                    {/* Actions */}
                    <div className="border-t border-gray-100 dark:border-gray-700 pt-8 flex flex-col sm:flex-row gap-4 justify-center">
                        <button
                            onClick={() => navigate('/set-password')}
                            className="inline-flex items-center justify-center px-6 py-3 bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 hover:bg-gray-50 dark:hover:bg-gray-700 text-gray-700 dark:text-gray-200 text-sm font-medium rounded-xl shadow-sm transition-colors cursor-pointer"
                        >
                            <svg className="w-4 h-4 mr-2 text-gray-500 dark:text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z" />
                            </svg>
                            Change Password
                        </button>
                    </div>

                </div>
            </div>
        </div>
    );
}