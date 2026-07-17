import React, { useState, Suspense, lazy } from 'react';
import { TopicCard } from '../components/shared/TopicCard';
import TopLoadingBar from '../components/shared/TopLoadingBar'; // Added missing import

// Lazy loaded modals
const CreateRevisionModal = lazy(() => import('../components/modals/CreateRevisionModal'));
const UpdateRevisionModal = lazy(() => import('../components/modals/UpdateRevisionModal'));
const ViewRevisionModal = lazy(() => import('../components/modals/ViewRevisionModal'));
const DeleteConfirmModal = lazy(() => import('../components/modals/DeleteConfirmModal'));

// Updated mock data utilizing arrays for links
const initialTopics = [
    {
        id: 'uuid-1',
        title: 'Java Class Loader Subsystem',
        description: 'Workflow and inner workings of the loading phase. Focus on system environment classpath variable overrides and handling file-based inputs.',
        links: ['https://docs.oracle.com/javase/specs/', 'https://www.baeldung.com/java-classloaders'],
        stage: 3,
        category: 'today'
    },
    {
        id: 'uuid-2',
        title: 'DBMS Normalization Forms',
        description: 'Differences between 3NF, 4NF, and 5NF with practical examples of data anomalies.',
        links: [],
        stage: 1,
        category: 'today'
    },
    {
        id: 'uuid-3',
        title: 'Tomcat Servlets & WEB-INF',
        description: 'Directory structure routing and hidden configuration files behavior when deleted.',
        links: [],
        stage: 2,
        category: 'tomorrow'
    },
    {
        id: 'uuid-4',
        title: 'Algorithm Design Approaches',
        description: 'Comparison between greedy, dynamic programming, and divide & conquer strategies.',
        links: ['https://leetcode.com/problems/'],
        stage: 5,
        category: 'other'
    }
];

export default function Dashboard() {
    const [topics, setTopics] = useState(initialTopics);

    // Modal Visibility States
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [viewTopic, setViewTopic] = useState(null);
    const [updateTopic, setUpdateTopic] = useState(null);
    const [deleteTopic, setDeleteTopic] = useState(null);

    const userName = "Student";

    // --- Handlers for Bulk Actions ---
    const handleReviseAllToday = () => {
        setTopics(currentTopics =>
            currentTopics.map(t => {
                if (t.category === 'today') {
                    return { ...t, stage: t.stage + 1, category: 'other' };
                }
                return t;
            })
        );
    };

    // --- Handlers for Single Card Actions ---
    const handleRevise = (topicId) => {
        setTopics(currentTopics =>
            currentTopics.map(t =>
                t.id === topicId
                    ? { ...t, stage: t.stage + 1, category: 'other' }
                    : t
            )
        );
    };

    // --- Modal Submission Handlers ---
    const handleCreateNew = (newTopicData) => {
        const newTopic = {
            id: `uuid-${Date.now()}`, 
            ...newTopicData,
            stage: 1,
            category: 'today' 
        };
        setTopics([newTopic, ...topics]);
        setIsCreateOpen(false);
    };

    const handleUpdate = (updatedTopicData) => {
        setTopics(currentTopics =>
            currentTopics.map(t =>
                t.id === updatedTopicData.id ? updatedTopicData : t
            )
        );
        setUpdateTopic(null);
    };

    const handleDelete = (topicId) => {
        setTopics(currentTopics => currentTopics.filter(t => t.id !== topicId));
        setDeleteTopic(null);
    };

    // --- Section Filtering ---
    const todayTopics = topics.filter(t => t.category === 'today');
    const tomorrowTopics = topics.filter(t => t.category === 'tomorrow');
    const otherTopics = topics.filter(t => t.category === 'other');

    return (
        <div className="pb-10 relative">

            {/* Dashboard Header */}
            <header className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-10">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
                        Welcome back, {userName}
                    </h1>
                    <p className="text-gray-600 dark:text-gray-400 mt-1">
                        You have {todayTopics.length} topics to revise today.
                    </p>
                </div>
                <div className="flex items-center gap-3 w-full sm:w-auto">
                    <button
                        onClick={handleReviseAllToday}
                        disabled={todayTopics.length === 0}
                        className="flex-1 sm:flex-none px-4 py-2.5 bg-green-600 hover:bg-green-700 disabled:bg-green-400 dark:disabled:bg-green-800 disabled:cursor-not-allowed text-white text-sm font-medium rounded-lg shadow-sm transition-colors flex items-center justify-center"
                    >
                        <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        Revise All Today
                    </button>
                    <button
                        onClick={() => setIsCreateOpen(true)}
                        className="flex-1 sm:flex-none px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg shadow-sm transition-colors flex items-center justify-center"
                    >
                        <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
                        </svg>
                        New
                    </button>
                </div>
            </header>

            {/* Today Section */}
            <section className="mb-10">
                <h2 className="text-xl font-bold text-gray-900 dark:text-white mb-4 flex items-center">
                    Today
                    <span className="ml-3 px-2 py-0.5 rounded-full bg-gray-100 dark:bg-gray-800 text-xs text-gray-600 dark:text-gray-400 font-medium">
                        {todayTopics.length}
                    </span>
                </h2>
                {todayTopics.length > 0 ? (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {todayTopics.map(topic => (
                            <TopicCard
                                key={topic.id}
                                topic={topic}
                                isActive={true}
                                onView={setViewTopic}
                                onUpdate={setUpdateTopic}
                                onDelete={setDeleteTopic}
                                onRevise={handleRevise}
                            />
                        ))}
                    </div>
                ) : (
                    <div className="p-8 text-center bg-gray-50 dark:bg-gray-800/50 rounded-xl border border-dashed border-gray-200 dark:border-gray-700">
                        <p className="text-gray-500 dark:text-gray-400">You're all caught up for today!</p>
                    </div>
                )}
            </section>

            {/* Tomorrow Section */}
            {tomorrowTopics.length > 0 && (
                <section className="mb-10">
                    <h2 className="text-xl font-bold text-gray-500 dark:text-gray-400 mb-4 flex items-center">
                        Tomorrow
                    </h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {tomorrowTopics.map(topic => (
                            <TopicCard
                                key={topic.id}
                                topic={topic}
                                isActive={false}
                                onView={setViewTopic}
                                onUpdate={setUpdateTopic}
                                onDelete={setDeleteTopic}
                                onRevise={handleRevise}
                            />
                        ))}
                    </div>
                </section>
            )}

            {/* Upcoming Section */}
            {otherTopics.length > 0 && (
                <section>
                    <h2 className="text-xl font-bold text-gray-500 dark:text-gray-400 mb-4 flex items-center">
                        Upcoming
                    </h2>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {otherTopics.map(topic => (
                            <TopicCard
                                key={topic.id}
                                topic={topic}
                                isActive={false}
                                onView={setViewTopic}
                                onUpdate={setUpdateTopic}
                                onDelete={setDeleteTopic}
                                onRevise={handleRevise}
                            />
                        ))}
                    </div>
                </section>
            )}

            {/* Modals Integration - Conditionally rendered to ensure they only download when triggered */}
            <Suspense fallback={<TopLoadingBar />}>
                
                {isCreateOpen && (
                    <CreateRevisionModal
                        isOpen={isCreateOpen}
                        onClose={() => setIsCreateOpen(false)}
                        onCreate={handleCreateNew}
                    />
                )}

                {updateTopic && (
                    <UpdateRevisionModal
                        isOpen={!!updateTopic}
                        onClose={() => setUpdateTopic(null)}
                        onUpdate={handleUpdate}
                        topicData={updateTopic}
                    />
                )}

                {viewTopic && (
                    <ViewRevisionModal
                        isOpen={!!viewTopic}
                        onClose={() => setViewTopic(null)}
                        topicData={viewTopic}
                    />
                )}

                {deleteTopic && (
                    <DeleteConfirmModal
                        isOpen={!!deleteTopic}
                        onClose={() => setDeleteTopic(null)}
                        onConfirm={handleDelete}
                        topicData={deleteTopic}
                    />
                )}

            </Suspense>
        </div>
    );
}