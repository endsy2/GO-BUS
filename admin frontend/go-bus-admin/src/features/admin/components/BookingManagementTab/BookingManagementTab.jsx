import React, { useState, useEffect } from 'react';
import { formatStatus } from 'shared/utils/formatters';
import { Card } from 'shared/components/ui/card';
import { Button } from 'shared/components/ui/button';
import { SearchInput } from 'shared/components/common/SearchInput';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from 'shared/components/ui/table';
import { Badge } from 'shared/components/ui/badge';
import adminService from '../../services/adminService';

const BookingManagementTab = () => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    fetchBookings();
  }, []);

  const fetchBookings = async () => {
    try {
      setLoading(true);
      const result = await adminService.bookings.list({});
      const bookingData = result.data || result;
      setBookings(Array.isArray(bookingData) ? bookingData : []);
    } catch (error) {
      console.error('Failed to fetch bookings:', error);
      setBookings([]);
    } finally {
      setLoading(false);
    }
  };

  const handleConfirm = async (id) => {
    try {
      await adminService.bookings.confirm(id);
      fetchBookings();
    } catch (error) {
      console.error('Failed to confirm booking:', error);
    }
  };

  const handleCancel = async (id) => {
    if (window.confirm('Cancel this booking?')) {
      try {
        await adminService.bookings.cancel(id);
        fetchBookings();
      } catch (error) {
        console.error('Failed to cancel booking:', error);
      }
    }
  };

  const handleForcePay = async (id) => {
    if (window.confirm('Force mark as paid?')) {
      try {
        await adminService.bookings.forcePay(id);
        fetchBookings();
      } catch (error) {
        console.error('Failed to force pay:', error);
      }
    }
  };

  const filteredBookings = bookings.filter(booking =>
    booking.bookingReference?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    booking.customerName?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <Card className="p-6">
      <div className="mb-4">
        <SearchInput
          placeholder="Search Bookings..."
          value={searchTerm}
          onChange={setSearchTerm}
        />
      </div>

      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Reference</TableHead>
            <TableHead>Customer</TableHead>
            <TableHead>Route</TableHead>
            <TableHead>Status</TableHead>
            <TableHead>Payment</TableHead>
            <TableHead>Actions</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {loading ? (
            <TableRow>
              <TableCell colSpan={6} className="text-center">Loading...</TableCell>
            </TableRow>
          ) : filteredBookings.length === 0 ? (
            <TableRow>
              <TableCell colSpan={6} className="text-center">No bookings found</TableCell>
            </TableRow>
          ) : (
            filteredBookings.map((booking) => (
              <TableRow key={booking.id}>
                <TableCell className="font-mono">{booking.bookingReference}</TableCell>
                <TableCell>{booking.customerName}</TableCell>
                <TableCell>{booking.routeName}</TableCell>
                <TableCell>
                  <Badge variant={booking.status === 'CONFIRMED' ? 'default' : 'secondary'}>
                    {formatStatus(booking.status)}
                  </Badge>
                </TableCell>
                <TableCell>
                  <Badge variant={booking.paymentStatus === 'PAID' ? 'default' : 'destructive'}>
                    {formatStatus(booking.paymentStatus)}
                  </Badge>
                </TableCell>
                <TableCell>
                  <div className="flex gap-2">
                    {booking.status !== 'CONFIRMED' && (
                      <Button size="sm" onClick={() => handleConfirm(booking.id)}>Confirm</Button>
                    )}
                    {booking.paymentStatus !== 'PAID' && (
                      <Button size="sm" variant="outline" onClick={() => handleForcePay(booking.id)}>Force Pay</Button>
                    )}
                    <Button size="sm" variant="destructive" onClick={() => handleCancel(booking.id)}>Cancel</Button>
                  </div>
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </Card>
  );
};

export default BookingManagementTab;
