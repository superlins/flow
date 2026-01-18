import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { workflowApi } from '../api/workflow';
import type { Workflow, UpdateWorkflowRequest } from '../types/workflow';
import { X } from 'lucide-react';

interface EditWorkflowModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  workflow: Workflow;
}

export function EditWorkflowModal({ isOpen, onClose, onSuccess, workflow }: EditWorkflowModalProps) {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState<UpdateWorkflowRequest>({
    name: '',
    description: '',
    inputSchema: '{}',
    outputSchema: '{}',
  });

  // Reset form data when workflow changes or modal opens
  useEffect(() => {
    if (workflow) {
      setFormData({
        name: workflow.name,
        description: workflow.description,
        inputSchema: workflow.inputSchema,
        outputSchema: workflow.outputSchema,
      });
    }
  }, [workflow]);

  const updateMutation = useMutation({
    mutationFn: (data: UpdateWorkflowRequest) =>
      workflowApi.update(workflow.key, workflow.version, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
      onSuccess();
      onClose();
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateMutation.mutate(formData);
  };

  if (!isOpen || !workflow) return null;

  return (
    <div className="modal modal-open">
      <div className="modal-box max-w-2xl">
        <div className="flex justify-between items-center mb-4">
          <h3 className="font-bold text-lg">Edit Workflow</h3>
          <button onClick={onClose} className="btn btn-sm btn-circle btn-ghost">
            <X size={16} />
          </button>
        </div>

        <div className="mb-4 p-3 bg-base-200 rounded-lg">
          <div className="text-sm space-y-1">
            <div><span className="font-medium">Key:</span> {workflow.key}</div>
            <div><span className="font-medium">Version:</span> {workflow.version}</div>
            <div><span className="font-medium">Status:</span> {workflow.status}</div>
          </div>
          {workflow.status !== 'DRAFT' && workflow.status !== 'DISABLED' && (
            <div className="mt-2 text-sm text-warning">
              ⚠️ Only DRAFT or DISABLED workflows can be updated
            </div>
          )}
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="form-control">
            <label className="label">
              <span className="label-text">Name *</span>
            </label>
            <input
              type="text"
              className="input input-bordered w-full"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="e.g., User Processing Workflow"
              required
              disabled={workflow.status !== 'DRAFT' && workflow.status !== 'DISABLED'}
            />
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Description</span>
            </label>
            <textarea
              className="textarea textarea-bordered w-full"
              value={formData.description || ''}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Describe your workflow..."
              rows={2}
              disabled={workflow.status !== 'DRAFT' && workflow.status !== 'DISABLED'}
            />
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Input Schema (JSON)</span>
            </label>
            <textarea
              className="textarea textarea-bordered w-full font-mono text-sm"
              value={formData.inputSchema || '{}'}
              onChange={(e) => setFormData({ ...formData, inputSchema: e.target.value })}
              rows={4}
              disabled={workflow.status !== 'DRAFT' && workflow.status !== 'DISABLED'}
            />
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Output Schema (JSON)</span>
            </label>
            <textarea
              className="textarea textarea-bordered w-full font-mono text-sm"
              value={formData.outputSchema || '{}'}
              onChange={(e) => setFormData({ ...formData, outputSchema: e.target.value })}
              rows={4}
              disabled={workflow.status !== 'DRAFT' && workflow.status !== 'DISABLED'}
            />
          </div>

          <div className="modal-action">
            <button type="button" onClick={onClose} className="btn">
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={updateMutation.isPending || (workflow.status !== 'DRAFT' && workflow.status !== 'DISABLED')}
            >
              {updateMutation.isPending ? <span className="loading loading-spinner"></span> : 'Update Workflow'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
