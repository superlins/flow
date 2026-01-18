import { useState } from 'react';
import { useQuery, useQueryClient, useMutation } from '@tanstack/react-query';
import { datasourceApi } from '../api/datasource';
import { ConfirmDialog } from './ConfirmDialog';
import { EditDatasourceModal } from './EditDatasourceModal';
import { ViewDatasourceModal } from './ViewDatasourceModal';
import type { ApiDatasource } from '../types/datasource';
import { Database, Power, PowerOff, Edit2, Trash2, Eye } from 'lucide-react';

export function DatasourceList() {
  const queryClient = useQueryClient();
  const [selectedDatasource, setSelectedDatasource] = useState<ApiDatasource | null>(null);
  const [isEditModalOpen, setIsEditModalOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isViewModalOpen, setIsViewModalOpen] = useState(false);
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

  if (isLoading) return <div className="flex justify-center py-8"><span className="loading loading-spinner loading-lg"></span></div>;
  if (error) return <div className="alert alert-error"><span>Error loading datasources: {error.message}</span></div>;
  if (!data || data.datasources.length === 0) {
    return (
      <div className="alert">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" className="stroke-info shrink-0 w-6 h-6"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        <span>No datasources found. Create your first datasource to get started!</span>
      </div>
    );
  }

  const handleStatusChange = async (key: string, version: number, action: 'enable' | 'disable') => {
    try {
      await datasourceApi[action](key, version);
      queryClient.invalidateQueries({ queryKey: ['datasources'] });
    } catch (error) {
      console.error(`Failed to ${action} datasource:`, error);
    }
  };

  const handleEdit = (datasource: ApiDatasource) => {
    setSelectedDatasource(datasource);
    setIsEditModalOpen(true);
  };

  const handleDelete = (datasource: ApiDatasource) => {
    setDatasourceToDelete(datasource);
    setIsDeleteDialogOpen(true);
  };

  const handleView = (datasource: ApiDatasource) => {
    setSelectedDatasource(datasource);
    setIsViewModalOpen(true);
  };

  const getTypeBadge = (type: string) => {
    const config = {
      'HTTP': { class: 'badge-primary', label: 'HTTP' },
      'R2DBC': { class: 'badge-secondary', label: 'R2DBC' },
      'CASSANDRA': { class: 'badge-accent', label: 'Cassandra' },
    };
    return config[type as keyof typeof config] || { class: 'badge-ghost', label: type };
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
        {data.datasources.map((datasource) => {
        const typeBadge = getTypeBadge(datasource.type);
        const statusBadge = getStatusBadge(datasource.status);
        return (
          <div key={datasource.id} className="card bg-base-100 shadow-xl hover:shadow-2xl transition-shadow">
            <div className="card-body">
              <div className="flex flex-col md:flex-row justify-between gap-4">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <Database size={20} className="text-primary" />
                    <h3 className="card-title">{datasource.name}</h3>
                    <div className={`badge ${typeBadge.class} badge-outline`}>
                      {typeBadge.label}
                    </div>
                    <div className={`badge ${statusBadge.class} badge-outline`}>
                      {statusBadge.label}
                    </div>
                  </div>
                  <p className="text-sm text-base-content/70 mb-3">{datasource.description || 'No description'}</p>
                  <div className="text-xs text-base-content/60 space-y-1">
                    <div><span className="font-medium">Key:</span> {datasource.key}</div>
                    <div><span className="font-medium">Version:</span> {datasource.version}</div>
                    <div><span className="font-medium">Connection:</span> {datasource.connection || 'Not configured'}</div>
                    <div><span className="font-medium">Created:</span> {new Date(datasource.createdAt).toLocaleString()}</div>
                  </div>
                </div>

                <div className="flex flex-row md:flex-col gap-2">
                  <button
                    onClick={() => handleView(datasource)}
                    className="btn btn-info gap-2 btn-sm"
                  >
                    <Eye size={16} />
                    View
                  </button>
                  <button
                    onClick={() => handleEdit(datasource)}
                    className="btn btn-primary gap-2 btn-sm"
                  >
                    <Edit2 size={16} />
                    Edit
                  </button>
                  {datasource.status === 'DISABLED' && (
                    <button
                      onClick={() => handleStatusChange(datasource.key, datasource.version, 'enable')}
                      className="btn btn-success gap-2 btn-sm"
                    >
                      <Power size={16} />
                      Enable
                    </button>
                  )}
                  {datasource.status === 'ENABLED' && (
                    <button
                      onClick={() => handleStatusChange(datasource.key, datasource.version, 'disable')}
                      className="btn btn-warning gap-2 btn-sm"
                    >
                      <PowerOff size={16} />
                      Disable
                    </button>
                  )}
                  {datasource.status === 'DISABLED' && (
                    <button
                      onClick={() => handleDelete(datasource)}
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
      message={`Are you sure you want to delete datasource "${datasourceToDelete?.name}"? This action cannot be undone.`}
      confirmText="Delete"
      cancelText="Cancel"
      variant="danger"
      onConfirm={() => {
        if (datasourceToDelete) {
          deleteMutation.mutate(datasourceToDelete);
        }
      }}
      onCancel={() => {
        setIsDeleteDialogOpen(false);
        setDatasourceToDelete(null);
      }}
    />
  </>
  );
}
