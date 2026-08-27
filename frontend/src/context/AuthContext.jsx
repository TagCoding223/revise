import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    setUser(null);
  };

  useEffect(() => {
    const initializeSession = () => {
      const storedToken = localStorage.getItem('token');
      const storedUserString = localStorage.getItem('user');

      if (storedToken && storedUserString) {
        // Parse the stringified user object back into JSON
        const storedUser = JSON.parse(storedUserString);

        axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`;
        setUser(storedUser);
      }
      setLoading(false);
    };

    initializeSession();

    // Global Axios Interceptor to catch expired tokens
    const interceptor = axios.interceptors.response.use(
      (response) => response, 
      (error) => {
        if (error.response && error.response.status === 401) {
          const requestUrl = error.config.url;
          // Ignore public auth routes to prevent loops
          if (!requestUrl.includes('/api/v1/auth/')) {
             console.warn("Session expired. Logging out.");
             logout(); 
             window.location.href = '/login'; 
          }
        }
        return Promise.reject(error);
      }
    );

    return () => {
      axios.interceptors.response.eject(interceptor);
    };
  }, []);

  const login = (userData) => {
    localStorage.setItem('token', userData.token);
    localStorage.setItem('refreshToken', userData.refreshToken); // NEW
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {!loading && children} 
    </AuthContext.Provider>
  );
};