import React from 'react';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from 'shared/components/ui/table';
import { Card, CardContent, CardHeader, CardTitle } from 'shared/components/ui/card';
import { Badge } from 'shared/components/common/Badge';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { Loader2 } from 'lucide-react';

const BookingTable = ({ bookings, loading }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  
  
  if (loading) {
    return (
      <Card>
        <CardContent className="flex items-center justify-center py-10">
          <Loader2 className="h-6 w-6 animate-spin text-muted-foreground" />
          <span className="ml-2 text-muted-foreground">{t('loadingBookings')}</span>
        </CardContent>
      </Card>
    );
  }

  if (!bookings || bookings.length === 0) {
    return (
      <Card>
        <CardHeader>
          <CardTitle>{t('recentBookings')}</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center py-10 text-muted-foreground">
            {t('noBookingsFound')}
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card>
      <CardHeader>
        <CardTitle>{t('recentBookings')}</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="overflow-x-auto">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>{t('bookingId')}</TableHead>
                <TableHead>{t('customer')}</TableHead>
                <TableHead>{t('destination')}</TableHead>
                <TableHead>{t('createdDate')}</TableHead>
                <TableHead>{t('status')}</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {bookings.map(booking => (
                <TableRow key={booking.id}>
                  <TableCell>#{booking.id}</TableCell>
                  <TableCell>{booking.fullName || 'N/A'}</TableCell>
                  <TableCell>{booking.destination || 'N/A'}</TableCell>
                  <TableCell>
                    {booking.createdAt 
                      ? new Date(booking.createdAt).toLocaleDateString() 
                      : 'N/A'}
                  </TableCell>
                  <TableCell>
                    <Badge variant={booking.bookingStatus && typeof booking.bookingStatus === 'string' ? booking.bookingStatus.toLowerCase() : 'default'}>
                      {booking.bookingStatus || 'N/A'}
                    </Badge>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  );
};

export default BookingTable;
