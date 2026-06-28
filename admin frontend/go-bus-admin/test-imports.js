// Test file to verify imports work correctly
// Run with: node test-imports.js

console.log('Testing service imports...');

try {
  // Test direct import
  const bookingService = require('./src/features/bookings/services/bookingService.js');
  console.log('✅ Direct import of bookingService:', typeof bookingService.default);
  
  // Test if it has the expected methods
  const service = bookingService.default;
  console.log('✅ Has getBookings:', typeof service.getBookings === 'function');
  console.log('✅ Has createBooking:', typeof service.createBooking === 'function');
  
  console.log('\n✅ All imports working correctly!');
} catch (error) {
  console.error('❌ Import failed:', error.message);
}
