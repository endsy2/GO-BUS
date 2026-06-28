import React, { useState, useEffect } from 'react';
import { formatStatus } from 'shared/utils/formatters';
import { Card } from 'shared/components/ui/card';
import { Button } from 'shared/components/ui/button';
import { Badge } from 'shared/components/ui/badge';
import notificationService from '../../services/notificationService';

const NotificationsPage = () => {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const user = JSON.parse(localStorage.getItem('user') || '{}');

  useEffect(() => {
    if (user.id) {
      fetchNotifications();
    }
  }, []);

  const fetchNotifications = async () => {
    try {
      setLoading(true);
      const result = await notificationService.getByUserId(user.id);
      const notifData = result.data || result;
      setNotifications(Array.isArray(notifData) ? notifData : []);
    } catch (error) {
      console.error('Failed to fetch notifications:', error);
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  };

  const handleMarkAsRead = async (id) => {
    try {
      await notificationService.markAsRead(id);
      fetchNotifications();
    } catch (error) {
      console.error('Failed to mark as read:', error);
    }
  };

  const handleDelete = async (id) => {
    try {
      await notificationService.delete(id);
      fetchNotifications();
    } catch (error) {
      console.error('Failed to delete notification:', error);
    }
  };

  return (
    <div className="p-6">
      <h1 className="text-3xl font-bold mb-6">Notifications</h1>

      <div className="space-y-4">
        {loading ? (
          <Card className="p-6 text-center">Loading...</Card>
        ) : notifications.length === 0 ? (
          <Card className="p-6 text-center">No notifications</Card>
        ) : (
          notifications.map((notification) => (
            <Card key={notification.id} className="p-4">
              <div className="flex justify-between items-start">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <Badge variant={notification.isRead ? 'secondary' : 'default'}>
                      {formatStatus(notification.type)}
                    </Badge>
                    {!notification.isRead && (
                      <Badge variant="destructive">New</Badge>
                    )}
                  </div>
                  <h3 className="font-semibold">{notification.title}</h3>
                  <p className="text-sm text-gray-600">{notification.message}</p>
                  <p className="text-xs text-gray-400 mt-2">
                    {new Date(notification.createdAt).toLocaleString()}
                  </p>
                </div>
                <div className="flex gap-2">
                  {!notification.isRead && (
                    <Button size="sm" onClick={() => handleMarkAsRead(notification.id)}>
                      Mark Read
                    </Button>
                  )}
                  <Button size="sm" variant="destructive" onClick={() => handleDelete(notification.id)}>
                    Delete
                  </Button>
                </div>
              </div>
            </Card>
          ))
        )}
      </div>
    </div>
  );
};

export default NotificationsPage;
