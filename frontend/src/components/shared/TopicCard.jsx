import React from 'react';

export function TopicCard({ topic, isActive, onView, onUpdate, onDelete, onRevise }) {
  return (
    <div 
      className={`p-5 rounded-xl border transition-all duration-300 flex flex-col justify-between ${
        isActive 
          ? 'bg-white dark:bg-gray-800 border-gray-200 dark:border-gray-700 shadow-sm' 
          : 'bg-gray-50/80 dark:bg-gray-800/40 border-gray-100 dark:border-gray-700/50 opacity-80'
      }`}
    >
      <div>
        <div className="flex justify-between items-start mb-2">
          <h3 className={`font-semibold text-lg ${isActive ? 'text-gray-900 dark:text-white' : 'text-gray-700 dark:text-gray-300'}`}>
            {topic.title}
          </h3>
          <span className="inline-flex items-center px-2.5 py-1 text-center py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800 dark:bg-blue-900/30 dark:text-blue-300">
            Stage {topic.stage}
          </span>
        </div>
        <p className={`text-sm mb-4 line-clamp-2 ${isActive ? 'text-gray-600 dark:text-gray-400' : 'text-gray-500 dark:text-gray-500'}`}>
          {topic.description}
        </p>
      </div>

      {/* Card Actions */}
      <div className="flex items-center gap-2 pt-4 border-t border-gray-100 dark:border-gray-700/50 mt-auto">
        
        {/* View Button */}
        <button 
          onClick={() => onView(topic)}
          className="p-2 text-gray-500 hover:text-blue-600 dark:text-gray-400 dark:hover:text-blue-400 transition-colors" 
          title="View Details"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
          </svg>
        </button>

        {/* Update Button */}
        <button 
          onClick={() => onUpdate(topic)}
          className="p-2 text-gray-500 hover:text-blue-600 dark:text-gray-400 dark:hover:text-blue-400 transition-colors" 
          title="Update"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
          </svg>
        </button>

        {/* Delete Button */}
        <button 
          onClick={() => onDelete(topic)}
          className="p-2 text-gray-500 hover:text-red-600 dark:text-gray-400 dark:hover:text-red-400 transition-colors" 
          title="Delete"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
          </svg>
        </button>

        {/* Revise Button */}
        <button 
          onClick={() => onRevise(topic.id)}
          disabled={!isActive}
          className={`ml-auto px-4 py-2 rounded-lg text-sm font-medium transition-colors flex items-center ${
            isActive 
              ? 'bg-green-600 hover:bg-green-700 text-white shadow-sm' 
              : 'bg-gray-200 dark:bg-gray-700 text-gray-400 dark:text-gray-500 cursor-not-allowed'
          }`}
        >
          <svg className="w-4 h-4 mr-1.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
          </svg>
          Revise
        </button>
      </div>
    </div>
  );
}