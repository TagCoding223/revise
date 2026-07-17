import React from 'react';

export default function DeleteConfirmModal({ isOpen, onClose, onConfirm, topicData }) {
  if (!isOpen || !topicData) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/50 dark:bg-black/60 backdrop-blur-sm">
      <div className="bg-white dark:bg-gray-800 rounded-2xl shadow-xl w-full max-w-sm overflow-hidden border border-gray-100 dark:border-gray-700 animate-in fade-in zoom-in-95 duration-200">
        <div className="p-6 text-center">
          
          <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 dark:bg-red-900/30 mb-4">
            <svg className="h-6 w-6 text-red-600 dark:text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          
          <h3 className="text-lg leading-6 font-bold text-gray-900 dark:text-white mb-2">
            Delete Revision Topic
          </h3>
          
          <p className="text-sm text-gray-500 dark:text-gray-400">
            Are you sure you want to delete <span className="font-semibold text-gray-700 dark:text-gray-300">"{topicData.title}"</span>? This action cannot be undone and will reset your spacing cycle for this topic.
          </p>

          <div className="mt-6 flex gap-3 justify-center">
            <button 
              type="button" 
              onClick={onClose}
              className="w-full inline-flex justify-center rounded-lg border border-gray-300 dark:border-gray-600 px-4 py-2 bg-white dark:bg-gray-800 text-sm font-medium text-gray-700 dark:text-gray-300 hover:bg-gray-50 dark:hover:bg-gray-700 focus:outline-none transition-colors"
            >
              Cancel
            </button>
            <button 
              type="button" 
              onClick={() => onConfirm(topicData.id)}
              className="w-full inline-flex justify-center rounded-lg border border-transparent px-4 py-2 bg-red-600 text-base font-medium text-white hover:bg-red-700 focus:outline-none sm:text-sm transition-colors"
            >
              Delete
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}