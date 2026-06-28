import axiosInstance from 'services/axiosConfig';

const bookingService = {
  // Admin: Get all bookings with filters and pagination
  filterBookings: async (filters = {}, page = 1, size = 20) => {
    const params = new URLSearchParams();

    if (filters.username) params.append('username', filters.username);
    if (filters.bookingStatus) params.append('bookingStatus', filters.bookingStatus);
    if (filters.paymentStatus) params.append('paymentStatus', filters.paymentStatus);
    if (filters.paymentMethod) params.append('paymentMethod', filters.paymentMethod);
    // refund is a tri-state: '' (all) / 'true' / 'false'. Only send when set.
    if (filters.refund === 'true' || filters.refund === 'false') {
      params.append('refund', filters.refund);
    }
    if (filters.departureFrom) params.append('departureFrom', filters.departureFrom);
    if (filters.departureTo) params.append('departureTo', filters.departureTo);
    if (filters.createdFrom) params.append('createdFrom', filters.createdFrom);
    if (filters.createdTo) params.append('createdTo', filters.createdTo);
    params.append('pageStart', page);
    params.append('pageSize', size);

    const response = await axiosInstance.get(`/admin/bookings?${params.toString()}`);
    return response.data;
  },

  // Get booking by ID
  getBookingById: async (id) => {
    const response = await axiosInstance.get(`/bookings/${id}`);
    return response.data;
  },

  // Create booking
  createBooking: async (bookingData) => {
    const response = await axiosInstance.post('/bookings', bookingData);
    return response.data;
  },

  // Admin: Force mark as paid
  forceMarkPaid: async (id) => {
    const response = await axiosInstance.patch(`/admin/bookings/${id}/force-pay`);
    return response.data;
  },
};

export default bookingService;
