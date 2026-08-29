import React, { useState, useEffect, Suspense, lazy } from 'react';
import api from '../api/axiosConfig';
import { TopicCard } from '../components/shared/TopicCard';
import TopLoadingBar from '../components/shared/TopLoadingBar';
import { useAlert } from '../context/AlertContext';
import { useAuth } from '../context/AuthContext';
import { Helmet } from 'react-helmet-async';

const CreateRevisionModal = lazy(() => import('../components/modals/CreateRevisionModal'));
const UpdateRevisionModal = lazy(() => import('../components/modals/UpdateRevisionModal'));
const ViewRevisionModal = lazy(() => import('../components/modals/ViewRevisionModal'));
const DeleteConfirmModal = lazy(() => import('../components/modals/DeleteConfirmModal'));
const ReviseAllConfirmModal = lazy(() => import('../components/modals/ReviseAllConfirmModal'));

const BACKEND_BASE_URL = import.meta.env.VITE_BACKEND_BASE_URL || '';

export default function Dashboard() {
    const [topics, setTopics] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [isProcessingBulk, setIsProcessingBulk] = useState(false);

    // Modal Visibility States
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [isReviseAllOpen, setIsReviseAllOpen] = useState(false);
    const [viewTopic, setViewTopic] = useState(null);
    const [updateTopic, setUpdateTopic] = useState(null);
    const [deleteTopic, setDeleteTopic] = useState(null);

    const { showAlert } = useAlert();
    const { user } = useAuth(); // If you added full name to context, you can use it here

    // --- Fetch Topics on Mount ---
    const fetchTopics = async () => {
        try {
            const response = await api.get(`${BACKEND_BASE_URL}/api/v1/topics`);
            setTopics(response.data);
        } catch (error) {
            console.error("Failed to load topics:", error);
            showAlert("Failed to load your topics. Please try refreshing the page.", "error");
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        fetchTopics();
    }, []);

    // --- API Mutation Handlers ---

    const handleReviseAllToday = async () => {
        setIsProcessingBulk(true);
        setIsReviseAllOpen(false);
        try {
            const response = await api.patch(`${BACKEND_BASE_URL}/api/v1/topics/revise-today`);
            showAlert(response.data.message || "All today's topics marked as revised!", "success");
            await fetchTopics(); // Refetch to get updated dates and categories
        } catch (error) {
            showAlert(error.response?.data?.message || "Failed to bulk revise topics.", "error");
        } finally {
            setIsProcessingBulk(false);
        }
    };

    const handleRevise = async (topicId) => {
        try {
            await api.patch(`${BACKEND_BASE_URL}/api/v1/topics/${topicId}/revise`);
            showAlert("Topic marked as revised! Excellent work.", "success");
            await fetchTopics();
        } catch (error) {
            showAlert(error.response?.data?.message || "Failed to mark topic as revised.", "error");
        }
    };

    const handleCreateNew = async (newTopicData) => {
        try {
            await api.post(`${BACKEND_BASE_URL}/api/v1/topics`, newTopicData);
            showAlert("New topic created successfully.", "success");
            setIsCreateOpen(false);
            await fetchTopics();
        } catch (error) {
            showAlert(error.response?.data?.message || "Failed to create topic.", "error");
        }
    };

    const handleUpdate = async (updatedTopicData) => {
        try {
            await api.put(`${BACKEND_BASE_URL}/api/v1/topics/${updatedTopicData.id}`, updatedTopicData);
            showAlert("Topic updated successfully.", "success");
            setUpdateTopic(null);
            await fetchTopics();
        } catch (error) {
            showAlert(error.response?.data?.message || "Failed to update topic.", "error");
        }
    };

    const handleDelete = async (topicId) => {
        try {
            await api.delete(`${BACKEND_BASE_URL}/api/v1/topics/${topicId}`);
            showAlert("Topic deleted successfully.", "success");
            setDeleteTopic(null);
            await fetchTopics();
        } catch (error) {
            showAlert(error.response?.data?.message || "Failed to delete topic.", "error");
        }
    };

    // --- Section Filtering ---
    const todayTopics = topics.filter(t => t.category === 'today');
    const tomorrowTopics = topics.filter(t => t.category === 'tomorrow');
    const otherTopics = topics.filter(t => t.category === 'other');

    if (isLoading) {
        return <TopLoadingBar />;
    }

    return (
        <div className="pb-10 relative">
            <Helmet>
                <title>Dashboard | Revise</title>
                <meta name="description" content="Manage your study topics, track your daily revision tasks, and apply active recall to master your subjects." />
            </Helmet>
            {/* Dashboard Header */}
            <header className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-10">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900 dark:text-white">
                        Welcome back
                    </h1>
                    <p className="text-gray-600 dark:text-gray-400 mt-1">
                        You have {todayTopics.length} topics to revise today.
                    </p>
                </div>
                <div className="flex items-center gap-3 w-full sm:w-auto">
                    <button
                        onClick={() => setIsReviseAllOpen(true)}
                        disabled={todayTopics.length === 0 || isProcessingBulk}
                        className="flex-1 sm:flex-none px-4 py-2.5 bg-green-600 hover:bg-green-700 disabled:bg-green-400 dark:disabled:bg-green-800 disabled:cursor-not-allowed text-white text-sm font-medium rounded-lg shadow-sm transition-colors flex items-center justify-center cursor-pointer"
                    >
                        {isProcessingBulk ? (
                            <svg className="animate-spin h-4 w-4 mr-2 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                        ) : (
                            <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                            </svg>
                        )}
                        Revise All Today
                    </button>
                    <button
                        onClick={() => setIsCreateOpen(true)}
                        className="flex-1 sm:flex-none px-4 py-2.5 bg-blue-600 hover:bg-blue-700 text-white text-sm font-medium rounded-lg shadow-sm transition-colors flex items-center justify-center cursor-pointer"
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

            {/* Modals Integration */}
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

                {isReviseAllOpen && (
                    <ReviseAllConfirmModal
                        isOpen={isReviseAllOpen}
                        onClose={() => setIsReviseAllOpen(false)}
                        onConfirm={handleReviseAllToday}
                        topicCount={todayTopics.length}
                    />
                )}

            </Suspense>
        </div>
    );
}