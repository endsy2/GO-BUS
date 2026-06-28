import React, { useState, useEffect } from 'react';
import { Card } from 'shared/components/ui/card';
import { Button } from 'shared/components/ui/button';
import { Label } from 'shared/components/ui/label';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from 'shared/components/ui/select';
import { Badge } from 'shared/components/ui/badge';
import SeatMap from '../../components/SeatMap/SeatMap';
import seatService from '../../services/seatService';

const SeatsPage = () => {
  const [buses, setBuses] = useState([]);
  const [selectedBusId, setSelectedBusId] = useState('');
  const [seats, setSeats] = useState([]);
  const [layout, setLayout] = useState({});
  const [loading, setLoading] = useState(false);
  const [stats, setStats] = useState({
    available: 0,
    booked: 0,
    reserved: 0,
    maintenance: 0
  });

  useEffect(() => {
    // Fetch buses list - you'll need to implement this
    // For now using mock data
    setBuses([
      { id: 1, name: 'Bus 001', layout: { rows: 10, columns: 4 } },
      { id: 2, name: 'Bus 002', layout: { rows: 12, columns: 4 } },
    ]);
  }, []);

  useEffect(() => {
    if (selectedBusId) {
      fetchSeats();
    }
  }, [selectedBusId]);

  const fetchSeats = async () => {
    try {
      setLoading(true);
      const result = await seatService.getByBus(selectedBusId);
      const seatData = result.data || result;
      const seatArray = Array.isArray(seatData) ? seatData : [];
      setSeats(seatArray);
      
      // Calculate stats
      const newStats = {
        available: seatArray.filter(s => s.status?.toLowerCase() === 'available').length,
        booked: seatArray.filter(s => s.status?.toLowerCase() === 'booked').length,
        reserved: seatArray.filter(s => s.status?.toLowerCase() === 'reserved').length,
        maintenance: seatArray.filter(s => s.status?.toLowerCase() === 'maintenance').length,
      };
      setStats(newStats);
      
      // Get layout from selected bus
      const bus = buses.find(b => b.id === parseInt(selectedBusId));
      if (bus) {
        setLayout(bus.layout || {});
      }
    } catch (error) {
      console.error('Failed to fetch seats:', error);
      setSeats([]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <h1 className="text-3xl font-bold">Seat Management</h1>
      </div>

      {/* Bus Selection */}
      <Card className="p-4">
        <div className="flex items-center gap-4">
          <Label className="text-sm font-semibold">Select Bus:</Label>
          <Select value={selectedBusId} onValueChange={setSelectedBusId}>
            <SelectTrigger className="w-64">
              <SelectValue placeholder="Choose A Bus" />
            </SelectTrigger>
            <SelectContent>
              {buses.map(bus => (
                <SelectItem key={bus.id} value={bus.id.toString()}>
                  {bus.name}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>
      </Card>

      {selectedBusId && (
        <>
          {/* Stats Cards */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <Card className="p-4 bg-green-50 border-green-200">
              <div className="text-sm text-green-600 font-semibold">Available</div>
              <div className="text-3xl font-bold text-green-700 mt-2">{stats.available}</div>
            </Card>
            <Card className="p-4 bg-red-50 border-red-200">
              <div className="text-sm text-red-600 font-semibold">Booked</div>
              <div className="text-3xl font-bold text-red-700 mt-2">{stats.booked}</div>
            </Card>
            <Card className="p-4 bg-yellow-50 border-yellow-200">
              <div className="text-sm text-yellow-600 font-semibold">Reserved</div>
              <div className="text-3xl font-bold text-yellow-700 mt-2">{stats.reserved}</div>
            </Card>
            <Card className="p-4 bg-gray-50 border-gray-200">
              <div className="text-sm text-gray-600 font-semibold">Maintenance</div>
              <div className="text-3xl font-bold text-gray-700 mt-2">{stats.maintenance}</div>
            </Card>
          </div>

          {/* Seat Map */}
          <Card className="p-6">
            {loading ? (
              <div className="text-center py-12 text-gray-500">Loading seats...</div>
            ) : (
              <SeatMap
                seats={seats}
                layout={layout}
                showStatus={true}
                interactive={false}
              />
            )}
          </Card>
        </>
      )}

      {!selectedBusId && (
        <Card className="p-12">
          <div className="text-center text-gray-500">
            <div className="text-6xl mb-4">🚌</div>
            <div className="text-lg">Select a bus to view seat layout</div>
          </div>
        </Card>
      )}
    </div>
  );
};

export default SeatsPage;
