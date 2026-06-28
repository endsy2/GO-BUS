import React, { useState, useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom';
import './App.css';
import { Sidebar } from './shared/components/layout/Sidebar';
import { TopBar } from './shared/components/layout/TopBar';
import { UnauthorizedDialog } from './shared/components/feedback/UnauthorizedDialog';
import { ToastProvider } from './shared/components/ui/toast';

// Feature imports
import { LoginPage } from './features/auth';
import { DashboardPage } from './features/dashboard';
import { BusesPage } from './features/buses';
import { RoutesPage } from './features/routes';
import { CustomersPage } from './features/customers';
import { BookingsPage } from './features/bookings';
import { ReportsPage } from './features/reports';
import { TeamPage, userService } from './features/team';
import { SchedulesPage } from './features/schedules';
import { LayoutsPage } from './features/layouts';
import { PromosPage } from './features/promos';
import { WalletsPage } from './features/wallets';
import { NotificationsPage } from './features/notifications';
import { ProfilePage } from './features/profile';
import { RefundsPage } from './features/refunds';

// Services
import { setUnauthorizedHandler } from './services';

// Protected Route wrapper
const ProtectedRoute = ({ children, isAuthenticated }) => {
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return children;
};

// Main Layout component
const MainLayout = ({ user, onLogout }) => {
  const navigate = useNavigate();
  const location = useLocation();
  
  // Determine active tab from current path
  const getActiveTab = () => {
    const path = location.pathname.slice(1); // Remove leading slash
    return path || 'dashboard';
  };

  const handleTabChange = (tab) => {
    navigate(`/${tab}`);
  };

  return (
    <div className="App">
      <Sidebar 
        activeTab={getActiveTab()} 
        setActiveTab={handleTabChange}
        user={user}
        onLogout={onLogout}
      />
      <div className="main-content">
        <TopBar />
        <div className="page-content">
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/bookings" element={<BookingsPage />} />
            <Route path="/buses" element={<BusesPage />} />
            <Route path="/routes" element={<RoutesPage />} />
            <Route path="/schedules" element={<SchedulesPage />} />
            <Route path="/layouts" element={<LayoutsPage />} />
            <Route path="/customers" element={<CustomersPage />} />
            <Route path="/promos" element={<PromosPage />} />
            <Route path="/wallets" element={<WalletsPage />} />
            <Route path="/refunds" element={<RefundsPage />} />
            <Route path="/notifications" element={<NotificationsPage />} />
            <Route path="/reports" element={<ReportsPage />} />
            <Route path="/team" element={<TeamPage />} />
            <Route path="/profile" element={<ProfilePage />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </div>
      </div>
    </div>
  );
};

function App() {
  const [user, setUser] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [showUnauthorizedDialog, setShowUnauthorizedDialog] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    setUnauthorizedHandler(() => {
      setShowUnauthorizedDialog(true);
    });

    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      const userData = JSON.parse(storedUser);
      setUser(userData);
      setIsAuthenticated(true);
      fetchProfile(userData);
    } else {
      setIsLoading(false);
    }
  }, []);

  const fetchProfile = async (userData) => {
    try {
      const result = await userService.getProfile();
      const profile = result.data || result;
      const updatedUser = { ...userData, ...profile };
      setUser(updatedUser);
      localStorage.setItem('user', JSON.stringify(updatedUser));
    } catch (error) {
      // If the interceptor already cleared localStorage (both tokens expired),
      // force logout straight away instead of leaving the user on a broken session.
      if (!localStorage.getItem('user')) {
        handleLogout();
        return;
      }
      console.error('[App] Failed to fetch profile on app load:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleUnauthorizedOk = () => {
    setShowUnauthorizedDialog(false);
    handleLogout();
  };

  const handleLoginSuccess = (userData) => {
    setUser(userData);
    setIsAuthenticated(true);
    setIsLoading(false);
  };

  const handleLogout = () => {
    localStorage.removeItem('user');
    localStorage.setItem('locale', 'en');
    setUser(null);
    setIsAuthenticated(false);
    window.location.href = '/login';
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-screen bg-slate-50 dark:bg-slate-950">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500 mx-auto mb-4"></div>
          <p className="text-slate-600 dark:text-slate-400">Loading...</p>
        </div>
      </div>
    );
  }

  return (
    <Router>
      <ToastProvider>
        <Routes>
          <Route 
            path="/login" 
            element={
              isAuthenticated ? (
                <Navigate to="/dashboard" replace />
              ) : (
                <LoginPage onLoginSuccess={handleLoginSuccess} />
              )
            } 
          />
          <Route
            path="/*"
            element={
              <ProtectedRoute isAuthenticated={isAuthenticated}>
                <MainLayout user={user} onLogout={handleLogout} />
              </ProtectedRoute>
            }
          />
        </Routes>
        <UnauthorizedDialog 
          isOpen={showUnauthorizedDialog}
          onOk={handleUnauthorizedOk}
        />
      </ToastProvider>
    </Router>
  );
}

export default App;
