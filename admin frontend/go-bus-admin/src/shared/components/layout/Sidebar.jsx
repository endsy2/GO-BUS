import React, { useState } from 'react';
import { NavItem } from './NavItem';
import { Icon } from 'shared/components/common/Icon';
import { Button } from 'shared/components/ui/button';
import { Avatar, AvatarFallback } from 'shared/components/ui/avatar';
import { LogOut, Menu, X } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'shared/components/ui/dialog';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { hasPermission } from 'shared/utils/permissions';

export const Sidebar = ({ activeTab, setActiveTab, user, onLogout }) => {
  const [showLogoutDialog, setShowLogoutDialog] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const { locale } = useLocale();
  
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;

  const allMenuItems = [
    { 
      id: 'dashboard', 
      icon: <Icon name="dashboard" />, 
      label: t('dashboard'),
      requiresPermission: null
    },
    { 
      id: 'bookings', 
      icon: <Icon name="calendar" />, 
      label: t('bookings'),
      requiresPermission: 'BOOKING_READ'
    },
    { 
      id: 'buses', 
      icon: <Icon name="bus" />, 
      label: t('buses'),
      requiresPermission: 'BUS_READ'
    },
    { 
      id: 'routes', 
      icon: <Icon name="mapPin" />, 
      label: t('routes'),
      requiresPermission: 'BUS_READ'
    },
    { 
      id: 'schedules', 
      icon: <Icon name="calendar" />, 
      label: t('schedules'),
      requiresPermission: 'BUS_READ'
    },
    { 
      id: 'layouts', 
      icon: <Icon name="grid" />, 
      label: t('layouts'),
      requiresPermission: 'BUS_READ'
    },
    { 
      id: 'customers', 
      icon: <Icon name="users" />, 
      label: t('customers'),
      requiresPermission: 'USER_READ'
    },
    { 
      id: 'wallets', 
      icon: <Icon name="dollarSign" />, 
      label: t('wallets'),
      requiresPermission: 'ADMIN_ACCESS'
    },
    { 
      id: 'refunds', 
      icon: <Icon name="dollarSign" />, 
      label: t('refunds') || 'Refunds',
      requiresPermission: 'ADMIN_ACCESS'
    },
    { 
      id: 'reports', 
      icon: <Icon name="barChart" />, 
      label: t('reports'),
      requiresPermission: 'ADMIN_ACCESS'
    },
  ];

  const allTeamItems = [
    { 
      id: 'team', 
      icon: <Icon name="userCheck" />, 
      label: t('team'),
      requiresPermission: 'ADMIN_ACCESS'
    },
  ];

  const menuItems = allMenuItems.filter(item => {
    if (!item.requiresPermission) return true;
    return hasPermission(user, item.requiresPermission);
  });

  const teamItems = allTeamItems.filter(item => {
    if (!item.requiresPermission) return true;
    return hasPermission(user, item.requiresPermission);
  });

  const handleLogoutClick = () => {
    setShowLogoutDialog(true);
  };

  const confirmLogout = () => {
    setShowLogoutDialog(false);
    onLogout();
  };

  const cancelLogout = () => {
    setShowLogoutDialog(false);
  };

  const handleNavItemClick = (id) => {
    setActiveTab(id);
    setIsMobileMenuOpen(false); // Close mobile menu after navigation
  };

  const SidebarContent = () => (
    <>
      {/* Header */}
      <div className="px-6 py-6 border-b border-gray-200 dark:border-gray-800 flex-shrink-0">
        <div className="mb-3 flex items-center justify-center">
          <img 
            src="/Blue Minimal Idea Free Education Logo.png" 
            alt="GoBus Logo" 
            className="max-w-[160px] h-auto max-h-16 object-contain rounded-xl" 
          />
        </div>
        <p className="text-sm text-center text-gray-600 dark:text-gray-400 font-medium">Go Bus Admin</p>
      </div>

      {/* Navigation */}
      <nav className="py-4 flex-1 overflow-y-auto overflow-x-hidden custom-scrollbar">
        <div className="mb-5">
          <div className="px-5 py-2 text-xs font-bold uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-2">
            MAIN
          </div>
          <div className="space-y-1">
            {menuItems.map(item => (
              <NavItem
                key={item.id}
                icon={item.icon}
                label={item.label}
                active={activeTab === item.id}
                onClick={() => handleNavItemClick(item.id)}
              />
            ))}
          </div>
        </div>

        {teamItems.length > 0 && (
          <div className="mb-5">
            <div className="px-5 py-2 text-xs font-bold uppercase tracking-wider text-gray-400 dark:text-gray-500 mb-2">
              TEAM
            </div>
            <div className="space-y-1">
              {teamItems.map(item => (
                <NavItem
                  key={item.id}
                  icon={item.icon}
                  label={item.label}
                  active={activeTab === item.id}
                  onClick={() => handleNavItemClick(item.id)}
                />
              ))}
            </div>
          </div>
        )}
      </nav>

      {/* User Profile Footer */}
      <div className="p-4 border-t border-border flex-shrink-0">
        <button
          onClick={() => handleNavItemClick('profile')}
          className="w-full flex items-center gap-3 p-3 bg-muted rounded-lg mb-3 hover:bg-muted/80 transition-colors cursor-pointer"
        >
          <Avatar className="h-9 w-9 flex-shrink-0">
            <AvatarFallback className="bg-primary/10 text-primary font-medium text-sm">
              {user?.fullName ? user.fullName.charAt(0).toUpperCase() : 'U'}
            </AvatarFallback>
          </Avatar>
          <div className="flex-1 overflow-hidden text-left">
            <div className="text-sm font-medium text-foreground truncate">
              {user?.userName || 'User'}
            </div>
            <div className="text-xs text-muted-foreground truncate">
              {user?.email || 'email'}
            </div>
          </div>
        </button>
        <Button
          variant="outline"
          className="w-full text-destructive hover:text-destructive hover:bg-destructive/10 border-border"
          onClick={handleLogoutClick}
        >
          <LogOut className="mr-2 h-4 w-4" />
          {t('logout')}
        </Button>
      </div>

      {/* Custom Scrollbar Styles */}
      <style>{`
        .custom-scrollbar::-webkit-scrollbar {
          width: 6px;
        }
        .custom-scrollbar::-webkit-scrollbar-track {
          background: transparent;
          border-radius: 10px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb {
          background: rgba(156, 163, 175, 0.3);
          border-radius: 10px;
        }
        .custom-scrollbar::-webkit-scrollbar-thumb:hover {
          background: rgba(156, 163, 175, 0.5);
        }
        [data-theme="dark"] .custom-scrollbar::-webkit-scrollbar-thumb {
          background: rgba(75, 85, 99, 0.5);
        }
        [data-theme="dark"] .custom-scrollbar::-webkit-scrollbar-thumb:hover {
          background: rgba(75, 85, 99, 0.7);
        }
      `}</style>
    </>
  );

  return (
    <>
      {/* Mobile Menu Button */}
      <button
        onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
        className="lg:hidden fixed top-4 left-4 z-[150] p-2 bg-white dark:bg-gray-800 rounded-lg shadow-lg border border-gray-200 dark:border-gray-700"
        aria-label="Toggle menu"
      >
        {isMobileMenuOpen ? (
          <X className="h-6 w-6 text-gray-700 dark:text-gray-300" />
        ) : (
          <Menu className="h-6 w-6 text-gray-700 dark:text-gray-300" />
        )}
      </button>

      {/* Mobile Overlay */}
      {isMobileMenuOpen && (
        <div
          className="lg:hidden fixed inset-0 bg-black/50 z-[110] backdrop-blur-sm"
          onClick={() => setIsMobileMenuOpen(false)}
        />
      )}

      {/* Desktop Sidebar */}
      <div className="hidden lg:flex w-[260px] h-screen bg-white dark:bg-[#0A0E1A] text-gray-900 dark:text-white flex-col shadow-xl border-r border-gray-200 dark:border-gray-800 fixed left-0 top-0 z-[100] transition-colors duration-200">
        <SidebarContent />
      </div>

      {/* Mobile Sidebar */}
      <div
        className={`lg:hidden fixed left-0 top-0 h-screen w-[280px] bg-white dark:bg-[#0A0E1A] text-gray-900 dark:text-white flex flex-col shadow-2xl border-r border-gray-200 dark:border-gray-800 z-[120] transition-transform duration-300 ease-in-out ${
          isMobileMenuOpen ? 'translate-x-0' : '-translate-x-full'
        }`}
      >
        <SidebarContent />
      </div>

      <Dialog open={showLogoutDialog} onOpenChange={cancelLogout}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>{t('confirmLogout')}</DialogTitle>
            <DialogDescription>
              {t('confirmLogoutMessage') || 'Are you sure you want to logout?'}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={cancelLogout}>
              {t('cancel')}
            </Button>
            <Button variant="destructive" onClick={confirmLogout}>
              {t('logout') || 'Logout'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
};

export default Sidebar;
