interface ConfirmDialogProps {
  isOpen: boolean;
  title: string;
  message: string;
  confirmText?: string;
  cancelText?: string;
  variant?: 'danger' | 'warning' | 'info' | 'success';
  onConfirm: () => void;
  onCancel: () => void;
}

export function ConfirmDialog({
  isOpen,
  title,
  message,
  confirmText = 'Confirm',
  cancelText = 'Cancel',
  variant = 'danger',
  onConfirm,
  onCancel,
}: ConfirmDialogProps) {
  if (!isOpen) return null;

  const getConfirmButtonClass = () => {
    const baseClass = 'btn';
    switch (variant) {
      case 'danger':
        return `${baseClass} btn-error`;
      case 'warning':
        return `${baseClass} btn-warning`;
      case 'success':
        return `${baseClass} btn-success`;
      case 'info':
        return `${baseClass} btn-info`;
      default:
        return `${baseClass} btn-error`;
    }
  };

  return (
    <dialog className="modal modal-open" aria-modal="true">
      <div className="modal-box">
        <h3 className="font-bold text-lg">{title}</h3>
        <p className="py-4">{message}</p>
        <div className="modal-action">
          <button className="btn" onClick={onCancel}>
            {cancelText}
          </button>
          <button className={getConfirmButtonClass()} onClick={onConfirm}>
            {confirmText}
          </button>
        </div>
      </div>
      <form method="dialog" className="modal-backdrop">
        <button onClick={onCancel}>close</button>
      </form>
    </dialog>
  );
}
