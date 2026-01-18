import { X } from 'lucide-react';
import type { Workflow } from '../types/workflow';

interface ViewWorkflowModalProps {
  isOpen: boolean;
  onClose: () => void;
  workflow: Workflow | null;
}

export function ViewWorkflowModal({ isOpen, onClose, workflow }: ViewWorkflowModalProps) {
  if (!isOpen || !workflow) return null;

  const getStatusBadge = () => {
    const config = {
      'DRAFT': { class: 'badge-ghost', label: 'Draft' },
      'ENABLED': { class: 'badge-success', label: 'Enabled' },
      'DISABLED': { class: 'badge-warning', label: 'Disabled' },
      'ARCHIVED': { class: 'badge-error', label: 'Archived' },
    };
    return config[workflow.status as keyof typeof config] || { class: 'badge-ghost', label: workflow.status };
  };

  const statusBadge = getStatusBadge();

  return (
    <div className="modal modal-open">
      <div className="modal-box max-w-3xl">
        <div className="flex justify-between items-center mb-4">
          <h3 className="font-bold text-lg">Workflow Details</h3>
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
                <div><span className="font-medium">Key:</span> {workflow.key}</div>
                <div><span className="font-medium">Version:</span> {workflow.version}</div>
                <div><span className="font-medium">Name:</span> {workflow.name}</div>
                <div className={`badge ${statusBadge.class} badge-outline`}>
                  {statusBadge.label}
                </div>
              </div>
              <div className="mt-2 text-sm">
                <span className="font-medium">Description:</span>
                <p className="mt-1">{workflow.description || 'No description provided'}</p>
              </div>
            </div>
          </div>

          {/* Nodes */}
          <div className="card bg-base-200">
            <div className="card-body py-3">
              <h4 className="font-semibold text-md mb-2">Nodes ({Object.keys(workflow.nodes).length})</h4>
              <pre className="bg-base-300 p-3 rounded text-xs overflow-x-auto max-h-48">
                {JSON.stringify(workflow.nodes, null, 2)}
              </pre>
            </div>
          </div>

          {/* Connections */}
          <div className="card bg-base-200">
            <div className="card-body py-3">
              <h4 className="font-semibold text-md mb-2">Connections ({Object.keys(workflow.connections).length})</h4>
              <pre className="bg-base-300 p-3 rounded text-xs overflow-x-auto max-h-48">
                {JSON.stringify(workflow.connections, null, 2)}
              </pre>
            </div>
          </div>

          {/* Input Schema */}
          <div className="card bg-base-200">
            <div className="card-body py-3">
              <h4 className="font-semibold text-md mb-2">Input Schema</h4>
              <pre className="bg-base-300 p-3 rounded text-xs overflow-x-auto">
                {workflow.inputSchema}
              </pre>
            </div>
          </div>

          {/* Output Schema */}
          <div className="card bg-base-200">
            <div className="card-body py-3">
              <h4 className="font-semibold text-md mb-2">Output Schema</h4>
              <pre className="bg-base-300 p-3 rounded text-xs overflow-x-auto">
                {workflow.outputSchema}
              </pre>
            </div>
          </div>

          {/* Timestamps */}
          <div className="card bg-base-200">
            <div className="card-body py-3">
              <h4 className="font-semibold text-md mb-2">Timestamps</h4>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div><span className="font-medium">Created:</span> {new Date(workflow.createdAt).toLocaleString()}</div>
                <div><span className="font-medium">Updated:</span> {new Date(workflow.updatedAt).toLocaleString()}</div>
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
