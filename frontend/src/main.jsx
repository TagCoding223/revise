import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom';
import './index.css'
import App from './App.jsx'
import { ThemeProvider } from './context/ThemeContext.jsx'
import { AuthProvider } from './context/AuthContext.jsx'
import { HelmetProvider } from 'react-helmet-async';
import { GoogleOAuthProvider } from '@react-oauth/google';

// Use the exact same Client ID we put in your Spring Boot application.properties
const GOOGLE_CLIENT_ID = import.meta.env.VITE_GOOGLE_CLIENT_KEY

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <GoogleOAuthProvider clientId='{GOOGLE_CLIENT_ID}'>
      <HelmetProvider>
        <BrowserRouter>
          <ThemeProvider>
            <AuthProvider>
              <App />
            </AuthProvider>
          </ThemeProvider>
        </BrowserRouter>
      </HelmetProvider>
    </GoogleOAuthProvider>
  </StrictMode>,
)
