import { useState } from 'react';
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query';
import { serviceApi } from '../api/service';
import { ConfirmDialog } from './ConfirmDialog';
import { EditServiceModal } from './EditServiceModal';
import { ViewServiceModal } from './ViewServiceModal';
import { CreateServiceModal } from './CreateServiceModal';
import { DataTable, type Column } from './common/DataTable';
import { StatusBadge } from './common/StatusBadge';
import type { ApiService } from '../types/service';
import { Power, PowerOff, Edit2, Trash2, Eye, Plus, Database, GitBranch } from 'lucide-react';

export function ServiceList() {
  const queryClient = useQueryClient();
  const [selectedService, setSelectedService] = useState<ApiService | null>(null);

  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
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

  const handleStatusChange = async (key: string, action: 'enable' | 'disable') => {
    try {
      await serviceApi[action](key);
      queryClient.invalidateQueries({ queryKey: ['services'] });
    } catch (error) {
      console.error(`Failed to ${action} service:`, error);
    }
  };

  const handleCreateSuccess = () => {
    queryClient.invalidateQueries({ queryKey: ['services'] });
    setIsCreateModalOpen(false);
  };

  const columns: Column<ApiService>[] = [
    { key: 'name', header: 'Name', className: 'font-semibold' },
    {
      key: 'key',
      header: 'Key',
      className: 'font-mono text-xs opacity-70',
      render: (s) => <span className="font-mono">{s.key}</span>
    },
    {
      key: 'mode',
      header: 'Mode',
      render: (s) => {
        const Icon = s.mode === 'DATASOURCE' ? Database : GitBranch;
        const style = s.mode === 'DATASOURCE' ? 'badge-primary' : 'badge-secondary';
        return (
          <div className={`badge ${style} badge-outline gap-1`}>
            <Icon size={12} /> {s.mode}
          </div>
        );
      }
    },
    {
      key: 'status',
      header: 'Status',
      render: (s) => <StatusBadge status={s.status} />
    },
    {
      key: 'actions',
      header: 'Actions',
      className: 'text-right min-w-[200px]',
      render: (service) => (
        <div className="join">
          <button
            className="btn btn-sm btn-ghost join-item tooltip"
            data-tip="View Details"
            onClick={(e) => { e.stopPropagation(); setSelectedService(service); setIsViewModalOpen(true); }}
          >
            <Eye size={16} />
          </button>

          <button
            className="btn btn-sm btn-ghost join-item tooltip"
            data-tip="Edit"
            onClick={(e) => { e.stopPropagation(); setSelectedService(service); setIsEditModalOpen(true); }}
          >
            <Edit2 size={16} />
          </button>

          {service.status === 'DISABLED' && (
            <button
              className="btn btn-sm btn-ghost text-success join-item tooltip"
              data-tip="Enable"
              onClick={(e) => { e.stopPropagation(); handleStatusChange(service.key, 'enable'); }}
            >
              <Power size={16} />
            </button>
          )}
          {service.status === 'ENABLED' && (
            <button
              className="btn btn-sm btn-ghost text-warning join-item tooltip"
              data-tip="Disable"
              onClick={(e) => { e.stopPropagation(); handleStatusChange(service.key, 'disable'); }}
            >
              <PowerOff size={16} />
            </button>
          )}

          {service.status === 'DISABLED' && (
            <button
              className="btn btn-sm btn-ghost text-error join-item tooltip"
              data-tip="Delete"
              onClick={(e) => { e.stopPropagation(); setServiceToDelete(service); setIsDeleteDialogOpen(true); }}
            >
              <Trash2 size={16} />
            </button>
          )}
        </div>
      )
    }
  ];

  if (error) return <div className="alert alert-error"><span>Error loading services: {error.message}</span></div>;


  return (
    <>
      <div className="space-y-4">
        <div className="flex justify-between items-center">
          <h2 className="text-xl font-bold">Services</h2>
          <button className="btn btn-accent gap-2" onClick={() => setIsCreateModalOpen(true)}>
            <Plus size={20} /> New Service
          </button>
        </div>

        <DataTable
          data={data?.services || []}
          columns={columns}
          keyField="key"
          isLoading={isLoading}
          emptyMessage="No services found."
          onRowClick={(s) => { setSelectedService(s); setIsViewModalOpen(true); }}
        />
      </div>

      <CreateServiceModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSuccess={handleCreateSuccess}
      />

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
        message={`Are you sure you want to delete service "${serviceToDelete?.name}"?`}
        confirmText="Delete"
        cancelText="Cancel"
        variant="danger"
        onConfirm={() => serviceToDelete && deleteMutation.mutate(serviceToDelete)}
        onCancel={() => setIsDeleteDialogOpen(false)}
      />
    </>
  );
}
