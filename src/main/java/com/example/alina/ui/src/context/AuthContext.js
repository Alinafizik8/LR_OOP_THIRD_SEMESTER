import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { login, logout, getCurrentUser } from '../api/auth';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const stored = getCurrentUser();
    setUser(stored);
    setLoading(false);
  }, []);

  const signIn = useCallback(async (username, password) => {
    const userData = await login(username, password);
    setUser(userData);
    return userData;
  }, []);

  const signOut = useCallback(() => {
    logout();
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{
      user,
      loading,
      signIn,
      signOut,
      isAuthenticated: !!user,
      isAdmin: user?.role === 'ADMIN'
    }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);