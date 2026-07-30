import axios from 'axios';

const BACKEND_BASE_URL = import.meta.env.VITE_BACKEND_BASE_URL || '';

// Create a custom axios instance
const api = axios.create({
    baseURL: BACKEND_BASE_URL,
    headers: {
        'Content-Type': 'application/json'
    }
});

// Request Interceptor: Automatically attach the Access Token to every request
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers['Authorization'] = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response Interceptor: Catch 401s and silently refresh the token
api.interceptors.response.use(
    (response) => {
        // Explicitly return the response so the component 'try' block can continue
        return response; 
    },
    async (error) => {
        const originalRequest = error.config;

        // If the error is 401 (Unauthorized) and we haven't already retried this request
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true; // Mark as retried to prevent infinite loops

            try {
                const refreshToken = localStorage.getItem('refreshToken');
                if (!refreshToken) throw new Error("No refresh token available");

                // Ask the backend for a new Access Token using the Refresh Token
                const response = await axios.post(`${BACKEND_BASE_URL}/api/v1/auth/refresh`, {
                    refreshToken: refreshToken
                });

                // Extract the new tokens
                const { token, refreshToken: newRefreshToken } = response.data;

                // Save them to local storage
                localStorage.setItem('token', token);
                localStorage.setItem('refreshToken', newRefreshToken);

                // Update the failed request with the brand new Access Token and try again
                originalRequest.headers['Authorization'] = `Bearer ${token}`;
                return api(originalRequest);
                
            } catch (refreshError) {
                // If the refresh token is also expired or invalid, we must force a logout
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');
                localStorage.removeItem('user');
                
                // Redirect the user to the login page safely
                window.location.href = '/login'; 
                return Promise.reject(refreshError);
            }
        }
        return Promise.reject(error);
    }
);

export default api;