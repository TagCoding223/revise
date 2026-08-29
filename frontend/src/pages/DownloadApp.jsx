import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function DownloadApp() {
  const navigate = useNavigate();
  const [copied, setCopied] = useState(false);

  // APK file details (place 'revise.apk' in our React/Vite 'public/' folder)
  const APK_DOWNLOAD_URL = import.meta.env.VITE_APK_DOWNLOAD_URL || '';
  const APP_VERSION = import.meta.env.VITE_APP_VERSION || '';
  const APK_SIZE = import.meta.env.VITE_APK_SIZE || '';
  const MIN_ANDROID = import.meta.env.VITE_MIN_ANDROID || '';
  const SHA256_CHECKSUM = import.meta.env.VITE_SHA256_CHECKSUM || '';

  const handleCopyHash = () => {
    navigator.clipboard.writeText(SHA256_CHECKSUM);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const features = [
    {
      title: 'Offline-First Sync Engine',
      description: 'Review, edit, and track topics with zero network connection. Local changes sync automatically via delta batching when online.',
      icon: (
        <svg className="w-6 h-6 text-blue-600 dark:text-blue-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
        </svg>
      )
    },
    {
      title: 'Spaced Repetition Algorithm',
      description: 'Proven lifelong intervals (1d, 3d, 7d, 16d, 35d...) to ensure topics transition permanently into long-term memory.',
      icon: (
        <svg className="w-6 h-6 text-indigo-600 dark:text-indigo-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      )
    },
    {
      title: 'Daily Reminder Worker',
      description: 'Integrated background WorkManager pushes notifications for due topics without draining battery in the background.',
      icon: (
        <svg className="w-6 h-6 text-amber-600 dark:text-amber-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
        </svg>
      )
    },
    {
      title: 'Last-Write-Wins Conflict Shield',
      description: 'Seamless cross-platform synchronization between web dashboard and mobile app with UTC-anchored mutation logs.',
      icon: (
        <svg className="w-6 h-6 text-emerald-600 dark:text-emerald-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
        </svg>
      )
    }
  ];

  return (
    <div className="pb-16 relative animate-in fade-in duration-300">
      {/* Back to Dashboard Navigation */}
      <header className="mb-6 max-w-3xl mx-auto">
        <button
          onClick={() => navigate(-1)}
          className="inline-flex items-center text-sm font-medium text-gray-500 hover:text-blue-600 dark:text-gray-400 dark:hover:text-blue-400 transition-colors cursor-pointer"
        >
          <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Back
        </button>
      </header>

      {/* Hero Download Card */}
      <div className="max-w-3xl mx-auto bg-white dark:bg-gray-800 rounded-3xl shadow-xl overflow-hidden border border-gray-100 dark:border-gray-700 mb-10">
        <div className="p-8 sm:p-12 text-center">
          
          {/* App Icon */}
          <div className="mx-auto flex items-center justify-center h-28 w-28 rounded-3xl bg-gradient-to-tr from-emerald-500 to-blue-600 shadow-lg shadow-blue-500/20 mb-6 text-white transform hover:scale-105 transition-transform duration-300">
            <svg className="h-16 w-16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M17.523 15.3414c-.5511 0-.9993-.4486-.9993-.9997s.4482-.9993.9993-.9993c.551 0 .9993.4482.9993.9993 0 .5511-.4483.9997-.9993.9997m-11.046 0c-.5511 0-.9993-.4486-.9993-.9997s.4482-.9993.9993-.9993c.5511 0 .9994.4482.9994.9993 0 .5511-.4483.9997-.9994.9997m11.4045-6.02l1.996-3.4572c.1554-.269.0631-.6135-.2059-.7689-.269-.1554-.6135-.0631-.7689.2059l-2.0211 3.5008C15.3402 8.169 13.7228 7.8286 12 7.8286s-3.3402.3404-4.8816.9724L5.0973 5.3002c-.1554-.269-.4999-.3613-.7689-.2059-.269.1554-.3613.4999-.2059.7689l1.996 3.4572C2.6105 11.2587.318 14.8291 0 19.1429h24c-.318-4.3138-2.6105-7.8842-6.1185-9.8215" />
            </svg>
          </div>

          {/* Title and Badges */}
          <h1 className="text-3xl sm:text-4xl font-extrabold text-gray-900 dark:text-white tracking-tight mb-2">
            Revise for Android
          </h1>
          <p className="text-gray-500 dark:text-gray-400 text-base sm:text-lg max-w-xl mx-auto mb-6">
            Master long-term retention on the go with real-time offline synchronization and smart spaced repetition.
          </p>

          <div className="flex flex-wrap items-center justify-center gap-2 mb-8">
            <span className="px-3 py-1 bg-blue-50 dark:bg-blue-900/30 text-blue-700 dark:text-blue-300 text-xs font-semibold rounded-full border border-blue-200 dark:border-blue-800">
              {APP_VERSION}
            </span>
            <span className="px-3 py-1 bg-emerald-50 dark:bg-emerald-900/30 text-emerald-700 dark:text-emerald-300 text-xs font-semibold rounded-full border border-emerald-200 dark:border-emerald-800">
              {APK_SIZE}
            </span>
            <span className="px-3 py-1 bg-gray-100 dark:bg-gray-700 text-gray-700 dark:text-gray-300 text-xs font-semibold rounded-full border border-gray-200 dark:border-gray-600">
              {MIN_ANDROID}
            </span>
          </div>

          {/* Primary Action Button */}
          <div className="flex flex-col sm:flex-row items-center justify-center gap-4 mb-8">
            <a
              href={APK_DOWNLOAD_URL}
              download="Revise.apk"
              className="w-full sm:w-auto inline-flex items-center justify-center px-8 py-4 text-base font-semibold rounded-2xl text-white bg-blue-600 hover:bg-blue-700 shadow-lg shadow-blue-600/30 hover:shadow-blue-600/40 focus:outline-none focus:ring-4 focus:ring-blue-500/30 transition-all duration-200 cursor-pointer"
            >
              <svg className="w-5 h-5 mr-3 -ml-1 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
              </svg>
              Download APK Package
            </a>
          </div>

          {/* SHA-256 Checksum Card */}
          <div className="bg-gray-50 dark:bg-gray-900/60 rounded-xl p-4 border border-gray-200/70 dark:border-gray-700/70 max-w-xl mx-auto text-left">
            <div className="flex items-center justify-between mb-1.5">
              <span className="text-xs font-bold uppercase tracking-wider text-gray-500 dark:text-gray-400">
                Package SHA-256 Checksum
              </span>
              <button
                onClick={handleCopyHash}
                className="text-xs font-semibold text-blue-600 dark:text-blue-400 hover:underline inline-flex items-center gap-1 cursor-pointer"
              >
                {copied ? (
                  <>
                    <svg className="w-3.5 h-3.5 text-emerald-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                    </svg>
                    <span>Copied!</span>
                  </>
                ) : (
                  <>
                    <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z" />
                    </svg>
                    <span>Copy</span>
                  </>
                )}
              </button>
            </div>
            <code className="text-xs text-gray-700 dark:text-gray-300 font-mono break-all block">
              {SHA256_CHECKSUM}
            </code>
          </div>

        </div>
      </div>

      {/* Feature Grid */}
      <div className="max-w-3xl mx-auto mb-10">
        <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-6 text-center sm:text-left">
          Key Capabilities
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {features.map((feature, idx) => (
            <div 
              key={idx}
              className="p-6 bg-white dark:bg-gray-800 rounded-2xl border border-gray-100 dark:border-gray-700 shadow-sm hover:shadow-md transition-shadow duration-200"
            >
              <div className="h-12 w-12 rounded-xl bg-gray-50 dark:bg-gray-700/50 flex items-center justify-center mb-4">
                {feature.icon}
              </div>
              <h3 className="text-base font-bold text-gray-900 dark:text-white mb-1">
                {feature.title}
              </h3>
              <p className="text-sm text-gray-500 dark:text-gray-400 leading-relaxed">
                {feature.description}
              </p>
            </div>
          ))}
        </div>
      </div>

      {/* Installation Instructions Guide */}
      <div className="max-w-3xl mx-auto bg-white dark:bg-gray-800 rounded-2xl p-6 sm:p-8 border border-gray-100 dark:border-gray-700 shadow-sm">
        <h3 className="text-lg font-bold text-gray-900 dark:text-white mb-4">
          How to install the APK
        </h3>
        <ol className="space-y-3 text-sm text-gray-600 dark:text-gray-300">
          <li className="flex items-start">
            <span className="flex items-center justify-center h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/50 text-blue-600 dark:text-blue-400 font-bold text-xs mr-3 shrink-0">1</span>
            <span>Download the <code className="font-semibold text-gray-900 dark:text-white">Revise.apk</code> file to your Android device.</span>
          </li>
          <li className="flex items-start">
            <span className="flex items-center justify-center h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/50 text-blue-600 dark:text-blue-400 font-bold text-xs mr-3 shrink-0">2</span>
            <span>Open the downloaded file from your browser or notification bar.</span>
          </li>
          <li className="flex items-start">
            <span className="flex items-center justify-center h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/50 text-blue-600 dark:text-blue-400 font-bold text-xs mr-3 shrink-0">3</span>
            <span>If prompted, enable <em>"Allow installation from unknown sources"</em> for your browser or file manager.</span>
          </li>
          <li className="flex items-start">
            <span className="flex items-center justify-center h-6 w-6 rounded-full bg-blue-100 dark:bg-blue-900/50 text-blue-600 dark:text-blue-400 font-bold text-xs mr-3 shrink-0">4</span>
            <span>Tap <strong>Install</strong> and launch the app to start learning!</span>
          </li>
        </ol>
      </div>

    </div>
  );
}