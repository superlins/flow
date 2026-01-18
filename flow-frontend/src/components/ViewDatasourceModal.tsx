import { X } from 'lucide-react';
import type { ApiDatasource } from '../types/datasource';

interface ViewDatasourceModalProps {
  isOpen: boolean;
  onClose: () => void;
  datasource: ApiDatasource | null;
}

export function ViewDatasourceModal({ isOpen, onClose, datasource }: ViewDatasourceModalProps) {
  if (!isOpen || !datasource) return null;

  return (
    <div className="modal modal-open">
      <div className="modal-box max-w-3xl">
        <div className="flex justify-between items-center mb-4">
          <h3 className="font-bold text-lg">Datasource Details</h3>
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
                <div><span className="font-medium">Key:</span> {datasource.key}</div>
                <div><span className="font-medium">Name:</span> {datasource.name}</div>
                <div><span className="font-medium">Version:</span> {datasource.version}</div>
                <div><span className="font-medium">Type:</span> {datasource.type}</div>
                <div><span className="font-medium">Status:</span> {datasource.status}</div>
                <div><span className="font-medium">Strict:</span> {datasource.strict ? 'Yes' : 'No'}</div>
              </div>
              <div className="mt-2 text-sm">
                <span className="font-medium">Description:</span>
                <p className="mt-1">{datasource.description || 'No description provided'}</p>
              </div>
            </div>
          </div>

          {/* Connection */}
          <div className="card bg-base-200">
            <div className="card-body py-3">
              <h4 className="font-semibold text-md mb-2">Connection</h4>
              <div className="text-sm">
                <div><span className="font-medium">URL:</span> {datasource.connection || 'Not configured'}</div>
              </div>
            </div>
          </div>

          {/* Input Schema */}
          <div className="card bg-base-200">
            <div className="card-body py-3">
              <h4 className="font-semibold text-md mb-2">Input Schema</h4>
              <pre className="bg-base-300 p-3 rounded text-xs overflow-x-auto">
                {datasource.inputSchema}
              </pre>
            </div>
          </div>

          {/* Output Schema */}
          <div className="card bg-base-200">
            <div className="card-body py-3">
              <h4 className="font-semibold text-md mb-2">Output Schema</h4>
              <pre className="bg-base-300 p-3 rounded text-xs overflow-x-auto">
                {datasource.outputSchema}
              </pre>
            </div>
          </div>

          {/* Timestamps */}
          <div className="card bg-base-200">
            <div className="card-body py-3">
              <h4 className="font-semibold text-md mb-2">Timestamps</h4>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div><span className="font-medium">Created:</span> {new Date(datasource.createdAt).toLocaleString()}</div>
                <div><span className="font-medium">Updated:</span> {new Date(datasource.updatedAt).toLocaleString()}</div>
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
