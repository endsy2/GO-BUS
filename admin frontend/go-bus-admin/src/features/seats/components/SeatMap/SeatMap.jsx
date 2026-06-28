import React from 'react';
import { Card } from 'shared/components/ui/card';

const SeatMap = ({ 
  seats = [], 
  layout = {}, 
  onSeatClick, 
  selectedSeats = [],
  showStatus = true,
  interactive = true 
}) => {
  const { rows = 10, columns = 4, seatConfiguration = {} } = layout;
  const aisles = seatConfiguration.aisles || [2];

  const getSeatAtPosition = (row, col) => {
    return seats.find(seat => seat.row === row && seat.column === col);
  };

  const getSeatClass = (seat, isSelected) => {
    if (!seat) return 'bg-transparent cursor-default';
    
    const baseClass = 'w-12 h-12 rounded-lg flex items-center justify-center text-xs font-semibold transition-all duration-200 border-2';
    
    if (isSelected) {
      return `${baseClass} bg-blue-500 text-white border-blue-600 scale-105 shadow-lg`;
    }

    switch (seat.status?.toLowerCase()) {
      case 'available':
        return `${baseClass} bg-green-100 text-green-800 border-green-300 hover:bg-green-200 ${interactive ? 'cursor-pointer hover:scale-105' : ''}`;
      case 'booked':
        return `${baseClass} bg-red-100 text-red-800 border-red-300 cursor-not-allowed opacity-75`;
      case 'reserved':
        return `${baseClass} bg-yellow-100 text-yellow-800 border-yellow-300 cursor-not-allowed opacity-75`;
      case 'maintenance':
        return `${baseClass} bg-gray-100 text-gray-800 border-gray-300 cursor-not-allowed opacity-75`;
      default:
        return `${baseClass} bg-gray-50 text-gray-600 border-gray-200 ${interactive ? 'cursor-pointer hover:bg-gray-100' : ''}`;
    }
  };

  const getSeatIcon = (seat) => {
    if (!seat) return null;
    
    switch (seat.type?.toLowerCase()) {
      case 'vip':
        return '⭐';
      case 'premium':
        return '💎';
      case 'standard':
      default:
        return '🪑';
    }
  };

  const handleSeatClick = (seat) => {
    if (!interactive || !seat || seat.status?.toLowerCase() !== 'available') return;
    if (onSeatClick) onSeatClick(seat);
  };

  const renderSeat = (row, col) => {
    const seat = getSeatAtPosition(row, col);
    const isSelected = selectedSeats.some(s => s.id === seat?.id);
    
    return (
      <div
        key={`${row}-${col}`}
        className={getSeatClass(seat, isSelected)}
        onClick={() => handleSeatClick(seat)}
        title={seat ? `Seat ${seat.seatNumber} - ${seat.status} (${seat.type})` : ''}
      >
        {seat && (
          <div className="flex flex-col items-center">
            <span className="text-lg">{getSeatIcon(seat)}</span>
            <span className="text-[10px] font-bold">{seat.seatNumber}</span>
          </div>
        )}
      </div>
    );
  };

  const renderRow = (rowIndex) => {
    const cols = [];
    for (let col = 1; col <= columns; col++) {
      cols.push(renderSeat(rowIndex, col));
      
      // Add aisle space
      if (aisles.includes(col) && col < columns) {
        cols.push(
          <div key={`aisle-${rowIndex}-${col}`} className="w-8" />
        );
      }
    }
    return cols;
  };

  return (
    <div className="flex flex-col items-center">
      {/* Driver Section */}
      <div className="mb-8 w-full max-w-md">
        <Card className="bg-gradient-to-r from-gray-700 to-gray-800 text-white p-4 rounded-t-3xl rounded-b-lg">
          <div className="text-center font-semibold">🚗 Driver</div>
        </Card>
      </div>

      {/* Seat Grid */}
      <div className="space-y-3">
        {Array.from({ length: rows }, (_, i) => i + 1).map((rowIndex) => (
          <div key={rowIndex} className="flex items-center gap-3">
            <span className="text-xs font-semibold text-gray-500 w-6">{rowIndex}</span>
            <div className="flex gap-2">
              {renderRow(rowIndex)}
            </div>
          </div>
        ))}
      </div>

      {/* Legend */}
      {showStatus && (
        <Card className="mt-8 p-4 w-full max-w-2xl">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-green-100 border-2 border-green-300 rounded-lg" />
              <span className="text-sm">Available</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-red-100 border-2 border-red-300 rounded-lg" />
              <span className="text-sm">Booked</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-yellow-100 border-2 border-yellow-300 rounded-lg" />
              <span className="text-sm">Reserved</span>
            </div>
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-blue-500 border-2 border-blue-600 rounded-lg" />
              <span className="text-sm">Selected</span>
            </div>
          </div>
        </Card>
      )}
    </div>
  );
};

export default SeatMap;
