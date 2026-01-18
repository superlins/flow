import { X } from 'lucide-react';
import type { ApiService } from '../types/service';
import { Zap, Database, GitBranch } from 'lucide-react';

interface ViewServiceModalProps {
  isOpen: boolean;
  onClose: () => void;
  service: ApiService | null;
}

export function ViewServiceModal({ isOpen, onClose, service }: ViewServiceModalProps) {
  if (!isOpen || !service) return null;

  const getModeBadge = (mode: string | null) => {
    if (!mode) return { class: 'badge-ghost', label: 'Unknown', icon: Zap };
    const config: Record<string, { class: string; label: string; icon: any }> = {
      'DATASOURCE': { class: 'badge-primary', label: 'Datasource', icon: Database },
      'WORKFLOW': { class: 'badge-secondary', label: 'Workflow', icon: GitBranch },
    };
    return config[mode] || { class: 'badge-ghost', label: mode, icon: Zap };
  };

  const modeBadge = getModeBadge(service.mode);
  const ModeIcon = modeBadge.icon || Zap;

  return (
    <div className="modal modal-open">
      <div className="modal-box max-w-3xl">
        <div className="flex justify-between items-center mb-4">
          <h3 className="font-bold text-lg">Service Details</h3>
          <button onClick={onClose} className="btn btn-sm btn-circle btn-ghost">
            <X size={16} />
          </button>
        </div>

        <div className="space-y-4">
          {/* Basic Info */}
          <div className="card bg-base-200">
            <div className="card-body py-3">
              <h4 className="font-semibold text-md mb-2">Basic Information</h4>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div><span className="font-medium">Key:</span> {service.key}</div>
                <div><span className="font-medium">Name:</span> {service.name}</div>
                <div><span className="font-medium">Status:</span> {service.status}</div>
                <div className={`badge ${modeBadge.class} badge-outline flex items-center gap-1 w-fit`}>
                  <ModeIcon size={12} />
                  {modeBadge.label}
                </div>
              </div>
              <div className="mt-2 text-sm">
                <span className="font-medium">Description:</span>
                <p className="mt-1">{service.description || 'No description provided'}</p>
              </div>
            </div>
          </div>

          {/* Target */}
          <div className="card bg-base-200">
            <div className="card-body py-3">
              <h4 className="font-semibold text-md mb-2">Target</h4>
              <div className="text-sm">
                {service.mode === 'DATASOURCE' && service.datasourceId && (
                  <div><span className="font-medium">Datasource:</span> {service.datasourceId}:{service.datasourceVersion}</div>
                )}
                {service.mode === 'WORKFLOW' && service.workflowId && (
                  <div><span className="font-medium">Workflow:</span> {service.workflowId}:{service.workflowVersion}</div>
                )}
              </div>
            </div>
          </div>

          {/* Input Schema */}
          <div className="card bg-base-200">
            <div className="card-body py-3">
              <h4 className="font-semibold text-md mb-2">Input Schema</h4>
              <pre className="bg-base-300 p-3 rounded text-xs overflow-x-auto">
                {service.inputSchema}
              </pre>
            </div>
          </div>

          {/* Output Schema */}
          <div className="card bg-base-200">
            <div className="card-body py-3">
              <h4 className="font-semibold text-md mb-2">Output Schema</h4>
              <pre className="bg-base-300 p-3 rounded text-xs overflow-x-auto">
                {service.outputSchema}
              </pre>
            </div>
          </div>

          {/* Timestamps */}
          <div className="card bg-base-200">
            <div className="card-body py-3">
              <h4 className="font-semibold text-md mb-2">Timestamps</h4>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div><span className="font-medium">Created:</span> {new Date(service.createdAt).toLocaleString()}</div>
                <div><span className="font-medium">Updated:</span> {new Date(service.updatedAt).toLocaleString()}</div>
              </div>
            </div>
          </div>
        </div>

        <div className="modal-action">
          <button className="btn" onClick={onClose}>Close</button>
        </div>
      </div>
    </div>
  );
}
