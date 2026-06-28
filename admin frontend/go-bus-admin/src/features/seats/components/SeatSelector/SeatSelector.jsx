import React, { useState, useEffect } from 'react';
import { Card } from 'shared/components/ui/card';
import { Button } from 'shared/components/ui/button';
import { Badge } from 'shared/components/ui/badge';
import { useToast } from 'shared/components/ui/toast';
import SeatMap from '../SeatMap/SeatMap';
import seatService from '../../services/seatService';

const SeatSelector = ({ busId, layout, onSelectionChange, maxSeats = null }) => {
  const { addToast } = useToast();
  const [seats, setSeats] = useState([]);
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (busId) {
      fetchSeats();
    }
  }, [busId]);

  const fetchSeats = async () => {
    try {
      setLoading(true);
      const result = await seatService.getByBus(busId);
      const seatData = result.data || result;
      setSeats(Array.isArray(seatData) ? seatData : []);
    } catch (error) {
      console.error('Failed to fetch seats:', error);
      setSeats([]);
    } finally {
      setLoading(false);
    }
  };

  const handleSeatClick = (seat) => {
    if (seat.status?.toLowerCase() !== 'available') return;

    const isSelected = selectedSeats.some(s => s.id === seat.id);
    
    let newSelection;
    if (isSelected) {
      newSelection = selectedSeats.filter(s => s.id !== seat.id);
    } else {
      if (maxSeats && selectedSeats.length >= maxSeats) {
        addToast({ message: `You can only select up to ${maxSeats} seat(s)`, type: 'warning' });
        return;
      }
      newSelection = [...selectedSeats, seat];
    }
    
    setSelectedSeats(newSelection);
    if (onSelectionChange) {
      onSelectionChange(newSelection);
    }
  };

  const clearSelection = () => {
    setSelectedSeats([]);
    if (onSelectionChange) {
      onSelectionChange([]);
    }
  };

  const availableCount = seats.filter(s => s.status?.toLowerCase() === 'available').length;
  const totalPrice = selectedSeats.reduce((sum, seat) => sum + (seat.price || 0), 0);

  if (loading) {
    return (
      <Card className="p-8">
        <div className="text-center text-gray-500">Loading seats...</div>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      {/* Selection Summary */}
      <Card className="p-4">
        <div className="flex justify-between items-center">
          <div className="flex gap-4">
            <div>
              <span className="text-sm text-muted-foreground">Available Seats:</span>
              <Badge className="ml-2" variant="outline">{availableCount}</Badge>
            </div>
            <div>
              <span className="text-sm text-muted-foreground">Selected:</span>
              <Badge className="ml-2">{selectedSeats.length}</Badge>
            </div>
            {totalPrice > 0 && (
              <div>
                <span className="text-sm text-muted-foreground">Total:</span>
                <Badge className="ml-2" variant="secondary">${totalPrice.toFixed(2)}</Badge>
              </div>
            )}
          </div>
          {selectedSeats.length > 0 && (
            <Button size="sm" variant="outline" onClick={clearSelection}>
              Clear Selection
            </Button>
          )}
        </div>
        
        {selectedSeats.length > 0 && (
          <div className="mt-3 pt-3 border-t">
            <span className="text-sm font-semibold text-foreground">Selected Seats: </span>
            <div className="flex flex-wrap gap-2 mt-2">
              {selectedSeats.map(seat => (
                <Badge key={seat.id} variant="default">
                  {seat.seatNumber}
                </Badge>
              ))}
            </div>
          </div>
        )}
      </Card>

      {/* Seat Map */}
      <Card className="p-6">
        <SeatMap
          seats={seats}
          layout={layout}
          onSeatClick={handleSeatClick}
          selectedSeats={selectedSeats}
          showStatus={true}
          interactive={true}
        />
      </Card>
    </div>
  );
};

export default SeatSelector;
