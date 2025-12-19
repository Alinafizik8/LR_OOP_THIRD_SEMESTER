import React from 'react';
import './Alert.css';

const Alert = ({ message, type = 'error', onClose }) => {
  if (!message) return null;

  return (
    <div className={`alert alert-${type}`} onClick={onClose}>
      <div className="alert-content">
        <span>{message}</span>
        <button className="alert-close" onClick={onClose}>×</button>
      </div>
    </div>
  );
};

export default Alert;
