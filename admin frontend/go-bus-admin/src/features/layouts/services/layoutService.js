import axiosInstance from 'services/axiosConfig';

const layoutService = {
  // Get all layouts
  getAllLayouts: async () => {
    const response = await axiosInstance.get('/layouts');
    return response.data;
  },

  // Get layout by ID
  getLayoutById: async (id) => {
    const response = await axiosInstance.get(`/layouts/${id}`);
    return response.data;
  },

  // Get layout by name
  getLayoutByName: async (name) => {
    const response = await axiosInstance.get(`/layouts/name/${name}`);
    return response.data;
  },

  // Create layout
  createLayout: async (layoutData) => {
    // Convert layout object to JSON string for API (backend expects String)
    // Remove isAvailable field from seats, blocked seats are null
    const payload = {
      name: layoutData.name,
      layout: JSON.stringify({
        rows: layoutData.rows,
        columns: layoutData.columns,
        totalSeats: layoutData.totalSeats,
        driverColumn: layoutData.driverColumn,
        aisleColumns: layoutData.aisleColumns,
        seats: layoutData.seats,
      }),
      description: layoutData.description || null
    };
    const response = await axiosInstance.post('/layouts', payload);
    return response.data;
  },

  // Update layout
  updateLayout: async (id, layoutData) => {
    // Convert layout object to JSON string for API (backend expects String)
    // Remove isAvailable field from seats, blocked seats are null
    const payload = {
      name: layoutData.name,
      layout: JSON.stringify({
        rows: layoutData.rows,
        columns: layoutData.columns,
        totalSeats: layoutData.totalSeats,
        driverColumn: layoutData.driverColumn,
        aisleColumns: layoutData.aisleColumns,
        seats: layoutData.seats,
      }),
      description: layoutData.description || null
    };
    const response = await axiosInstance.put(`/layouts/${id}`, payload);
    return response.data;
  },

  // Delete layout
  deleteLayout: async (id) => {
    const response = await axiosInstance.delete(`/layouts/${id}`);
    return response.data;
  },
};

export default layoutService;
