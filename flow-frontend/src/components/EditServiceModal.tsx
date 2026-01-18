import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { serviceApi } from '../api/service';
import type { ApiService, UpdateServiceRequest } from '../types/service';
import { X } from 'lucide-react';

interface EditServiceModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  service: ApiService;
}

export function EditServiceModal({ isOpen, onClose, onSuccess, service }: EditServiceModalProps) {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState<UpdateServiceRequest>({
    name: '',
    description: '',
    inputSchema: '{}',
    outputSchema: '{}',
  });

  // Reset form data when service changes or modal opens
  useEffect(() => {
    if (service) {
      setFormData({
        name: service.name,
        description: service.description,
        inputSchema: service.inputSchema,
        outputSchema: service.outputSchema,
      });
    }
  }, [service]);

  const updateMutation = useMutation({
    mutationFn: (data: UpdateServiceRequest) => serviceApi.update(service.key, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['services'] });
      onSuccess();
      onClose();
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateMutation.mutate(formData);
  };

  if (!isOpen || !service) return null;

  return (
    <div className="modal modal-open">
      <div className="modal-box max-w-2xl">
        <div className="flex justify-between items-center mb-4">
          <h3 className="font-bold text-lg">Edit Service</h3>
          <button onClick={onClose} className="btn btn-sm btn-circle btn-ghost">
            <X size={16} />
          </button>
        </div>

        <div className="mb-4 p-3 bg-base-200 rounded-lg">
          <div className="text-sm space-y-1">
            <div><span className="font-medium">Key:</span> {service.key}</div>
            <div><span className="font-medium">Mode:</span> {service.mode}</div>
            {service.mode === 'DATASOURCE' && (
              <div><span className="font-medium">Datasource:</span> {service.datasourceId}:{service.datasourceVersion}</div>
            )}
            {service.mode === 'WORKFLOW' && (
              <div><span className="font-medium">Workflow:</span> {service.workflowId}:{service.workflowVersion}</div>
            )}
          </div>
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
              placeholder="e.g., User Service"
              required
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
              placeholder="Describe your service..."
              rows={2}
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
            />
          </div>

          <div className="modal-action">
            <button type="button" onClick={onClose} className="btn">
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={updateMutation.isPending}>
              {updateMutation.isPending ? <span className="loading loading-spinner"></span> : 'Update Service'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
