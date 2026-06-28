import React, { useState } from 'react';
import { Card } from 'shared/components/ui/card';
import { Button } from 'shared/components/ui/button';
import { Input } from 'shared/components/ui/input';
import { Label } from 'shared/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from 'shared/components/ui/select';

const SeatLayoutEditor = ({ layout, seats = [], onSeatsChange }) => {
  const { rows = 10, columns = 4, seatConfiguration = {} } = layout;
  const aisles = seatConfiguration.aisles || [2];
  
  const [editingSeats, setEditingSeats] = useState(seats);

  const getSeatAtPosition = (row, col) => {
    return editingSeats.find(seat => seat.row === row && seat.column === col);
  };

  const updateSeat = (row, col, updates) => {
    const existingSeat = getSeatAtPosition(row, col);
    let newSeats;
    
    if (existingSeat) {
      newSeats = editingSeats.map(seat => 
        seat.row === row && seat.column === col 
          ? { ...seat, ...updates }
          : seat
      );
    } else {
      const seatNumber = `${String.fromCharCode(64 + row)}${col}`;
      newSeats = [...editingSeats, {
        row,
        column: col,
        seatNumber,
        type: 'standard',
        status: 'available',
        ...updates
      }];
    }
    
    setEditingSeats(newSeats);
    if (onSeatsChange) onSeatsChange(newSeats);
  };

  const toggleSeat = (row, col) => {
    const seat = getSeatAtPosition(row, col);
    if (seat) {
      const newSeats = editingSeats.filter(s => !(s.row === row && s.column === col));
      setEditingSeats(newSeats);
      if (onSeatsChange) onSeatsChange(newSeats);
    } else {
      updateSeat(row, col, {});
    }
  };

  const getSeatClass = (seat) => {
    if (!seat) {
      return 'bg-gray-50 border-2 border-dashed border-gray-300 cursor-pointer hover:bg-gray-100';
    }
    
    const typeColors = {
      vip: 'bg-purple-100 border-purple-400',
      premium: 'bg-blue-100 border-blue-400',
      standard: 'bg-green-100 border-green-400'
    };
    
    return `${typeColors[seat.type] || typeColors.standard} border-2 cursor-pointer hover:scale-105`;
  };

  const getSeatIcon = (seat) => {
    if (!seat) return '+';
    
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

  const renderSeat = (row, col) => {
    const seat = getSeatAtPosition(row, col);
    
    return (
      <div
        key={`${row}-${col}`}
        className={`w-14 h-14 rounded-lg flex flex-col items-center justify-center text-xs font-semibold transition-all duration-200 ${getSeatClass(seat)}`}
        onClick={() => toggleSeat(row, col)}
        title={seat ? `${seat.seatNumber} - ${seat.type}` : 'Click to add seat'}
      >
        <span className="text-lg">{getSeatIcon(seat)}</span>
        {seat && <span className="text-[10px] font-bold">{seat.seatNumber}</span>}
      </div>
    );
  };

  const renderRow = (rowIndex) => {
    const cols = [];
    for (let col = 1; col <= columns; col++) {
      cols.push(renderSeat(rowIndex, col));
      
      if (aisles.includes(col) && col < columns) {
        cols.push(
          <div key={`aisle-${rowIndex}-${col}`} className="w-6 flex items-center justify-center">
            <div className="h-full w-px bg-gray-300" />
          </div>
        );
      }
    }
    return cols;
  };

  const autoFillSeats = () => {
    const newSeats = [];
    for (let row = 1; row <= rows; row++) {
      for (let col = 1; col <= columns; col++) {
        const seatNumber = `${String.fromCharCode(64 + row)}${col}`;
        newSeats.push({
          row,
          column: col,
          seatNumber,
          type: 'standard',
          status: 'available'
        });
      }
    }
    setEditingSeats(newSeats);
    if (onSeatsChange) onSeatsChange(newSeats);
  };

  return (
    <div className="space-y-6">
      {/* Controls */}
      <Card className="p-4">
        <div className="flex justify-between items-center">
          <div className="text-sm text-gray-600">
            Click seats to add/remove. Total: <Badge>{editingSeats.length}</Badge>
          </div>
          <Button size="sm" onClick={autoFillSeats}>
            Auto-Fill All Seats
          </Button>
        </div>
      </Card>

      {/* Driver Section */}
      <div className="flex justify-center">
        <Card className="bg-gradient-to-r from-gray-700 to-gray-800 text-white p-4 rounded-t-3xl rounded-b-lg w-64">
          <div className="text-center font-semibold">🚗 Driver</div>
        </Card>
      </div>

      {/* Seat Grid */}
      <div className="flex justify-center">
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
      </div>

      {/* Type Legend */}
      <Card className="p-4">
        <div className="grid grid-cols-3 gap-4">
          <div className="flex items-center gap-2">
            <div className="w-10 h-10 bg-green-100 border-2 border-green-400 rounded-lg flex items-center justify-center">
              🪑
            </div>
            <span className="text-sm">Standard</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-10 h-10 bg-blue-100 border-2 border-blue-400 rounded-lg flex items-center justify-center">
              💎
            </div>
            <span className="text-sm">Premium</span>
          </div>
          <div className="flex items-center gap-2">
            <div className="w-10 h-10 bg-purple-100 border-2 border-purple-400 rounded-lg flex items-center justify-center">
              ⭐
            </div>
            <span className="text-sm">VIP</span>
          </div>
        </div>
      </Card>
    </div>
  );
};

export default SeatLayoutEditor;
