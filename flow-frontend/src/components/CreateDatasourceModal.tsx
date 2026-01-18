import { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { datasourceApi } from '../api/datasource';
import type { CreateDatasourceRequest } from '../types/datasource';
import { X } from 'lucide-react';

interface CreateDatasourceModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export function CreateDatasourceModal({ isOpen, onClose, onSuccess }: CreateDatasourceModalProps) {
  const queryClient = useQueryClient();
  const [formData, setFormData] = useState<CreateDatasourceRequest>({
    key: '',
    name: '',
    description: '',
    type: 'HTTP',
    version: 1,
    inputSchema: '{}',
    outputSchema: '{}',
    strict: false,
    connection: '',
    operation: 'default operation',
  });

  const createMutation = useMutation({
    mutationFn: (data: CreateDatasourceRequest) => datasourceApi.create(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['datasources'] });
      onSuccess();
      onClose();
      setFormData({
        key: '',
        name: '',
        description: '',
        type: 'HTTP',
        version: 1,
        inputSchema: '{}',
        outputSchema: '{}',
        strict: false,
        connection: '',
        operation: 'default operation',
      });
    },
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!formData.key || !formData.name) return;
    createMutation.mutate(formData);
  };

  if (!isOpen) return null;

  return (
    <div className={`modal modal-open ${isOpen ? '' : ''}`}>
      <div className="modal-box max-w-2xl">
        <div className="flex justify-between items-center mb-4">
          <h3 className="font-bold text-lg">Create New Datasource</h3>
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
              placeholder="e.g., user-service"
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

          <div className="grid grid-cols-2 gap-4">
            <div className="form-control">
              <label className="label">
                <span className="label-text">Type</span>
              </label>
              <select
                className="select select-bordered w-full"
                value={formData.type}
                onChange={(e) => setFormData({ ...formData, type: e.target.value as any })}
              >
                <option value="HTTP">HTTP</option>
                <option value="R2DBC">R2DBC</option>
                <option value="CASSANDRA">Cassandra</option>
              </select>
            </div>

            <div className="form-control">
              <label className="label">
                <span className="label-text">Version</span>
              </label>
              <input
                type="number"
                className="input input-bordered w-full"
                value={formData.version}
                onChange={(e) => setFormData({ ...formData, version: parseInt(e.target.value) || 1 })}
                min={1}
              />
            </div>
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
            <button type="submit" className="btn btn-primary" disabled={createMutation.isPending}>
              {createMutation.isPending ? <span className="loading loading-spinner"></span> : 'Create Datasource'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
