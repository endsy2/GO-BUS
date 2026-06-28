import React, { useState } from 'react';
import { formatStatus } from 'shared/utils/formatters';
import { Card } from 'shared/components/ui/card';
import { Button } from 'shared/components/ui/button';
import { Badge } from 'shared/components/ui/badge';
import { Label } from 'shared/components/ui/label';
import { Separator } from 'shared/components/ui/separator';
import { useToast } from 'shared/components/ui/toast';
import SeatSelector from '../SeatSelector/SeatSelector';

const SeatBookingFlow = ({
  busId,
  layout,
  onConfirm,
  onCancel,
  maxSeats = 5,
  showPricing = true
}) => {
  const { addToast } = useToast();
  const [selectedSeats, setSelectedSeats] = useState([]);
  const [step, setStep] = useState(1); // 1: Select, 2: Confirm

  const handleSelectionChange = (seats) => {
    setSelectedSeats(seats);
  };

  const handleContinue = () => {
    if (selectedSeats.length === 0) {
      addToast({ message: 'Please select at least one seat', type: 'warning' });
      return;
    }
    setStep(2);
  };

  const handleConfirm = () => {
    if (onConfirm) {
      onConfirm(selectedSeats);
    }
  };

  const totalPrice = selectedSeats.reduce((sum, seat) => sum + (seat.price || 0), 0);

  if (step === 2) {
    return (
      <div className="space-y-6">
        <Card className="p-6">
          <h2 className="text-xl font-bold mb-4">Confirm Your Booking</h2>
          
          <div className="space-y-4">
            <div>
              <Label className="text-sm font-semibold text-muted-foreground">Selected Seats</Label>
              <div className="flex flex-wrap gap-2 mt-2">
                {selectedSeats.map(seat => (
                  <Badge key={seat.id} variant="default" className="text-base px-3 py-1">
                    {seat.seatNumber}
                  </Badge>
                ))}
              </div>
            </div>

            <Separator />

            <div className="space-y-2">
              {selectedSeats.map(seat => (
                <div key={seat.id} className="flex justify-between items-center">
                  <div className="flex items-center gap-2">
                    <span className="font-semibold">Seat {seat.seatNumber}</span>
                    <Badge variant="outline" className="text-xs">{formatStatus(seat.type)}</Badge>
                  </div>
                  {showPricing && (
                    <span className="font-semibold">${(seat.price || 0).toFixed(2)}</span>
                  )}
                </div>
              ))}
            </div>

            {showPricing && (
              <>
                <Separator />
                <div className="flex justify-between items-center text-lg font-bold">
                  <span>Total Amount</span>
                  <span className="text-primary">${totalPrice.toFixed(2)}</span>
                </div>
              </>
            )}
          </div>

          <div className="flex gap-3 mt-6">
            <Button variant="outline" onClick={() => setStep(1)} className="flex-1">
              Back to Selection
            </Button>
            <Button onClick={handleConfirm} className="flex-1">
              Confirm Booking
            </Button>
          </div>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <Card className="p-6">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-bold">Select Your Seats</h2>
          <Badge variant="secondary">
            Step 1 of 2
          </Badge>
        </div>
        <p className="text-sm text-muted-foreground">
          Click on available seats to select them. You can select up to {maxSeats} seat(s).
        </p>
      </Card>

      <SeatSelector
        busId={busId}
        layout={layout}
        onSelectionChange={handleSelectionChange}
        maxSeats={maxSeats}
      />

      <Card className="p-4">
        <div className="flex gap-3">
          {onCancel && (
            <Button variant="outline" onClick={onCancel} className="flex-1">
              Cancel
            </Button>
          )}
          <Button 
            onClick={handleContinue} 
            disabled={selectedSeats.length === 0}
            className="flex-1"
          >
            Continue to Confirmation ({selectedSeats.length} seat{selectedSeats.length !== 1 ? 's' : ''})
          </Button>
        </div>
      </Card>
    </div>
  );
};

export default SeatBookingFlow;
