import { useState } from 'react';
import { workflowApi } from '../api/workflow';
import { Plus, X } from 'lucide-react';
import { useQueryClient } from '@tanstack/react-query';

interface CreateWorkflowModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
}

export function CreateWorkflowModal({ isOpen, onClose, onSuccess }: CreateWorkflowModalProps) {
  const [key, setKey] = useState('');
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const queryClient = useQueryClient();

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);

    try {
      await workflowApi.create({ key, name, description });
      onSuccess();
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
      handleClose();
    } catch (error) {
      console.error('Failed to create workflow:', error);
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    setKey('');
    setName('');
    setDescription('');
    onClose();
  };

  return (
    <dialog className={`modal ${isOpen ? 'modal-open' : ''}`}>
      <div className="modal-box">
        <form method="dialog">
          <button className="btn btn-sm btn-circle btn-ghost absolute right-2 top-2" onClick={handleClose}>
            <X size={20} />
          </button>
        </form>

        <h3 className="font-bold text-lg mb-4">Create New Workflow</h3>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="form-control">
            <label className="label">
              <span className="label-text font-medium">Key *</span>
            </label>
            <input
              type="text"
              required
              value={key}
              onChange={(e) => setKey(e.target.value)}
              className="input input-bordered w-full"
              placeholder="unique-workflow-key"
            />
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text font-medium">Name *</span>
            </label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="input input-bordered w-full"
              placeholder="My Workflow"
            />
          </div>

          <div className="form-control">
            <label className="label">
              <span className="label-text font-medium">Description</span>
            </label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="textarea textarea-bordered h-24"
              placeholder="Describe what this workflow does..."
            />
          </div>

          <div className="modal-action">
            <button
              type="button"
              onClick={handleClose}
              className="btn"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="btn btn-primary gap-2"
            >
              {isSubmitting ? <span className="loading loading-spinner"></span> : <Plus size={18} />}
              {isSubmitting ? 'Creating...' : 'Create'}
            </button>
          </div>
        </form>
      </div>
      <form method="dialog" className="modal-backdrop">
        <button onClick={handleClose}>close</button>
      </form>
    </dialog>
  );
}
