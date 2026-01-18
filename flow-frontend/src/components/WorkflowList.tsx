import { useState } from 'react';
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query';
import { workflowApi } from '../api/workflow';
import { ConfirmDialog } from './ConfirmDialog';
import { EditWorkflowModal } from './EditWorkflowModal';
import { ViewWorkflowModal } from './ViewWorkflowModal';
import type { Workflow } from '../types/workflow';
import { Play, Archive, Power, PowerOff, Edit2, Trash2, Eye } from 'lucide-react';

interface WorkflowListProps {
  onExecute: (workflow: Workflow) => void;
}

export function WorkflowList({ onExecute }: WorkflowListProps) {
  const queryClient = useQueryClient();
  const [selectedWorkflow, setSelectedWorkflow] = useState<Workflow | null>(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);
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

  if (isLoading) return <div className="flex justify-center py-8"><span className="loading loading-spinner loading-lg"></span></div>;
  if (error) return <div className="alert alert-error"><span>Error loading workflows: {error.message}</span></div>;
  if (!data || data.workflows.length === 0) {
    return (
      <div className="alert">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" className="stroke-info shrink-0 w-6 h-6"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        <span>No workflows found. Create your first workflow to get started!</span>
      </div>
    );
  }

  const handleStatusChange = async (key: string, version: number, action: 'enable' | 'disable' | 'archive') => {
    try {
      await workflowApi[action](key, version);
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
    } catch (error) {
      console.error(`Failed to ${action} workflow:`, error);
    }
  };

  const handleEdit = (workflow: Workflow) => {
    setSelectedWorkflow(workflow);
    setIsEditModalOpen(true);
  };

  const handleDelete = (workflow: Workflow) => {
    setWorkflowToDelete(workflow);
    setIsDeleteDialogOpen(true);
  };

  const handleView = (workflow: Workflow) => {
    setSelectedWorkflow(workflow);
    setIsViewModalOpen(true);
  };

  const getStatusBadge = (status: string) => {
    const config = {
      'ENABLED': { class: 'badge-success', label: 'Enabled' },
      'DISABLED': { class: 'badge-warning', label: 'Disabled' },
      'ARCHIVED': { class: 'badge-ghost', label: 'Archived' },
      'DRAFT': { class: 'badge-info', label: 'Draft' },
    };
    return config[status as keyof typeof config] || { class: 'badge-ghost', label: status };
  };

  return (
    <>
      <div className="space-y-4">
        {data.workflows.map((workflow) => {
        const statusBadge = getStatusBadge(workflow.status);
        return (
          <div key={workflow.id} className="card bg-base-100 shadow-xl hover:shadow-2xl transition-shadow">
            <div className="card-body">
              <div className="flex flex-col md:flex-row justify-between gap-4">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <h3 className="card-title">{workflow.name}</h3>
                    <div className={`badge ${statusBadge.class} badge-outline`}>
                      {statusBadge.label}
                    </div>
                  </div>
                  <p className="text-sm text-base-content/70 mb-3">{workflow.description || 'No description'}</p>
                  <div className="text-xs text-base-content/60 space-y-1">
                    <div><span className="font-medium">Key:</span> {workflow.key}</div>
                    <div><span className="font-medium">Version:</span> {workflow.version}</div>
                    <div><span className="font-medium">Created:</span> {new Date(workflow.createdAt).toLocaleString()}</div>
                  </div>
                </div>

                <div className="flex flex-row md:flex-col gap-2">
                  <button
                    onClick={() => handleView(workflow)}
                    className="btn btn-info gap-2 btn-sm"
                  >
                    <Eye size={16} />
                    View
                  </button>
                  <button
                    onClick={() => handleEdit(workflow)}
                    className="btn btn-primary gap-2 btn-sm"
                  >
                    <Edit2 size={16} />
                    Edit
                  </button>
                  {workflow.status === 'ENABLED' && (
                    <button
                      onClick={() => onExecute(workflow)}
                      className="btn btn-success gap-2 btn-sm"
                    >
                      <Play size={16} />
                      Execute
                    </button>
                  )}
                  {workflow.status === 'DISABLED' && (
                    <button
                      onClick={() => handleStatusChange(workflow.key, workflow.version, 'enable')}
                      className="btn btn-success gap-2 btn-sm"
                    >
                      <Power size={16} />
                      Enable
                    </button>
                  )}
                  {workflow.status === 'ENABLED' && (
                    <button
                      onClick={() => handleStatusChange(workflow.key, workflow.version, 'disable')}
                      className="btn btn-warning gap-2 btn-sm"
                    >
                      <PowerOff size={16} />
                      Disable
                    </button>
                  )}
                  {(workflow.status === 'ENABLED' || workflow.status === 'DISABLED') && (
                    <button
                      onClick={() => handleStatusChange(workflow.key, workflow.version, 'archive')}
                      className="btn btn-neutral gap-2 btn-sm"
                    >
                      <Archive size={16} />
                      Archive
                    </button>
                  )}
                  {workflow.status === 'ARCHIVED' && (
                    <button
                      onClick={() => handleDelete(workflow)}
                      className="btn btn-error gap-2 btn-sm"
                    >
                      <Trash2 size={16} />
                      Delete
                    </button>
                  )}
                </div>
              </div>
            </div>
          </div>
        );
      })}
    </div>

    <ViewWorkflowModal
      isOpen={isViewModalOpen}
      onClose={() => {
        setIsViewModalOpen(false);
        setSelectedWorkflow(null);
      }}
      workflow={selectedWorkflow}
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
      message={`Are you sure you want to delete workflow "${workflowToDelete?.name}"? Only archived workflows can be deleted. This action cannot be undone.`}
      confirmText="Delete"
      cancelText="Cancel"
      variant="danger"
      onConfirm={() => {
        if (workflowToDelete) {
          deleteMutation.mutate(workflowToDelete);
        }
      }}
      onCancel={() => {
        setIsDeleteDialogOpen(false);
        setWorkflowToDelete(null);
      }}
    />
  </>
  );
}
