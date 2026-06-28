import React, { useState } from 'react';
import { FileText, TrendingUp, Users, Bus } from 'lucide-react';
import { Tabs, TabsContent, TabsList, TabsTrigger } from 'shared/components/ui/tabs';
import { useLocale } from 'shared/context/LocaleContext';
import { translations } from 'shared/locales/translations';
import FinancialReportsTab from '../../components/FinancialReportsTab/FinancialReportsTab';
import OperationalReportsTab from '../../components/OperationalReportsTab/OperationalReportsTab';
import CustomerReportsTab from '../../components/CustomerReportsTab/CustomerReportsTab';
import BusReportsTab from '../../components/BusReportsTab/BusReportsTab';

const ReportsPage = () => {
  const { locale } = useLocale();
  const t = (key) => translations[locale]?.[key] || translations.en[key] || key;
  const [activeTab, setActiveTab] = useState('financial');

  return (
    <div className="flex-1 p-6 md:p-8 overflow-y-auto bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-50 dark:from-slate-950 dark:via-slate-900 dark:to-slate-900 min-h-screen">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-slate-900 dark:text-slate-100 mb-2">
          {t('reportsAnalytics')}
        </h1>
        <p className="text-slate-600 dark:text-slate-400">
          Generate comprehensive reports for financial, operational, customer, and bus analytics
        </p>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab} className="w-full">
        <TabsList className="grid w-full grid-cols-4 mb-8">
          <TabsTrigger value="financial" className="flex items-center gap-2">
            <FileText className="w-4 h-4" />
            Financial
          </TabsTrigger>
          <TabsTrigger value="operational" className="flex items-center gap-2">
            <TrendingUp className="w-4 h-4" />
            Operational
          </TabsTrigger>
          <TabsTrigger value="customer" className="flex items-center gap-2">
            <Users className="w-4 h-4" />
            Customer
          </TabsTrigger>
          <TabsTrigger value="bus" className="flex items-center gap-2">
            <Bus className="w-4 h-4" />
            Bus & Route
          </TabsTrigger>
        </TabsList>

        <TabsContent value="financial">
          <FinancialReportsTab />
        </TabsContent>

        <TabsContent value="operational">
          <OperationalReportsTab />
        </TabsContent>

        <TabsContent value="customer">
          <CustomerReportsTab />
        </TabsContent>

        <TabsContent value="bus">
          <BusReportsTab />
        </TabsContent>
      </Tabs>
    </div>
  );
};

export default ReportsPage;
