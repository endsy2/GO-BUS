import axiosInstance from 'services/axiosConfig';

const scheduleService = {
  // Get all schedules with pagination
  getSchedules: async (pageNo = 1, pageSize = 10) => {
    const response = await axiosInstance.get('/schedules', {
      params: { pageNo, pageSize }
    });
    return response.data;
  },

  // Get schedule by ID
  getScheduleById: async (id) => {
    const response = await axiosInstance.get(`/schedules/${id}`);
    return response.data;
  },

  // Get all schedules for a specific bus (used to pre-check time conflicts)
  getSchedulesByBus: async (busId) => {
    const response = await axiosInstance.get(`/schedules/bus/${busId}`);
    return response.data;
  },

  // Create new schedule
  createSchedule: async (scheduleData) => {
    const response = await axiosInstance.post('/schedules', scheduleData);
    return response.data;
  },

  // Update schedule
  updateSchedule: async (id, scheduleData) => {
    const response = await axiosInstance.put(`/schedules/${id}`, scheduleData);
    return response.data;
  },

  // Delete schedule
  deleteSchedule: async (id) => {
    const response = await axiosInstance.delete(`/schedules/${id}`);
    return response.data;
  },

  // Get schedules with filters and pagination
  // All filter parameters are now optional
  filterSchedules: async (routeId = null, fromDate = null, toDate = null, maxPrice = null, pageNo = 1, pageSize = 10) => {
    const params = new URLSearchParams();
    
    // Only add parameters if they have values
    if (routeId) params.append('routeId', routeId);
    
    // Backend expects LocalDateTime in ISO.DATE_TIME format (e.g. 2026-05-31T00:00:00).
    // The pickers give us a YYYY-MM-DD date, so we attach a fixed time:
    //  - fromDate -> start of the day (00:00:00)
    //  - toDate   -> end of the day   (23:59:59) so the whole day is included
    if (fromDate) {
      const datePart = String(fromDate).split('T')[0]; // YYYY-MM-DD
      params.append('fromDate', `${datePart}T00:00:00`);
    }
    if (toDate) {
      const datePart = String(toDate).split('T')[0]; // YYYY-MM-DD
      params.append('toDate', `${datePart}T23:59:59`);
    }
    
    if (maxPrice) params.append('maxPrice', maxPrice);
    params.append('pageNo', pageNo);
    params.append('pageSize', pageSize);

    const response = await axiosInstance.get(`/schedules/filter/specification?${params.toString()}`);
    return response.data;
  },

  // Get schedule seats by schedule ID
  getScheduleSeats: async (scheduleId) => {
    const response = await axiosInstance.get(`/schedule-seats/schedule/${scheduleId}`);
    return response.data;
  },

  // Get bus details by schedule ID (legacy - if still needed)
  getBusBySchedule: async (scheduleId) => {
    const response = await axiosInstance.get(`/buses/schedule/${scheduleId}`);
    return response.data;
  },
};

export default scheduleService;
