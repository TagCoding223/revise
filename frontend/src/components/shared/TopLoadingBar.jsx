import React from 'react';

export default function TopLoadingBar() {
  return (
    <div className="fixed top-0 left-0 w-full h-1 z-50 bg-blue-100/30 dark:bg-gray-800 overflow-hidden">
      <div className="h-full w-1/2 bg-blue-600 animate-progress-bar rounded-full"></div>
    </div>
  );
}