import { useState } from 'react';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { WorkflowList } from './components/WorkflowList';
import { CreateWorkflowModal } from './components/CreateWorkflowModal';
import { ExecuteWorkflowModal } from './components/ExecuteWorkflowModal';
import type { Workflow } from './types/workflow';
import { Plus, GitBranch } from 'lucide-react';

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
    },
  },
});

function App() {
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [selectedWorkflow, setSelectedWorkflow] = useState<Workflow | undefined>(undefined);
  const [isExecuteModalOpen, setIsExecuteModalOpen] = useState(false);

  const handleExecute = (workflow: Workflow) => {
    setSelectedWorkflow(workflow);
    setIsExecuteModalOpen(true);
  };

  const handleCreateSuccess = () => {
    queryClient.invalidateQueries({ queryKey: ['workflows'] });
  };

  return (
    <QueryClientProvider client={queryClient}>
      <div className="min-h-screen bg-base-200">
        <div className="navbar bg-base-100 shadow-lg">
          <div className="flex-1">
            <a className="btn btn-ghost normal-case text-xl gap-2">
              <GitBranch className="text-primary" size={24} />
              Flow Workflow Manager
            </a>
          </div>
          <div className="flex-none">
            <button
              onClick={() => setIsCreateModalOpen(true)}
              className="btn btn-primary gap-2"
            >
              <Plus size={20} />
              Create Workflow
            </button>
          </div>
        </div>

        <div className="container mx-auto p-8">
          <WorkflowList onExecute={handleExecute} />
        </div>

        <CreateWorkflowModal
          isOpen={isCreateModalOpen}
          onClose={() => setIsCreateModalOpen(false)}
          onSuccess={handleCreateSuccess}
        />

        <ExecuteWorkflowModal
          isOpen={isExecuteModalOpen}
          onClose={() => setIsExecuteModalOpen(false)}
          workflow={selectedWorkflow}
        />
      </div>
    </QueryClientProvider>
  );
}

export default App;
