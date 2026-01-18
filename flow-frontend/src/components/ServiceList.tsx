import { useState } from 'react';
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query';
import { serviceApi } from '../api/service';
import { ConfirmDialog } from './ConfirmDialog';
import { EditServiceModal } from './EditServiceModal';
import { ViewServiceModal } from './ViewServiceModal';
import type { ApiService } from '../types/service';
import { Zap, Power, PowerOff, Database, GitBranch, Edit2, Trash2, Eye } from 'lucide-react';

export function ServiceList() {
  const queryClient = useQueryClient();
  const [selectedService, setSelectedService] = useState<ApiService | null>(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);
  const [serviceToDelete, setServiceToDelete] = useState<ApiService | null>(null);

  const { data, isLoading, error } = useQuery({
    queryKey: ['services'],
    queryFn: () => serviceApi.list(),
  });

  const deleteMutation = useMutation({
    mutationFn: (sv: ApiService) => serviceApi.delete(sv.key),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['services'] });
      setIsDeleteDialogOpen(false);
      setServiceToDelete(null);
    },
  });

  if (isLoading) return <div className="flex justify-center py-8"><span className="loading loading-spinner loading-lg"></span></div>;
  if (error) return <div className="alert alert-error"><span>Error loading services: {error.message}</span></div>;
  if (!data || data.services.length === 0) {
    return (
      <div className="alert">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" className="stroke-info shrink-0 w-6 h-6"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        <span>No services found. Create your first service to get started!</span>
      </div>
    );
  }

  const handleStatusChange = async (key: string, action: 'enable' | 'disable') => {
    try {
      await serviceApi[action](key);
      queryClient.invalidateQueries({ queryKey: ['services'] });
    } catch (error) {
      console.error(`Failed to ${action} service:`, error);
    }
  };

  const handleEdit = (service: ApiService) => {
    setSelectedService(service);
    setIsEditModalOpen(true);
  };

  const handleDelete = (service: ApiService) => {
    setServiceToDelete(service);
    setIsDeleteDialogOpen(true);
  };

  const handleView = (service: ApiService) => {
    setSelectedService(service);
    setIsViewModalOpen(true);
  };

  const getModeBadge = (mode: string | null) => {
    if (!mode) return { class: 'badge-ghost', label: 'Unknown', icon: Zap };
    const config: Record<string, { class: string; label: string; icon: any }> = {
      'DATASOURCE': { class: 'badge-primary', label: 'Datasource', icon: Database },
      'WORKFLOW': { class: 'badge-secondary', label: 'Workflow', icon: GitBranch },
    };
    return config[mode] || { class: 'badge-ghost', label: mode, icon: Zap };
  };

  const getStatusBadge = (status: string) => {
    const config = {
      'ENABLED': { class: 'badge-success', label: 'Enabled' },
      'DISABLED': { class: 'badge-warning', label: 'Disabled' },
    };
    return config[status as keyof typeof config] || { class: 'badge-ghost', label: status };
  };

  return (
    <>
      <div className="space-y-4">
        {data.services.map((service) => {
        const modeBadge = getModeBadge(service.mode);
        const statusBadge = getStatusBadge(service.status);
        const ModeIcon = modeBadge.icon || Zap;

        return (
          <div key={service.id} className="card bg-base-100 shadow-xl hover:shadow-2xl transition-shadow">
            <div className="card-body">
              <div className="flex flex-col md:flex-row justify-between gap-4">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <Zap size={20} className="text-primary" />
                    <h3 className="card-title">{service.name}</h3>
                    <div className={`badge ${modeBadge.class} badge-outline flex items-center gap-1`}>
                      <ModeIcon size={12} />
                      {modeBadge.label}
                    </div>
                    <div className={`badge ${statusBadge.class} badge-outline`}>
                      {statusBadge.label}
                    </div>
                  </div>
                  <p className="text-sm text-base-content/70 mb-3">{service.description || 'No description'}</p>
                  <div className="text-xs text-base-content/60 space-y-1">
                    <div><span className="font-medium">Key:</span> {service.key}</div>
                    {service.mode === 'DATASOURCE' && service.datasourceId && (
                      <div>
                        <span className="font-medium">Datasource:</span> {service.datasourceId}:{service.datasourceVersion}
                      </div>
                    )}
                    {service.mode === 'WORKFLOW' && service.workflowId && (
                      <div>
                        <span className="font-medium">Workflow:</span> {service.workflowId}:{service.workflowVersion}
                      </div>
                    )}
                    <div><span className="font-medium">Created:</span> {new Date(service.createdAt).toLocaleString()}</div>
                  </div>
                </div>

                <div className="flex flex-row md:flex-col gap-2">
                  <button
                    onClick={() => handleView(service)}
                    className="btn btn-info gap-2 btn-sm"
                  >
                    <Eye size={16} />
                    View
                  </button>
                  <button
                    onClick={() => handleEdit(service)}
                    className="btn btn-primary gap-2 btn-sm"
                  >
                    <Edit2 size={16} />
                    Edit
                  </button>
                  {service.status === 'DISABLED' && (
                    <button
                      onClick={() => handleStatusChange(service.key, 'enable')}
                      className="btn btn-success gap-2 btn-sm"
                    >
                      <Power size={16} />
                      Enable
                    </button>
                  )}
                  {service.status === 'ENABLED' && (
                    <button
                      onClick={() => handleStatusChange(service.key, 'disable')}
                      className="btn btn-warning gap-2 btn-sm"
                    >
                      <PowerOff size={16} />
                      Disable
                    </button>
                  )}
                  {service.status === 'DISABLED' && (
                    <button
                      onClick={() => handleDelete(service)}
                      className="btn btn-error gap-2 btn-sm"
                    >
                      <Trash2 size={16} />
                      Delete
                    </button>
                  )}
                </div>
              </div>
            </div>
          </div>
        );
      })}
    </div>

    <ViewServiceModal
      isOpen={isViewModalOpen}
      onClose={() => {
        setIsViewModalOpen(false);
        setSelectedService(null);
      }}
      service={selectedService}
    />

    <EditServiceModal
      isOpen={isEditModalOpen}
      onClose={() => {
        setIsEditModalOpen(false);
        setSelectedService(null);
      }}
      onSuccess={() => {
        queryClient.invalidateQueries({ queryKey: ['services'] });
        setIsEditModalOpen(false);
        setSelectedService(null);
      }}
      service={selectedService!}
    />

    <ConfirmDialog
      isOpen={isDeleteDialogOpen}
      title="Delete Service"
      message={`Are you sure you want to delete service "${serviceToDelete?.name}"? This action cannot be undone.`}
      confirmText="Delete"
      cancelText="Cancel"
      variant="danger"
      onConfirm={() => {
        if (serviceToDelete) {
          deleteMutation.mutate(serviceToDelete);
        }
      }}
      onCancel={() => {
        setIsDeleteDialogOpen(false);
        setServiceToDelete(null);
      }}
    />
  </>
  );
}
