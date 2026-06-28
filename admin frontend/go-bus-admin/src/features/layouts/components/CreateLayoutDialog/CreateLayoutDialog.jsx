import React, { useState, useEffect } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from 'shared/components/ui/dialog';
import { Input } from 'shared/components/common/Input';
import { Button } from 'shared/components/common/Button';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import { useToast } from 'shared/components/ui/toast';
import { Loader2, LayoutGrid, Armchair, X } from 'lucide-react';
import layoutService from '../../services/layoutService';

const CreateLayoutDialog = ({ open, onClose, onSuccess }) => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const { addToast } = useToast();
  
  const [formData, setFormData] = useState({
    name: '',
    rows: '',
    columns: '',
    description: '',
    driverColumn: '',
    aisleColumns: ''
  });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [seatLayout, setSeatLayout] = useState([]);

  // Generate seat layout when rows/columns change
  useEffect(() => {
    const rows = parseInt(formData.rows) || 0;
    const cols = parseInt(formData.columns) || 0;
    
    if (rows > 0 && cols > 0 && rows <= 20 && cols <= 10) {
      const newLayout = [];
      for (let r = 0; r < rows; r++) {
        const row = [];
        for (let c = 0; c < cols; c++) {
          row.push({
            row: r,
            col: c,
            seatNumber: `${String.fromCharCode(65 + r)}${c + 1}`,
            isAvailable: true
          });
        }
        newLayout.push(row);
      }
      setSeatLayout(newLayout);
    } else {
      setSeatLayout([]);
    }
  }, [formData.rows, formData.columns]);

  const toggleSeatAvailability = (rowIndex, colIndex) => {
    setSeatLayout(prev => {
      const newLayout = [...prev];
      newLayout[rowIndex] = [...newLayout[rowIndex]];
      newLayout[rowIndex][colIndex] = {
        ...newLayout[rowIndex][colIndex],
        isAvailable: !newLayout[rowIndex][colIndex].isAvailable
      };
      return newLayout;
    });
  };

  const getAvailableSeatsCount = () => {
    return seatLayout.flat().filter(seat => seat.isAvailable).length;
  };

  const getAisleColumnsList = () => {
    const aisleColsStr = formData.aisleColumns.trim();
    if (!aisleColsStr) return [];
    return aisleColsStr.split(',').map(c => parseInt(c.trim()) - 1).filter(c => !isNaN(c) && c >= 0);
  };

  const isAisleColumn = (colIndex) => {
    return getAisleColumnsList().includes(colIndex);
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }
  };

  const validate = () => {
    const newErrors = {};
    if (!formData.name.trim()) {
      newErrors.name = t('nameRequired') || 'Name is required';
    }
    if (!formData.rows || formData.rows <= 0) {
      newErrors.rows = t('rowsRequired') || 'Rows must be greater than 0';
    }
    if (formData.rows > 20) {
      newErrors.rows = 'Maximum 20 rows allowed';
    }
    if (!formData.columns || formData.columns <= 0) {
      newErrors.columns = t('columnsRequired') || 'Columns must be greater than 0';
    }
    if (formData.columns > 10) {
      newErrors.columns = 'Maximum 10 columns allowed';
    }
    return newErrors;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    const newErrors = validate();
    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);
    try {
      const availableSeats = getAvailableSeatsCount();
      
      // Prepare seat data - remove isAvailable field
      // For blocked seats, send null to maintain 2D structure
      const seats = seatLayout.map(row => 
        row.map(seat => 
          seat.isAvailable ? { seatNumber: seat.seatNumber } : null
        )
      );
      
      const payload = {
        name: formData.name.trim(),
        rows: parseInt(formData.rows),
        columns: parseInt(formData.columns),
        totalSeats: availableSeats,
        driverColumn: formData.driverColumn ? parseInt(formData.driverColumn) : null,
        aisleColumns: formData.aisleColumns.trim() || null,
        seats: seats,
        description: formData.description.trim() || null
      };
      
      await layoutService.createLayout(payload);
      setFormData({ name: '', rows: '', columns: '', description: '', driverColumn: '', aisleColumns: '' });
      setSeatLayout([]);
      setErrors({});
      onSuccess();
    } catch (error) {
      const errorMessage = error.response?.data?.message || 'Failed to create layout';
      addToast({ message: errorMessage, type: 'error' });
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    if (!loading) {
      setFormData({ name: '', rows: '', columns: '', description: '', driverColumn: '', aisleColumns: '' });
      setSeatLayout([]);
      setErrors({});
      onClose();
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleClose}>
      <DialogContent className="sm:max-w-4xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <LayoutGrid className="w-5 h-5 text-primary" />
            {t('createLayout') || 'Create Layout'}
          </DialogTitle>
        </DialogHeader>

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Left Column - Form */}
            <div className="space-y-4">
              <Input
                label={t('layoutName') || 'Layout Name'}
                name="name"
                value={formData.name}
                onChange={handleChange}
                placeholder={t('enterLayoutName') || 'e.g., Standard 40-Seater'}
                error={errors.name}
                required
              />

              <div className="grid grid-cols-2 gap-4">
                <Input
                  label={t('rows') || 'Rows'}
                  name="rows"
                  type="number"
                  min="1"
                  max="20"
                  value={formData.rows}
                  onChange={handleChange}
                  placeholder="10"
                  error={errors.rows}
                  required
                />

                <Input
                  label={t('columns') || 'Columns'}
                  name="columns"
                  type="number"
                  min="1"
                  max="10"
                  value={formData.columns}
                  onChange={handleChange}
                  placeholder="4"
                  error={errors.columns}
                  required
                />
              </div>

              <Input
                label={t('driverColumn') || 'Driver Column'}
                name="driverColumn"
                type="number"
                min="1"
                max={formData.columns || 10}
                value={formData.driverColumn}
                onChange={handleChange}
                placeholder="1"
              />

              <Input
                label={t('aisleColumns') || 'Aisle Columns'}
                name="aisleColumns"
                type="text"
                value={formData.aisleColumns}
                onChange={handleChange}
                placeholder="2,3"
              />
              <p className="text-xs text-slate-500 dark:text-slate-400 -mt-2">
                💡 {t('aisleColumnsHelp') || 'Enter column numbers separated by commas (e.g., "2,3")'}
              </p>

              <div>
                <label className="block text-sm font-medium text-slate-700 dark:text-slate-300 mb-2">
                  {t('description') || 'Description'} ({t('optional') || 'Optional'})
                </label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  placeholder={t('enterDescription') || 'Enter layout description...'}
                  rows={3}
                  className="w-full px-3 py-2 border border-slate-300 dark:border-slate-600 rounded-lg bg-white dark:bg-slate-800 text-slate-900 dark:text-slate-100 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-primary focus:border-transparent resize-none"
                />
              </div>

              {seatLayout.length > 0 && (
                <div className="bg-blue-50 dark:bg-blue-900/20 border border-blue-200 dark:border-blue-800 rounded-lg p-4">
                  <h4 className="font-semibold text-slate-900 dark:text-slate-100 mb-2">
                    {t('layoutSummary') || 'Layout Summary'}
                  </h4>
                  <div className="space-y-1 text-sm">
                    <div className="flex justify-between">
                      <span className="text-slate-600 dark:text-slate-400">
                        {t('customerSeats') || 'Customer Seats'}:
                      </span>
                      <span className="font-semibold text-slate-900 dark:text-slate-100">
                        {getAvailableSeatsCount()}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-slate-600 dark:text-slate-400">
                        {t('blockedSeats') || 'Blocked'}:
                      </span>
                      <span className="font-semibold text-slate-900 dark:text-slate-100">
                        {seatLayout.flat().filter(s => !s.isAvailable).length}
                      </span>
                    </div>
                  </div>
                </div>
              )}

              <div className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg p-4">
                <h4 className="font-semibold text-slate-900 dark:text-slate-100 mb-3 text-sm">
                  {t('legend') || 'Legend'}
                </h4>
                <div className="space-y-2 text-xs">
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-green-500 rounded flex items-center justify-center">
                      <Armchair className="w-4 h-4 text-white" />
                    </div>
                    <span className="text-slate-700 dark:text-slate-300">
                      {t('availableSeat') || 'Available Seat'}
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-slate-300 dark:bg-slate-600 rounded flex items-center justify-center">
                      <X className="w-4 h-4 text-slate-600 dark:text-slate-300" />
                    </div>
                    <span className="text-slate-700 dark:text-slate-300">
                      {t('blockedSeat') || 'Blocked Seat'}
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-slate-800 dark:bg-slate-700 rounded flex items-center justify-center text-white font-bold text-xs">
                      🚗
                    </div>
                    <span className="text-slate-700 dark:text-slate-300">
                      {t('driverPosition') || 'Driver'}
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <div className="w-8 h-8 bg-slate-100 dark:bg-slate-700 border-2 border-dashed border-slate-400 dark:border-slate-500 rounded"></div>
                    <span className="text-slate-700 dark:text-slate-300">
                      {t('aisle') || 'Aisle'}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            {/* Right Column - Seat Layout Preview */}
            <div className="space-y-4">
              <div className="bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg p-4">
                <h4 className="font-semibold text-slate-900 dark:text-slate-100 mb-4 flex items-center gap-2">
                  <LayoutGrid className="w-4 h-4" />
                  {t('seatLayoutPreview') || 'Seat Layout Preview'}
                </h4>
                
                {seatLayout.length === 0 ? (
                  <div className="text-center py-12 text-slate-500 dark:text-slate-400">
                    <Armchair className="w-12 h-12 mx-auto mb-3 opacity-30" />
                    <p className="text-sm">
                      {t('enterRowsColumns') || 'Enter rows and columns to preview'}
                    </p>
                  </div>
                ) : (
                  <div className="overflow-x-auto pb-6">
                    {/* Driver Row */}
                    {formData.driverColumn && (
                      <div className="mb-4 flex gap-2 justify-center">
                        {Array.from({ length: parseInt(formData.columns) || 0 }).map((_, colIndex) => {
                          const isDriver = parseInt(formData.driverColumn) === colIndex + 1;
                          const isAisle = isAisleColumn(colIndex);
                          
                          return (
                            <div key={`driver-${colIndex}`}>
                              {isDriver ? (
                                <div className="w-10 h-10 bg-slate-800 dark:bg-slate-700 rounded flex items-center justify-center text-white font-bold text-lg">
                                  🚗
                                </div>
                              ) : isAisle ? (
                                <div className="w-10 h-10 bg-slate-100 dark:bg-slate-700 border-2 border-dashed border-slate-400 dark:border-slate-500 rounded"></div>
                              ) : (
                                <div className="w-10 h-10"></div>
                              )}
                            </div>
                          );
                        })}
                      </div>
                    )}

                    {/* Customer Seats Grid */}
                    <div className="flex flex-col gap-2 items-center">
                      {seatLayout.map((row, rowIndex) => (
                        <div key={rowIndex} className="flex gap-2">
                          {row.map((seat, colIndex) => {
                            const isAisle = isAisleColumn(colIndex);
                            
                            return (
                              <div
                                key={`${rowIndex}-${colIndex}`}
                                className="relative group"
                                onClick={() => !isAisle && toggleSeatAvailability(rowIndex, colIndex)}
                              >
                                {isAisle ? (
                                  <div className="w-10 h-10 bg-slate-100 dark:bg-slate-700 border-2 border-dashed border-slate-400 dark:border-slate-500 rounded"></div>
                                ) : (
                                  <div
                                    className={`w-10 h-10 rounded flex items-center justify-center transition-all ${
                                      seat.isAvailable
                                        ? 'bg-green-500 hover:bg-green-600 text-white cursor-pointer'
                                        : 'bg-slate-300 dark:bg-slate-600 hover:bg-slate-400 dark:hover:bg-slate-500 cursor-pointer'
                                    }`}
                                  >
                                    {seat.isAvailable ? (
                                      <Armchair className="w-5 h-5" />
                                    ) : (
                                      <X className="w-5 h-5 text-slate-600 dark:text-slate-300" />
                                    )}
                                  </div>
                                )}
                                {!isAisle && (
                                  <div className="absolute -bottom-5 left-1/2 -translate-x-1/2 text-[10px] font-mono text-slate-600 dark:text-slate-400 opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap">
                                    {seat.seatNumber}
                                  </div>
                                )}
                              </div>
                            );
                          })}
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>

          <DialogFooter className="gap-2">
            <Button
              type="button"
              variant="secondary"
              onClick={handleClose}
              disabled={loading}
            >
              {t('cancel') || 'Cancel'}
            </Button>
            <Button
              type="submit"
              variant="primary"
              disabled={loading || seatLayout.length === 0}
              className="flex items-center gap-2"
            >
              {loading && <Loader2 className="w-4 h-4 animate-spin" />}
              {loading ? (t('creating') || 'Creating...') : (t('create') || 'Create')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default CreateLayoutDialog;
