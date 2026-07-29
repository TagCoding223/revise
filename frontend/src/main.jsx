import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom';
import './index.css'
import App from './App.jsx'
import { ThemeProvider } from './context/ThemeContext.jsx'
import { AuthProvider } from './context/AuthContext.jsx'
import { HelmetProvider } from 'react-helmet-async';
import { GoogleOAuthProvider } from '@react-oauth/google';
import { AlertProvider } from './context/AlertContext.jsx';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <HelmetProvider>
      <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_KEY}>
        <BrowserRouter>
          <ThemeProvider>
            <AlertProvider>
              <AuthProvider>
                <App />
              </AuthProvider>
            </AlertProvider>
          </ThemeProvider>
        </BrowserRouter>
      </GoogleOAuthProvider>
    </HelmetProvider>
  </StrictMode>,
)
