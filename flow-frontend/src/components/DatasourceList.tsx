import { useState } from 'react';
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query';
import { datasourceApi } from '../api/datasource';
import { ConfirmDialog } from './ConfirmDialog';
import { EditDatasourceModal } from './EditDatasourceModal';
import { ViewDatasourceModal } from './ViewDatasourceModal';
import { CreateDatasourceModal } from './CreateDatasourceModal';
import { DataTable, type Column } from './common/DataTable';
import { StatusBadge } from './common/StatusBadge';
import type { ApiDatasource } from '../types/datasource';
import { Power, PowerOff, Edit2, Trash2, Eye, Plus } from 'lucide-react';

export function DatasourceList() {
  const queryClient = useQueryClient();
  const [selectedDatasource, setSelectedDatasource] = useState<ApiDatasource | null>(null);

  // Modals state
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [datasourceToDelete, setDatasourceToDelete] = useState<ApiDatasource | null>(null);

  const { data, isLoading, error } = useQuery({
    queryKey: ['datasources'],
    queryFn: () => datasourceApi.list(),
  });

  const deleteMutation = useMutation({
    mutationFn: (ds: ApiDatasource) => datasourceApi.delete(ds.key, ds.version),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['datasources'] });
      setIsDeleteDialogOpen(false);
      setDatasourceToDelete(null);
    },
  });

  const handleStatusChange = async (key: string, version: number, action: 'enable' | 'disable') => {
    try {
      await datasourceApi[action](key, version);
      queryClient.invalidateQueries({ queryKey: ['datasources'] });
    } catch (error) {
      console.error(`Failed to ${action} datasource:`, error);
    }
  };

  const handleCreateSuccess = () => {
    queryClient.invalidateQueries({ queryKey: ['datasources'] });
    setIsCreateModalOpen(false);
  };

  const columns: Column<ApiDatasource>[] = [
    { key: 'name', header: 'Name', className: 'font-semibold' },
    {
      key: 'key',
      header: 'Key',
      className: 'font-mono text-xs opacity-70',
      render: (ds) => <span className="font-mono">{ds.key}</span>
    },
    {
      key: 'type',
      header: 'Type',
      render: (ds) => <div className="badge badge-outline">{ds.type}</div>
    },
    {
      key: 'status',
      header: 'Status',
      render: (ds) => <StatusBadge status={ds.status} />
    },
    {
      key: 'actions',
      header: 'Actions',
      className: 'text-right min-w-[200px]',
      render: (datasource) => (
        <div className="join">
          <button
            className="btn btn-sm btn-ghost join-item tooltip"
            data-tip="View Details"
            onClick={(e) => { e.stopPropagation(); setSelectedDatasource(datasource); setIsViewModalOpen(true); }}
          >
            <Eye size={16} />
          </button>

          <button
            className="btn btn-sm btn-ghost join-item tooltip"
            data-tip="Edit"
            onClick={(e) => { e.stopPropagation(); setSelectedDatasource(datasource); setIsEditModalOpen(true); }}
          >
            <Edit2 size={16} />
          </button>

          {datasource.status === 'DISABLED' && (
            <button
              className="btn btn-sm btn-ghost text-success join-item tooltip"
              data-tip="Enable"
              onClick={(e) => { e.stopPropagation(); handleStatusChange(datasource.key, datasource.version, 'enable'); }}
            >
              <Power size={16} />
            </button>
          )}
          {datasource.status === 'ENABLED' && (
            <button
              className="btn btn-sm btn-ghost text-warning join-item tooltip"
              data-tip="Disable"
              onClick={(e) => { e.stopPropagation(); handleStatusChange(datasource.key, datasource.version, 'disable'); }}
            >
              <PowerOff size={16} />
            </button>
          )}

          {datasource.status === 'DISABLED' && (
            <button
              className="btn btn-sm btn-ghost text-error join-item tooltip"
              data-tip="Delete"
              onClick={(e) => { e.stopPropagation(); setDatasourceToDelete(datasource); setIsDeleteDialogOpen(true); }}
            >
              <Trash2 size={16} />
            </button>
          )}
        </div>
      )
    }
  ];

  if (error) return <div className="alert alert-error"><span>Error loading datasources: {error.message}</span></div>;

  return (
    <>
      <div className="space-y-4">
        <div className="flex justify-between items-center">
          <h2 className="text-xl font-bold">Datasources</h2>
          <button className="btn btn-secondary gap-2" onClick={() => setIsCreateModalOpen(true)}>
            <Plus size={20} /> New Datasource
          </button>
        </div>

        <DataTable
          data={data?.datasources || []}
          columns={columns}
          keyField={(ds) => `${ds.key}-${ds.version}`}
          isLoading={isLoading}
          emptyMessage="No datasources found."
          onRowClick={(ds) => { setSelectedDatasource(ds); setIsViewModalOpen(true); }}
        />
      </div>

      <CreateDatasourceModal
        isOpen={isCreateModalOpen}
        onClose={() => setIsCreateModalOpen(false)}
        onSuccess={handleCreateSuccess}
      />

      <ViewDatasourceModal
        isOpen={isViewModalOpen}
        onClose={() => {
          setIsViewModalOpen(false);
          setSelectedDatasource(null);
        }}
        datasource={selectedDatasource}
      />

      <EditDatasourceModal
        isOpen={isEditModalOpen}
        onClose={() => {
          setIsEditModalOpen(false);
          setSelectedDatasource(null);
        }}
        onSuccess={() => {
          queryClient.invalidateQueries({ queryKey: ['datasources'] });
          setIsEditModalOpen(false);
          setSelectedDatasource(null);
        }}
        datasource={selectedDatasource!}
      />

      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        title="Delete Datasource"
        message={`Are you sure you want to delete datasource "${datasourceToDelete?.name}"?`}
        confirmText="Delete"
        cancelText="Cancel"
        variant="danger"
        onConfirm={() => datasourceToDelete && deleteMutation.mutate(datasourceToDelete)}
        onCancel={() => setIsDeleteDialogOpen(false)}
      />
    </>
  );
}
