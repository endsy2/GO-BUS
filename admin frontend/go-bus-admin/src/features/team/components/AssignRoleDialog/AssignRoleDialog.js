import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'shared/components/ui/dialog';
import { Button } from 'shared/components/ui/button';
import { Avatar, AvatarFallback } from 'shared/components/ui/avatar';
import { Checkbox } from 'shared/components/ui/checkbox';
import { Label } from 'shared/components/ui/label';
import { Shield, Loader2 } from 'lucide-react';
import adminService from 'features/admin/services/adminService';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { cn } from 'lib/utils';

const AssignRoleDialog = ({ isOpen, user, onSave, onCancel }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const [availableRoles, setAvailableRoles] = useState([]);
  const [selectedRoles, setSelectedRoles] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    if (isOpen) {
      fetchRoles();
      if (user?.roles) {
        setSelectedRoles(user.roles.map(role => role.name || role));
      }
    }
  }, [isOpen, user]);

  const fetchRoles = async () => {
    try {
      setLoading(true);
      const result = await adminService.roles.getAll();
      const rolesData = result.data || result;
      setAvailableRoles(Array.isArray(rolesData) ? rolesData : []);
      setError('');
    } catch (err) {
      setError('Failed to load roles');
    } finally {
      setLoading(false);
    }
  };

  const handleRoleToggle = (roleName) => {
    setSelectedRoles(prev => {
      if (prev.includes(roleName)) {
        return prev.filter(r => r !== roleName);
      } else {
        return [...prev, roleName];
      }
    });
  };

  const handleSave = () => {
    onSave(selectedRoles);
  };

  return (
    <Dialog open={isOpen} onOpenChange={onCancel}>
      <DialogContent className="max-w-[500px]">
        <DialogHeader>
          <DialogTitle>{t('assignRoles')}</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          {user && (
            <div className="flex items-center gap-3 p-4 bg-muted rounded-lg">
              <Avatar className="h-12 w-12">
                <AvatarFallback className="bg-gradient-to-br from-primary to-purple-600 text-white text-lg font-semibold">
                  {user.fullName ? user.fullName.charAt(0).toUpperCase() : '?'}
                </AvatarFallback>
              </Avatar>
              <div>
                <div className="font-semibold">{user.fullName}</div>
                <div className="text-sm text-muted-foreground">{user.email}</div>
              </div>
            </div>
          )}

          {error && (
            <div className="p-3 bg-destructive/10 border border-destructive/20 rounded-md text-sm text-destructive">
              {error}
            </div>
          )}

          {loading ? (
            <div className="flex items-center justify-center gap-2 py-10 text-muted-foreground">
              <Loader2 className="h-5 w-5 animate-spin" />
              <span>{t('loadingRolesDialog')}</span>
            </div>
          ) : (
            <div className="space-y-3">
              <p className="text-sm font-medium">{t('selectRolesForUser')}</p>
              <div className="space-y-2 max-h-[400px] overflow-y-auto">
                {availableRoles.map(role => (
                  <Label
                    key={role.id}
                    className={cn(
                      "flex items-start gap-3 p-3 border rounded-lg cursor-pointer transition-all hover:bg-accent hover:border-primary",
                      selectedRoles.includes(role.name) && "border-primary bg-primary/5"
                    )}
                  >
                    <Checkbox
                      checked={selectedRoles.includes(role.name)}
                      onCheckedChange={() => handleRoleToggle(role.name)}
                      className="mt-0.5"
                    />
                    <div className="flex-1 space-y-1">
                      <div className="flex items-center gap-2 font-semibold text-sm">
                        <Shield className="h-4 w-4" />
                        {role.name.replace('ROLE_', '')}
                      </div>
                      {role.description && (
                        <div className="text-xs text-muted-foreground leading-snug">
                          {role.description}
                        </div>
                      )}
                    </div>
                  </Label>
                ))}
              </div>
            </div>
          )}
        </div>

        <DialogFooter>
          <Button variant="outline" onClick={onCancel}>
            {t('cancel')}
          </Button>
          <Button onClick={handleSave} disabled={loading}>
            {t('saveRoles')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default AssignRoleDialog;
