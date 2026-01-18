import { useQuery, useQueryClient } from '@tanstack/react-query';
import { pluginApi } from '../api/plugin';
import { Puzzle, Power, PowerOff, RotateCcw, Trash2 } from 'lucide-react';

export function PluginList() {
  const queryClient = useQueryClient();
  const { data, isLoading, error } = useQuery({
    queryKey: ['plugins'],
    queryFn: () => pluginApi.list(),
  });

  if (isLoading) return <div className="flex justify-center py-8"><span className="loading loading-spinner loading-lg"></span></div>;
  if (error) return <div className="alert alert-error"><span>Error loading plugins: {error.message}</span></div>;
  if (!data || data.plugins.length === 0) {
    return (
      <div className="alert">
        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" className="stroke-info shrink-0 w-6 h-6"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        <span>No plugins found. This is normal if you haven't configured any datasource plugins yet.</span>
      </div>
    );
  }

  const handleAction = async (pluginId: string, action: 'start' | 'stop' | 'reload') => {
    try {
      await pluginApi[action](pluginId);
      queryClient.invalidateQueries({ queryKey: ['plugins'] });
    } catch (error) {
      console.error(`Failed to ${action} plugin:`, error);
    }
  };

  const getStatusBadge = (state: string) => {
    const config = {
      'STARTED': { class: 'badge-success', label: 'Running' },
      'STOPPED': { class: 'badge-warning', label: 'Stopped' },
      'DISABLED': { class: 'badge-error', label: 'Disabled' },
      'UNLOADED': { class: 'badge-ghost', label: 'Unloaded' },
    };
    return config[state as keyof typeof config] || { class: 'badge-info', label: state };
  };

  return (
    <div className="space-y-4">
      {data.plugins.map((plugin) => {
        const statusBadge = getStatusBadge(plugin.state);
        return (
          <div key={plugin.pluginId} className="card bg-base-100 shadow-xl hover:shadow-2xl transition-shadow">
            <div className="card-body">
              <div className="flex flex-col md:flex-row justify-between gap-4">
                <div className="flex-1">
                  <div className="flex items-center gap-3 mb-2">
                    <Puzzle size={20} className="text-primary" />
                    <h3 className="card-title">{plugin.name}</h3>
                    <div className={`badge ${statusBadge.class} badge-outline`}>
                      {statusBadge.label}
                    </div>
                  </div>
                  <p className="text-sm text-base-content/70 mb-3">{plugin.description || 'No description'}</p>
                  <div className="text-xs text-base-content/60 space-y-1">
                    <div><span className="font-medium">ID:</span> {plugin.pluginId}</div>
                    <div><span className="font-medium">Version:</span> {plugin.pluginVersion}</div>
                    <div><span className="font-medium">Author:</span> {plugin.author}</div>
                    {plugin.supportedTypes.length > 0 && (
                      <div>
                        <span className="font-medium">Supported Types:</span> {plugin.supportedTypes.join(', ')}
                      </div>
                    )}
                    <div><span className="font-medium">Ready:</span> {plugin.ready ? 'Yes' : 'No'}</div>
                    <div><span className="font-medium">Auth Required:</span> {plugin.requiresAuth ? 'Yes' : 'No'}</div>
                  </div>
                </div>

                <div className="flex flex-row md:flex-col gap-2">
                  {plugin.state === 'STOPPED' && (
                    <button
                      onClick={() => handleAction(plugin.pluginId, 'start')}
                      className="btn btn-success gap-2 btn-sm"
                    >
                      <Power size={16} />
                      Start
                    </button>
                  )}
                  {plugin.state === 'STARTED' && (
                    <button
                      onClick={() => handleAction(plugin.pluginId, 'stop')}
                      className="btn btn-warning gap-2 btn-sm"
                    >
                      <PowerOff size={16} />
                      Stop
                    </button>
                  )}
                  {plugin.state !== 'UNLOADED' && (
                    <button
                      onClick={() => handleAction(plugin.pluginId, 'reload')}
                      className="btn btn-info gap-2 btn-sm"
                    >
                      <RotateCcw size={16} />
                      Reload
                    </button>
                  )}
                  {plugin.state !== 'STARTED' && (
                    <button
                      onClick={async () => {
                        try {
                          await pluginApi.unload(plugin.pluginId);
                          queryClient.invalidateQueries({ queryKey: ['plugins'] });
                        } catch (error) {
                          console.error('Failed to unload plugin:', error);
                        }
                      }}
                      className="btn btn-error gap-2 btn-sm"
                    >
                      <Trash2 size={16} />
                      Unload
                    </button>
                  )}
                </div>
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}