import React from 'react';
import { Card, CardContent } from 'shared/components/ui/card';
import { cn } from 'lib/utils';

const StatCard = ({ title, value, icon, change }) => {
  const isPositive = change.startsWith('+');

  return (
    <Card className="transition-transform hover:-translate-y-1 hover:shadow-lg">
      <CardContent className="flex items-center gap-5 p-6">
        <div className="flex items-center justify-center w-[70px] h-[70px] rounded-xl bg-gradient-to-br from-primary to-primary/80 text-white text-4xl">
          {icon}
        </div>
        <div className="flex-1">
          <h3 className="text-sm font-medium text-muted-foreground mb-2">{title}</h3>
          <div className="text-3xl font-bold text-foreground mb-1">{value}</div>
          <span className={cn(
            "text-sm font-semibold",
            isPositive ? "text-green-600" : "text-red-600"
          )}>
            {change}
          </span>
        </div>
      </CardContent>
    </Card>
  );
};

export default StatCard;
