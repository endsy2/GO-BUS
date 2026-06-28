import React from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from 'shared/components/ui/dialog';
import { Button } from 'shared/components/common/Button';
import { Lock } from 'lucide-react';

export const UnauthorizedDialog = ({ isOpen, onOk }) => {
  return (
    <Dialog open={isOpen} onOpenChange={onOk}>
      <DialogContent className="sm:max-w-[450px]">
        <DialogHeader className="items-center space-y-4">
          <div className="flex items-center justify-center w-16 h-16 rounded-full bg-destructive/10">
            <Lock className="w-8 h-8 text-destructive" />
          </div>
          <DialogTitle className="text-2xl text-center">Unauthorized Access</DialogTitle>
          <DialogDescription className="text-center space-y-2">
            <p>Your session has expired or you don't have permission to access this resource.</p>
            <p className="text-sm text-muted-foreground">Please login again to continue.</p>
          </DialogDescription>
        </DialogHeader>
        <DialogFooter className="sm:justify-center">
          <Button variant="danger" onClick={onOk} className="px-10">
            OK
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
};

export default UnauthorizedDialog;
