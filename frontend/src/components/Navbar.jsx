import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  const handleLinkClick = () => {
    setMenuOpen(false);
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/dashboard" className="navbar-brand" onClick={handleLinkClick}>
          Табулированные функции
        </Link>

        <button
          className="navbar-toggle"
          onClick={() => setMenuOpen(!menuOpen)}
          aria-label="Toggle menu"
        >
          <span></span>
          <span></span>
          <span></span>
        </button>

        <div className={`navbar-links ${menuOpen ? 'open' : ''}`}>
          <Link
            to="/dashboard"
            className={`navbar-link ${isActive('/dashboard') ? 'active' : ''}`}
            onClick={handleLinkClick}
          >
            Мои функции
          </Link>
          <Link
            to="/operations"
            className={`navbar-link ${isActive('/operations') ? 'active' : ''}`}
            onClick={handleLinkClick}
          >
            Операции
          </Link>
          <Link
            to="/differentiation"
            className={`navbar-link ${isActive('/differentiation') ? 'active' : ''}`}
            onClick={handleLinkClick}
          >
            Дифференцирование
          </Link>
          <Link
            to="/explorer"
            className={`navbar-link ${isActive('/explorer') ? 'active' : ''}`}
            onClick={handleLinkClick}
          >
            Изучение функции
          </Link>
          <Link
            to="/settings"
            className={`navbar-link ${isActive('/settings') ? 'active' : ''}`}
            onClick={handleLinkClick}
          >
            Настройки
          </Link>
        </div>

        <div className="navbar-user">
          <span className="user-name">{user?.username}</span>
          <button onClick={handleLogout} className="logout-btn">
            Выйти
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
