import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // Run once when the app loads to check if a valid session exists
  useEffect(() => {
    const checkSession = async () => {
      try {
        // MOCK API CALL: Replace this with your actual Axios/Fetch call to your Spring Boot /me endpoint
        // const response = await axios.get('/api/v1/auth/me');
        // setUser(response.data);
        
        // Simulating a network delay for now
        await new Promise(resolve => setTimeout(resolve, 500)); 
        setUser(null); // Assuming no user is logged in on initial bare load
      } catch (error) {
        // If 401 Unauthorized, ensure user state is clear
        setUser(null);
      } finally {
        setLoading(false);
      }
    };

    checkSession();
  }, []);

  // Call this function when the user submits the login form successfully
  const login = (userData) => {
    setUser(userData);
  };

  // Call this function when the user clicks Logout in the Navbar
  const logout = async () => {
    try {
      // Optional: Hit your backend to invalidate the token/cookie
      // await axios.post('/api/v1/auth/logout');
    } finally {
      setUser(null);
    }
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {/* If still checking session, you can render a spinner here, or just render the children and handle loading in the routes */}
      {!loading && children} 
    </AuthContext.Provider>
  );
};