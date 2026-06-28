// Export services from feature folders
// Import default exports and re-export them
import authService from '../features/auth/services/authService';
import userService from '../features/team/services/teamService';
import busService from '../features/buses/services/busService';
import routeService from '../features/routes/services/routeService';
import bookingService from '../features/bookings/services/bookingService';
import customerService from '../features/customers/services/customerService';
import reportService from '../features/reports/services/reportService';

export { authService, userService, busService, routeService, bookingService, customerService, reportService };

// Export axios configuration
export { default as axiosInstance } from './axiosConfig';
export { setUnauthorizedHandler } from './axiosConfig';

