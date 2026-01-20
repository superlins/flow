import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { DashboardLayout } from './layouts/DashboardLayout';
import { DashboardPage } from './pages/DashboardPage';
import { WorkflowDetailPage } from './pages/WorkflowDetailPage';
import { WorkflowList } from './components/WorkflowList'; // Legacy, will use as page for now
import { DatasourceList } from './components/DatasourceList'; // Legacy
import { ServiceList } from './components/ServiceList'; // Legacy
import { PluginList } from './components/PluginList'; // Legacy
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

/* 
   Wrapper components to adapt legacy list components to page structure if needed, 
   or just use them directly if they are self-contained. 
   Currently Sidebar handled view switching in old App.tsx. 
   Now Layout handles Sidebar, so we just need the Content part of those components.
   Looking at old App.tsx, they were just <WorkflowList /> etc.
   We can use them directly as elements.
*/

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<DashboardLayout />}>
            <Route index element={<DashboardPage />} />

            <Route path="workflows" element={<WorkflowList onExecute={() => { }} />} />
            {/* Note: onExecute prop in WorkflowList might need adjustment to navigate instead of opening modal directly, 
                or we pass a handler that opens the modal which logic might need to move up or stay in the list.
                For now, let's keep WorkflowList as is but we might need to patch it to handle navigation to detail.
             */}
            <Route path="workflows/:key/:version" element={<WorkflowDetailPage />} />

            <Route path="datasources" element={<DatasourceList />} />
            <Route path="services" element={<ServiceList />} />
            <Route path="plugins" element={<PluginList />} />

            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
