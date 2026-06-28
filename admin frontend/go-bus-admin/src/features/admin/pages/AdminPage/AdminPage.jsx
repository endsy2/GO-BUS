import React, { useState } from 'react';
import { Tabs, TabsList, TabsTrigger, TabsContent } from 'shared/components/ui/tabs';
import { Card } from 'shared/components/ui/card';
import UserManagementTab from '../../components/UserManagementTab/UserManagementTab';
import RoleManagementTab from '../../components/RoleManagementTab/RoleManagementTab';
import PermissionManagementTab from '../../components/PermissionManagementTab/PermissionManagementTab';
import BookingManagementTab from '../../components/BookingManagementTab/BookingManagementTab';

const AdminPage = () => {
  const [activeTab, setActiveTab] = useState('users');

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-6">Admin Panel</h1>

      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="mb-6">
          <TabsTrigger value="users">Users</TabsTrigger>
          <TabsTrigger value="roles">Roles</TabsTrigger>
          <TabsTrigger value="permissions">Permissions</TabsTrigger>
          <TabsTrigger value="bookings">Bookings</TabsTrigger>
        </TabsList>

        <TabsContent value="users">
          <UserManagementTab />
        </TabsContent>

        <TabsContent value="roles">
          <RoleManagementTab />
        </TabsContent>

        <TabsContent value="permissions">
          <PermissionManagementTab />
        </TabsContent>

        <TabsContent value="bookings">
          <BookingManagementTab />
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default AdminPage;
