import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
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
                const response = await axios.get(`${BACKEND_BASE_URL}/api/v1/users/me`);
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
                            <p className="text-gray-500 dark:text-gray-400 font-medium mb-6">
                                {profileData.email}
                            </p>

                            {/* Badge showing Auth Type */}
                            <div className="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold bg-gray-100 dark:bg-gray-700 text-gray-600 dark:text-gray-300 mb-8">
                                {profileData.authProvider === 'GOOGLE' ? (
                                    <>
                                        <svg className="w-3.5 h-3.5 mr-1.5" viewBox="0 0 24 24" fill="currentColor">
                                            <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" />
                                            <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" />
                                            <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" />
                                            <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" />
                                        </svg>
                                        Google Account
                                    </>
                                ) : (
                                    <>
                                        <svg className="w-3.5 h-3.5 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                                        </svg>
                                        Email Account
                                    </>
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
                            {profileData?.authProvider === 'GOOGLE' ? 'Set Local Password' : 'Change Password'}
                        </button>
                    </div>

                </div>
            </div>
        </div>
    );
}