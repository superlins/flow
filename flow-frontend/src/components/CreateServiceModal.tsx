import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { serviceApi } from '../api/service';
import type { CreateServiceRequest } from '../types/service';
import { X } from 'lucide-react';

interface CreateServiceModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export function CreateServiceModal({ isOpen, onClose, onSuccess }: CreateServiceModalProps) {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState<CreateServiceRequest>({
    key: '',
    name: '',
    description: '',
    mode: 'DATASOURCE',
    datasourceKey: '',
    datasourceVersion: 1,
    workflowKey: '',
    workflowVersion: 1,
    inputSchema: '{}',
    outputSchema: '{}',
  });

  const createMutation = useMutation({
    mutationFn: (data: CreateServiceRequest) => serviceApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['services'] });
      onSuccess();
      onClose();
      setFormData({
        key: '',
        name: '',
        description: '',
        mode: 'DATASOURCE',
        datasourceKey: '',
        datasourceVersion: 1,
        workflowKey: '',
        workflowVersion: 1,
        inputSchema: '{}',
        outputSchema: '{}',
      });
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.key || !formData.name) return;

    // 验证必需字段
    if (formData.mode === 'DATASOURCE' && !formData.datasourceKey) {
      alert('Datasource key is required for Datasource mode');
      return;
    }
    if (formData.mode === 'WORKFLOW' && !formData.workflowKey) {
      alert('Workflow key is required for Workflow mode');
      return;
    }

    createMutation.mutate(formData);
  };

  if (!isOpen) return null;

  return (
    <div className={`modal modal-open ${isOpen ? '' : ''}`}>
      <div className="modal-box max-w-2xl">
        <div className="flex justify-between items-center mb-4">
          <h3 className="font-bold text-lg">Create New Service</h3>
          <button onClick={onClose} className="btn btn-sm btn-circle btn-ghost">
            <X size={16} />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="form-control">
            <label className="label">
              <span className="label-text">Key *</span>
            </label>
            <input
              type="text"
              className="input input-bordered w-full"
              value={formData.key}
              onChange={(e) => setFormData({ ...formData, key: e.target.value })}
              placeholder="e.g., get-user"
              required
            />
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Name *</span>
            </label>
            <input
              type="text"
              className="input input-bordered w-full"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="e.g., Get User Service"
              required
            />
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Description</span>
            </label>
            <textarea
              className="textarea textarea-bordered w-full"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              placeholder="Describe your service..."
              rows={2}
            />
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Mode</span>
            </label>
            <select
              className="select select-bordered w-full"
              value={formData.mode}
              onChange={(e) => setFormData({ ...formData, mode: e.target.value as any })}
            >
              <option value="DATASOURCE">Datasource</option>
              <option value="WORKFLOW">Workflow</option>
            </select>
          </div>

          {formData.mode === 'DATASOURCE' && (
            <div className="grid grid-cols-2 gap-4">
              <div className="form-control">
                <label className="label">
                  <span className="label-text">Datasource Key *</span>
                </label>
                <input
                  type="text"
                  className="input input-bordered w-full"
                  value={formData.datasourceKey}
                  onChange={(e) => setFormData({ ...formData, datasourceKey: e.target.value })}
                  placeholder="e.g., user-datasource"
                  required
                />
              </div>

              <div className="form-control">
                <label className="label">
                  <span className="label-text">Version</span>
                </label>
                <input
                  type="number"
                  className="input input-bordered w-full"
                  value={formData.datasourceVersion}
                  onChange={(e) => setFormData({ ...formData, datasourceVersion: parseInt(e.target.value) || 1 })}
                  min={1}
                />
              </div>
            </div>
          )}

          {formData.mode === 'WORKFLOW' && (
            <div className="grid grid-cols-2 gap-4">
              <div className="form-control">
                <label className="label">
                  <span className="label-text">Workflow Key *</span>
                </label>
                <input
                  type="text"
                  className="input input-bordered w-full"
                  value={formData.workflowKey}
                  onChange={(e) => setFormData({ ...formData, workflowKey: e.target.value })}
                  placeholder="e.g., get-user-workflow"
                  required
                />
              </div>

              <div className="form-control">
                <label className="label">
                  <span className="label-text">Version</span>
                </label>
                <input
                  type="number"
                  className="input input-bordered w-full"
                  value={formData.workflowVersion}
                  onChange={(e) => setFormData({ ...formData, workflowVersion: parseInt(e.target.value) || 1 })}
                  min={1}
                />
              </div>
            </div>
          )}

          <div className="form-control">
            <label className="label">
              <span className="label-text">Input Schema (JSON)</span>
            </label>
            <textarea
              className="textarea textarea-bordered w-full font-mono text-sm"
              value={formData.inputSchema}
              onChange={(e) => setFormData({ ...formData, inputSchema: e.target.value })}
              rows={4}
            />
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Output Schema (JSON)</span>
            </label>
            <textarea
              className="textarea textarea-bordered w-full font-mono text-sm"
              value={formData.outputSchema}
              onChange={(e) => setFormData({ ...formData, outputSchema: e.target.value })}
              rows={4}
            />
          </div>

          <div className="modal-action">
            <button type="button" onClick={onClose} className="btn">
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={createMutation.isPending}>
              {createMutation.isPending ? <span className="loading loading-spinner"></span> : 'Create Service'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
