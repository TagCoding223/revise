import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from 'axios';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Run once when the app loads to check if a valid session exists
  useEffect(() => {
    const initializeSession = () => {
      const storedToken = localStorage.getItem('jwt_token');
      const storedUserId = localStorage.getItem('user_id');
      const storedNewUser = localStorage.getItem('is_new_user') === 'true';

      if (storedToken && storedUserId) {
        // Attach the token to Axios defaults so all future requests are authenticated
        axios.defaults.headers.common['Authorization'] = `Bearer ${storedToken}`;
        
        // Restore user state
        setUser({ 
          token: storedToken, 
          userId: storedUserId, 
          newUser: storedNewUser 
        });
      }
      
      setLoading(false);
    };

    initializeSession();
  }, []);

  // Called from Auth.jsx when login/google-auth is successful
  const login = (userData) => {
    // 1. Save to localStorage so the session survives browser refreshes
    localStorage.setItem('jwt_token', userData.token);
    localStorage.setItem('user_id', userData.userId);
    localStorage.setItem('is_new_user', userData.newUser === true);
    
    // 2. Attach the token to Axios globally
    axios.defaults.headers.common['Authorization'] = `Bearer ${userData.token}`;
    
    // 3. Update React state
    setUser(userData);
  };

  // Called from the Navbar when the user clicks Logout
  const logout = () => {
    // 1. Wipe localStorage
    localStorage.removeItem('jwt_token');
    localStorage.removeItem('user_id');
    localStorage.removeItem('is_new_user');
    
    // 2. Remove the Axios authorization header
    delete axios.defaults.headers.common['Authorization'];
    
    // 3. Clear React state
    setUser(null);
  };

  const markPasswordSet = () => {
    localStorage.setItem('is_new_user', 'false');
    setUser((prev) => ({ ...prev, newUser: false }));
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, markPasswordSet, loading }}>
      {/* If still checking session, you can render a spinner here, or just render the children and handle loading in the routes */}
      {!loading && children} 
    </AuthContext.Provider>
  );
};