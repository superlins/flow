import { useState } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { WorkflowList } from './components/WorkflowList';
import { CreateWorkflowModal } from './components/CreateWorkflowModal';
import { ExecuteWorkflowModal } from './components/ExecuteWorkflowModal';
import { DatasourceList } from './components/DatasourceList';
import { CreateDatasourceModal } from './components/CreateDatasourceModal';
import { ServiceList } from './components/ServiceList';
import { CreateServiceModal } from './components/CreateServiceModal';
import { Sidebar, type View } from './components/Sidebar';
import { PluginList } from './components/PluginList';
import type { Workflow } from './types/workflow';
import { Plus, Menu } from 'lucide-react';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

function App() {
  const [currentView, setCurrentView] = useState<View>('workflows');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  // Workflow modals
  const [isCreateWorkflowModalOpen, setIsCreateWorkflowModalOpen] = useState(false);
  const [selectedWorkflow, setSelectedWorkflow] = useState<Workflow | undefined>(undefined);
  const [isExecuteModalOpen, setIsExecuteModalOpen] = useState(false);

  // Datasource modal
  const [isCreateDatasourceModalOpen, setIsCreateDatasourceModalOpen] = useState(false);

  // Service modal
  const [isCreateServiceModalOpen, setIsCreateServiceModalOpen] = useState(false);

  const handleExecute = (workflow: Workflow) => {
    setSelectedWorkflow(workflow);
    setIsExecuteModalOpen(true);
  };

  const handleCreateSuccess = () => {
    queryClient.invalidateQueries({ queryKey: ['workflows', 'datasources', 'services'] });
  };

  const renderContent = () => {
    switch (currentView) {
      case 'workflows':
        return <WorkflowList onExecute={handleExecute} />;
      case 'datasources':
        return <DatasourceList />;
      case 'services':
        return <ServiceList />;
      case 'plugins':
        return <PluginList />;
      default:
        return <WorkflowList onExecute={handleExecute} />;
    }
  };

  const renderCreateButton = () => {
    switch (currentView) {
      case 'workflows':
        return (
          <button
            onClick={() => setIsCreateWorkflowModalOpen(true)}
            className="btn btn-primary gap-2"
          >
            <Plus size={20} />
            Create Workflow
          </button>
        );
      case 'datasources':
        return (
          <button
            onClick={() => setIsCreateDatasourceModalOpen(true)}
            className="btn btn-primary gap-2"
          >
            <Plus size={20} />
            Create Datasource
          </button>
        );
      case 'services':
        return (
          <button
            onClick={() => setIsCreateServiceModalOpen(true)}
            className="btn btn-primary gap-2"
          >
            <Plus size={20} />
            Create Service
          </button>
        );
      default:
        return null;
    }
  };

  return (
    <QueryClientProvider client={queryClient}>
      <div className="flex min-h-screen bg-base-200">
        <Sidebar
          currentView={currentView}
          setCurrentView={setCurrentView}
          isOpen={isSidebarOpen}
          onToggle={() => setIsSidebarOpen(!isSidebarOpen)}
        />

        {/* Main content area */}
        <div className="flex-1 flex flex-col lg:ml-0">
          {/* Top bar */}
          <div className="navbar bg-base-100 shadow-sm">
            <div className="flex-1 flex items-center gap-4">
              <button
                onClick={() => setIsSidebarOpen(!isSidebarOpen)}
                className="lg:hidden btn btn-ghost btn-circle"
              >
                <Menu size={20} />
              </button>
              <h1 className="text-2xl font-semibold capitalize">
                {currentView}
              </h1>
            </div>
            <div className="flex-none">
              {renderCreateButton()}
            </div>
          </div>

          {/* Content */}
          <div className="flex-1 p-4 lg:p-8 overflow-auto">
            {renderContent()}
          </div>
        </div>

        <CreateWorkflowModal
          isOpen={isCreateWorkflowModalOpen}
          onClose={() => setIsCreateWorkflowModalOpen(false)}
          onSuccess={handleCreateSuccess}
        />

        <ExecuteWorkflowModal
          isOpen={isExecuteModalOpen}
          onClose={() => setIsExecuteModalOpen(false)}
          workflow={selectedWorkflow}
        />

        <CreateDatasourceModal
          isOpen={isCreateDatasourceModalOpen}
          onClose={() => setIsCreateDatasourceModalOpen(false)}
          onSuccess={handleCreateSuccess}
        />

        <CreateServiceModal
          isOpen={isCreateServiceModalOpen}
          onClose={() => setIsCreateServiceModalOpen(false)}
          onSuccess={handleCreateSuccess}
        />
      </div>
    </QueryClientProvider>
  );
}

export default App;
