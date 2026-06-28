import React from 'react';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from 'shared/components/ui/select';
import { Button } from 'shared/components/ui/button';
import { ChevronLeft, ChevronRight } from 'lucide-react';

export const Pagination = ({
  currentPage,
  totalPages,
  pageSize,
  totalElements,
  onPageChange,
  onPageSizeChange
}) => {
  const pageSizeOptions = [15, 30, 50].filter((size, i, arr) =>
    i === 0 || totalElements > arr[i - 1]
  );

  const handlePrevious = () => {
    if (currentPage > 0) {
      onPageChange(currentPage - 1);
    }
  };

  const handleNext = () => {
    if (currentPage < totalPages - 1) {
      onPageChange(currentPage + 1);
    }
  };

  const startItem = totalElements === 0 ? 0 : currentPage * pageSize + 1;
  const endItem = Math.min((currentPage + 1) * pageSize, totalElements);

  return (
    <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3 p-3 sm:p-4 border-t bg-card rounded-b-lg">
      {/* Entry count */}
      <div className="text-xs sm:text-sm text-muted-foreground text-center sm:text-left">
        Showing {startItem}–{endItem} of {totalElements}
      </div>

      <div className="flex flex-col xs:flex-row items-center gap-3 sm:gap-6">
        {/* Page size selector */}
        <div className="flex items-center gap-2 text-xs sm:text-sm">
          <span className="text-muted-foreground whitespace-nowrap">Show</span>
          <Select value={String(pageSize)} onValueChange={(val) => onPageSizeChange(Number(val))}>
            <SelectTrigger className="w-[60px] sm:w-[70px] h-8">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {pageSizeOptions.map(size => (
                <SelectItem key={size} value={String(size)}>{size}</SelectItem>
              ))}
            </SelectContent>
          </Select>
          <span className="text-muted-foreground whitespace-nowrap">per page</span>
        </div>

        {/* Prev / page label / Next */}
        <div className="flex items-center gap-2 sm:gap-3">
          <Button
            variant="outline"
            size="icon"
            className="h-8 w-8"
            onClick={handlePrevious}
            disabled={currentPage <= 0}
          >
            <ChevronLeft className="h-4 w-4" />
          </Button>

          <span className="text-sm min-w-[100px] text-center">
            Page {currentPage + 1} of {totalPages}
          </span>

          <Button
            variant="outline"
            size="icon"
            className="h-8 w-8"
            onClick={handleNext}
            disabled={currentPage >= totalPages - 1}
          >
            <ChevronRight className="h-4 w-4" />
          </Button>
        </div>
      </div>
    </div>
  );
};

export default Pagination;
