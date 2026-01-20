import { useState } from 'react';
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query';
import { workflowApi } from '../api/workflow';
import { ConfirmDialog } from './ConfirmDialog';
import { EditWorkflowModal } from './EditWorkflowModal';
import { CreateWorkflowModal } from './CreateWorkflowModal';
import { ExecuteWorkflowModal } from './ExecuteWorkflowModal';
import { StatusBadge } from './common/StatusBadge';
import { DataTable, type Column } from './common/DataTable';
import type { Workflow } from '../types/workflow';
import { Play, Edit2, Trash2, Eye, Plus } from 'lucide-react';
import { useNavigate } from 'react-router-dom';

interface WorkflowListProps {
  onExecute: (workflow: Workflow) => void;
}

// @ts-ignore
export function WorkflowList({ onExecute }: WorkflowListProps) {
  const queryClient = useQueryClient();
  const navigate = useNavigate();

  const [selectedWorkflow, setSelectedWorkflow] = useState<Workflow | null>(null);

  // Modals state
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isExecuteModalOpen, setIsExecuteModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [workflowToDelete, setWorkflowToDelete] = useState<Workflow | null>(null);

  const { data, isLoading, error } = useQuery({
    queryKey: ['workflows'],
    queryFn: () => workflowApi.list(),
  });

  const deleteMutation = useMutation({
    mutationFn: (wf: Workflow) => workflowApi.delete(wf.key, wf.version),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
      setIsDeleteDialogOpen(false);
      setWorkflowToDelete(null);
    },
  });

  const handleStatusChange = async (key: string, version: number, action: 'enable' | 'disable' | 'archive') => {
    try {
      await workflowApi[action](key, version);
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
    } catch (error) {
      console.error(`Failed to ${action} workflow:`, error);
    }
  };

  const handleCreateSuccess = () => {
    queryClient.invalidateQueries({ queryKey: ['workflows'] });
    setIsCreateModalOpen(false);
  };

  const columns: Column<Workflow>[] = [
    { key: 'name', header: 'Name', className: 'font-semibold' },
    {
      key: 'key',
      header: 'Key',
      className: 'font-mono text-xs opacity-70',
      render: (wf) => <span className="font-mono">{wf.key}</span>
    },
    { key: 'version', header: 'Version', className: 'w-24 text-center' },
    {
      key: 'status',
      header: 'Status',
      className: 'w-32',
      render: (wf) => <StatusBadge status={wf.status} />
    },
    {
      key: 'actions',
      header: 'Actions',
      className: 'text-right min-w-[200px]',
      render: (workflow) => (
        <div className="join">
          <button
            className="btn btn-sm btn-ghost join-item tooltip"
            data-tip="View Details"
            onClick={(e) => { e.stopPropagation(); navigate(`/workflows/${workflow.key}/${workflow.version}`); }}
          >
            <Eye size={16} />
          </button>

          <button
            className="btn btn-sm btn-ghost join-item tooltip"
            data-tip="Edit"
            onClick={(e) => { e.stopPropagation(); setSelectedWorkflow(workflow); setIsEditModalOpen(true); }}
          >
            <Edit2 size={16} />
          </button>

          {workflow.status === 'ENABLED' && (
            <button
              className="btn btn-sm btn-ghost text-success join-item tooltip"
              data-tip="Execute"
              onClick={(e) => { e.stopPropagation(); setSelectedWorkflow(workflow); setIsExecuteModalOpen(true); }}
            >
              <Play size={16} />
            </button>
          )}

          {workflow.status === 'ARCHIVED' && (
            <button
              className="btn btn-sm btn-ghost text-error join-item tooltip"
              data-tip="Delete"
              onClick={(e) => { e.stopPropagation(); setWorkflowToDelete(workflow); setIsDeleteDialogOpen(true); }}
            >
              <Trash2 size={16} />
            </button>
          )}

          <div className="dropdown dropdown-end join-item">
            <div tabIndex={0} role="button" className="btn btn-sm btn-ghost">...</div>
            <ul tabIndex={0} className="dropdown-content z-[1] menu p-2 shadow bg-base-100 rounded-box w-52">
              {workflow.status === 'DISABLED' && (
                <li><a onClick={(e) => { e.stopPropagation(); handleStatusChange(workflow.key, workflow.version, 'enable'); }}>Enable</a></li>
              )}
              {workflow.status === 'ENABLED' && (
                <li><a onClick={(e) => { e.stopPropagation(); handleStatusChange(workflow.key, workflow.version, 'disable'); }}>Disable</a></li>
              )}
              {(workflow.status !== 'ARCHIVED') && (
                <li><a onClick={(e) => { e.stopPropagation(); handleStatusChange(workflow.key, workflow.version, 'archive'); }}>Archive</a></li>
              )}
            </ul>
          </div>
        </div>
      )
    }
  ];

  if (error) return <div className="alert alert-error"><span>Error loading workflows: {error.message}</span></div>;


  return (
    <>
      <div className="space-y-4">
        <div className="flex justify-between items-center">
          <h2 className="text-xl font-bold">All Workflows</h2>
          <button className="btn btn-primary gap-2" onClick={() => setIsCreateModalOpen(true)}>
            <Plus size={20} /> New Workflow
          </button>
        </div>

        <DataTable
          data={data?.workflows || []}
          columns={columns}
          keyField={(wf) => `${wf.key}-${wf.version}`}
          isLoading={isLoading}
          emptyMessage="No workflows found."
          onRowClick={(wf) => navigate(`/workflows/${wf.key}/${wf.version}`)}
        />
      </div>

      <CreateWorkflowModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSuccess={handleCreateSuccess}
      />

      <ExecuteWorkflowModal
        isOpen={isExecuteModalOpen}
        onClose={() => { setIsExecuteModalOpen(false); setSelectedWorkflow(null); }}
        workflow={selectedWorkflow!} // Fix typing if possible, or assume checked before open
      />

      <EditWorkflowModal
        isOpen={isEditModalOpen}
        onClose={() => {
          setIsEditModalOpen(false);
          setSelectedWorkflow(null);
        }}
        onSuccess={() => {
          queryClient.invalidateQueries({ queryKey: ['workflows'] });
          setIsEditModalOpen(false);
          setSelectedWorkflow(null);
        }}
        workflow={selectedWorkflow!}
      />

      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        title="Delete Workflow"
        message={`Are you sure you want to delete workflow "${workflowToDelete?.name}"?`}
        confirmText="Delete"
        cancelText="Cancel"
        variant="danger"
        onConfirm={() => workflowToDelete && deleteMutation.mutate(workflowToDelete)}
        onCancel={() => setIsDeleteDialogOpen(false)}
      />
    </>
  );
}
