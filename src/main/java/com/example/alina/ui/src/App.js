import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import Layout from './components/Layout';
import LoginPage from './context/pages/LoginPage';
import RegisterPage from './context/pages/RegisterPage';
import DashboardPage from './context/pages/DashboardPage';
import FunctionsPage from './context/pages/FunctionsPage';
import FunctionEditorPage from './context/pages/FunctionEditorPage';
import GraphViewerPage from './context/pages/GraphViewerPage';
import OperationsPage from './context/pages/OperationsPage';
import SettingsPage from './context/pages/SettingsPage';
import DifferentiationPage from './context/pages/DifferentiationPage';

const PrivateRoute = ({ children }) => {
  const { isAuthenticated, loading } = useAuth();
  if (loading) return <div>Загрузка...</div>;
  return isAuthenticated ? children : <Navigate to="/login" replace />;
};

function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <Router>
          <Layout>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/register" element={<RegisterPage />} />
              <Route path="/" element={<PrivateRoute><DashboardPage /></PrivateRoute>} />
              <Route path="/functions" element={<PrivateRoute><FunctionsPage /></PrivateRoute>} />
              <Route path="/functions/new" element={<PrivateRoute><FunctionEditorPage /></PrivateRoute>} />
              <Route path="/functions/:id/edit" element={<PrivateRoute><FunctionEditorPage /></PrivateRoute>} />
              <Route path="/functions/:id/graph" element={<PrivateRoute><GraphViewerPage /></PrivateRoute>} />
              <Route path="/operations" element={<PrivateRoute><OperationsPage /></PrivateRoute>} />
              <Route path="/differentiation" element={<PrivateRoute><DifferentiationPage /></PrivateRoute>} />
              <Route path="/settings" element={<PrivateRoute><SettingsPage /></PrivateRoute>} />
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </Layout>
          <ToastContainer
            position="top-right"
            autoClose={3000}
            hideProgressBar={false}
            newestOnTop={false}
            closeOnClick
            rtl={false}
            pauseOnFocusLoss
            draggable
            pauseOnHover
          />
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
}

export default App;