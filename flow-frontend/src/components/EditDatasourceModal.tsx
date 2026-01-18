import { useState, useEffect } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { datasourceApi } from '../api/datasource';
import type { ApiDatasource, UpdateDatasourceRequest } from '../types/datasource';
import { X } from 'lucide-react';

interface EditDatasourceModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  datasource: ApiDatasource;
}

export function EditDatasourceModal({ isOpen, onClose, onSuccess, datasource }: EditDatasourceModalProps) {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState<UpdateDatasourceRequest>({
    name: '',
    description: '',
    inputSchema: '{}',
    outputSchema: '{}',
    strict: false,
    connection: '',
  });

  // Reset form data when datasource changes or modal opens
  useEffect(() => {
    if (datasource) {
      setFormData({
        name: datasource.name,
        description: datasource.description,
        inputSchema: datasource.inputSchema,
        outputSchema: datasource.outputSchema,
        strict: datasource.strict,
        connection: datasource.connection,
      });
    }
  }, [datasource]);

  const updateMutation = useMutation({
    mutationFn: (data: UpdateDatasourceRequest) =>
      datasourceApi.update(datasource.key, datasource.version, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['datasources'] });
      onSuccess();
      onClose();
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    updateMutation.mutate(formData);
  };

  if (!isOpen || !datasource) return null;

  return (
    <div className="modal modal-open">
      <div className="modal-box max-w-2xl">
        <div className="flex justify-between items-center mb-4">
          <h3 className="font-bold text-lg">Edit Datasource</h3>
          <button onClick={onClose} className="btn btn-sm btn-circle btn-ghost">
            <X size={16} />
          </button>
        </div>

        <div className="mb-4 p-3 bg-base-200 rounded-lg">
          <div className="text-sm space-y-1">
            <div><span className="font-medium">Key:</span> {datasource.key}</div>
            <div><span className="font-medium">Version:</span> {datasource.version}</div>
            <div><span className="font-medium">Type:</span> {datasource.type}</div>
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
              placeholder="e.g., User Service Datasource"
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
              placeholder="Describe your datasource..."
              rows={2}
            />
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text">Connection URL</span>
            </label>
            <input
              type="text"
              className="input input-bordered w-full"
              value={formData.connection}
              onChange={(e) => setFormData({ ...formData, connection: e.target.value })}
              placeholder="e.g., http://localhost:8080/api/users"
            />
          </div>

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

          <div className="form-control">
            <label className="label cursor-pointer">
              <span className="label-text">Strict Validation</span>
              <input
                type="checkbox"
                className="toggle toggle-primary"
                checked={formData.strict}
                onChange={(e) => setFormData({ ...formData, strict: e.target.checked })}
              />
            </label>
          </div>

          <div className="modal-action">
            <button type="button" onClick={onClose} className="btn">
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={updateMutation.isPending}>
              {updateMutation.isPending ? <span className="loading loading-spinner"></span> : 'Update Datasource'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
