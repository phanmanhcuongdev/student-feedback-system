import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './i18n'
import './index.css'
import App from './App.tsx'
import { BrowserRouter } from "react-router-dom";
import { AuthProvider } from "./features/auth/AuthContext";
import { NotificationProvider } from "./features/notifications/NotificationProvider";

createRoot(document.getElementById('root')!).render(
  <StrictMode>
      <AuthProvider>
          <BrowserRouter>
              <NotificationProvider>
                  <App />
              </NotificationProvider>
          </BrowserRouter>
      </AuthProvider>
  </StrictMode>,
)
