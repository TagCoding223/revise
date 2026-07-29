import React, { useEffect, useState } from "react";

export default function Alert({ message, type, duration, onClose }) {
    const [isVisible, setIsVisible] = useState(false);
    const [isClosing, setIsClosing] = useState(false);

    useEffect(() => {
        // Trigger the slide-down animation slightly after mount
        const showTimer = setTimeout(() => setIsVisible(true), 10);

        // Trigger the close process when the duration finishes
        const closeTime = setTimeout(() => handleClose(), duration);

        return () => {
            clearTimeout(showTimer);
            clearTimeout(closeTime);
        };
    }, [duration]);

    const handleClose = () => {
        setIsVisible(false);
        setIsClosing(true);

        // Wait for the slide-up animation to finish before destroying the component
        setTimeout(onClose, 400);
    }

    // Dynamic styling based on alert type
    const typeConfig = {
        error: {
            bg: 'bg-red-50 dark:bg-red-900/20',
            border: 'border-red-200 dark:border-red-800/50',
            text: 'text-red-800 dark:text-red-300',
            icon: (
                <svg className="w-5 h-5 text-red-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            )
        },
        success: {
            bg: 'bg-green-50 dark:bg-green-900/20',
            border: 'border-green-200 dark:border-green-800/50',
            text: 'text-green-800 dark:text-green-300',
            icon: (
                <svg className="w-5 h-5 text-green-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            )
        },
        warning: {
            bg: 'bg-yellow-50 dark:bg-yellow-900/20',
            border: 'border-yellow-200 dark:border-yellow-800/50',
            text: 'text-yellow-800 dark:text-yellow-300',
            icon: (
                <svg className="w-5 h-5 text-yellow-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
            )
        },
        info: {
            bg: 'bg-gray-50 dark:bg-gray-800',
            border: 'border-gray-200 dark:border-gray-700',
            text: 'text-gray-800 dark:text-gray-300',
            icon: (
                <svg className="w-5 h-5 text-blue-500" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
            )
        }
    };

    const currentConfig = typeConfig[type] || typeConfig.info;

    return (
        <div
            className={`fixed top-6 left-1/2 -translate-x-1/2 z-[100] w-[calc(100%-2rem)] max-w-md transition-all duration-400 ease-in-out ${isVisible ? 'translate-y-0 opacity-100' : '-translate-y-10 opacity-0'
                }`}
        >
            <div className={`relative overflow-hidden rounded-xl border shadow-xl backdrop-blur-sm ${currentConfig.bg} ${currentConfig.border}`}>

                {/* Alert Content */}
                <div className="flex items-start p-4 gap-3">
                    <div className="shrink-0 mt-0.5">
                        {currentConfig.icon}
                    </div>
                    <div className="flex-1 break-words">
                        <p className={`text-sm font-medium leading-relaxed ${currentConfig.text}`}>
                            {message}
                        </p>
                    </div>

                    {/* Close Button */}
                    <button
                        onClick={handleClose}
                        className={`shrink-0 p-1 rounded-md opacity-60 hover:opacity-100 transition-opacity ${currentConfig.text}`}
                    >
                        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>

                {/* Animated Blue Progress Bar */}
                <div
                    className="absolute bottom-0 left-0 h-1 bg-blue-600 dark:bg-blue-400 animate-fill-bar"
                    style={{ animationDuration: `${duration}ms` }}
                />
            </div>
        </div>
    );
}