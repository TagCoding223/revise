import React, { createContext, useContext, useState, useCallback } from 'react';
import Alert from '../components/shared/Alert';

const AlertContext = createContext();

export const useAlert = () => useContext(AlertContext);

export const AlertProvider = ({ children }) => {
  const [alert, setAlert] = useState(null);

  // default duration is 30,000ms (30 seconds)
  const showAlert = useCallback((message, type = 'info', duration = 30000) => {
    setAlert({ id: Date.now(), message, type, duration });
  }, []);

  const closeAlert = useCallback(() => {
    setAlert(null);
  }, []);

  return (
    <AlertContext.Provider value={{ showAlert }}>
      {children}
      {alert && (
        <Alert
          key={alert.id} // Forces React to remount if a new alert arrives
          message={alert.message}
          type={alert.type}
          duration={alert.duration}
          onClose={closeAlert}
        />
      )}
    </AlertContext.Provider>
  );
};