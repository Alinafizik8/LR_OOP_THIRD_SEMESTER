import React, { createContext, useContext, useState, useEffect } from 'react';

const FactoryContext = createContext();

export const useFactory = () => {
  const context = useContext(FactoryContext);
  if (!context) {
    throw new Error('useFactory must be used within FactoryProvider');
  }
  return context;
};

export const FactoryProvider = ({ children }) => {
  const [factoryType, setFactoryType] = useState(() => {
    return localStorage.getItem('factoryType') || 'ARRAY';
  });

  useEffect(() => {
    localStorage.setItem('factoryType', factoryType);
  }, [factoryType]);

  return (
    <FactoryContext.Provider value={{ factoryType, setFactoryType }}>
      {children}
    </FactoryContext.Provider>
  );
};
