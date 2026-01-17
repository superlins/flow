import { useState } from 'react';
import { workflowApi } from '../api/workflow';
import type { Workflow, WorkflowExecution } from '../types/workflow';
import { X, CheckCircle, XCircle, Clock, Play } from 'lucide-react';

interface ExecuteWorkflowModalProps {
  isOpen: boolean;
  onClose: () => void;
  workflow?: Workflow;
}

export function ExecuteWorkflowModal({ isOpen, onClose, workflow }: ExecuteWorkflowModalProps) {
  const [jsonData, setJsonData] = useState('{}');
  const [isExecuting, setIsExecuting] = useState(false);
  const [execution, setExecution] = useState<WorkflowExecution | null>(null);
  const [error, setError] = useState<string | null>(null);

  if (!isOpen) return null;

  const handleExecute = async () => {
    if (!workflow) return;

    setIsExecuting(true);
    setError(null);
    setExecution(null);

    try {
      const inputData = JSON.parse(jsonData);
      const result = await workflowApi.execute(workflow.key, workflow.version, inputData);
      setExecution(result);
    } catch (err) {
      if (err instanceof SyntaxError) {
        setError('Invalid JSON format');
      } else {
        setError('Execution failed: ' + (err as Error).message);
      }
    } finally {
      setIsExecuting(false);
    }
  };

  const handleClose = () => {
    setJsonData('{}');
    setExecution(null);
    setError(null);
    onClose();
  };

  return (
    <dialog className={`modal ${isOpen ? 'modal-open' : ''}`}>
      <div className="modal-box max-w-4xl">
        <form method="dialog">
          <button className="btn btn-sm btn-circle btn-ghost absolute right-2 top-2" onClick={handleClose}>
            <X size={20} />
          </button>
        </form>

        <h3 className="font-bold text-lg mb-2">Execute Workflow</h3>
        {workflow && (
          <p className="text-sm text-base-content/70 mb-4">{workflow.name}</p>
        )}

        <div className="space-y-4">
          <div className="form-control">
            <label className="label">
              <span className="label-text font-medium">Input JSON Data</span>
            </label>
            <textarea
              value={jsonData}
              onChange={(e) => setJsonData(e.target.value)}
              className="textarea textarea-bordered h-48 font-mono text-sm"
              placeholder='{"key": "value"}'
            />
            <div className="label">
              <span className="label-text-alt">Enter valid JSON to execute workflow</span>
              <button
                type="button"
                onClick={() => setJsonData(JSON.stringify(JSON.parse(jsonData), null, 2))}
                className="label-text-alt link link-primary"
              >
                Format JSON
              </button>
            </div>
          </div>

          <button
            onClick={handleExecute}
            disabled={isExecuting}
            className="btn btn-success w-full gap-2"
          >
            {isExecuting ? <span className="loading loading-spinner"></span> : <Play size={20} />}
            {isExecuting ? 'Executing...' : 'Execute Workflow'}
          </button>

          {error && (
            <div role="alert" className="alert alert-error">
              <XCircle size={20} />
              <span>{error}</span>
            </div>
          )}

          {execution && (
            <div className="space-y-4">
              <div role="alert" className={`alert ${
                execution.status === 'SUCCESS' ? 'alert-success' : 'alert-error'
              }`}>
                {execution.status === 'SUCCESS' ? <CheckCircle size={20} /> : <XCircle size={20} />}
                <div>
                  <h4 className="font-bold">Execution {execution.status}</h4>
                  <div className="text-xs">
                    <div>ID: <span className="font-mono">{execution.executionId.slice(0, 8)}...</span></div>
                    <div>Duration: {execution.durationMs}ms</div>
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="form-control">
                  <label className="label">
                    <span className="label-text font-medium"><Clock size={18} className="inline mr-1" /> Input</span>
                  </label>
                  <pre className="bg-base-200 rounded-lg p-4 overflow-auto max-h-48 font-mono text-xs">
                    {JSON.stringify(execution.input, null, 2)}
                  </pre>
                </div>

                <div className="form-control">
                  <label className="label">
                    <span className="label-text font-medium">Output</span>
                  </label>
                  <pre className="bg-base-200 rounded-lg p-4 overflow-auto max-h-48 font-mono text-xs">
                    {JSON.stringify(execution.output, null, 2)}
                  </pre>
                </div>
              </div>

              {execution.errorMessage && (
                <div role="alert" className="alert alert-error">
                  <XCircle size={20} />
                  <div>
                    <h4 className="font-bold">Error Message</h4>
                    <span className="text-sm">{execution.errorMessage}</span>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>

        <div className="modal-action">
          <form method="dialog">
            <button className="btn" onClick={handleClose}>Close</button>
          </form>
        </div>
      </div>
      <form method="dialog" className="modal-backdrop">
        <button onClick={handleClose}>close</button>
      </form>
    </dialog>
  );
}
