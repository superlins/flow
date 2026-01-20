import { useParams, useNavigate } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { workflowApi } from '../api/workflow';
import { StatusBadge } from '../components/common/StatusBadge';
import { Play, Pause, Trash2, ArrowLeft, Clock } from 'lucide-react';
import { useState } from 'react';
import { DataTable } from '../components/common/DataTable';

export function WorkflowDetailPage() {
    const { key, version } = useParams<{ key: string; version: string }>();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const [activeTab, setActiveTab] = useState('overview');

    const v = parseInt(version || '1', 10);

    const { data: workflow, isLoading } = useQuery({
        queryKey: ['workflow', key, v],
        queryFn: () => workflowApi.get(key!, v),
        enabled: !!key,
    });

    const enableMutation = useMutation({
        mutationFn: () => workflowApi.enable(key!, v),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['workflow', key, v] }),
    });

    const disableMutation = useMutation({
        mutationFn: () => workflowApi.disable(key!, v),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['workflow', key, v] }),
    });

    // Mock Execution Data (API Missing)
    const executions = [
        { id: 'exec-1', status: 'COMPLETED', startTime: '2023-10-27 10:00:00', duration: '120ms' },
        { id: 'exec-2', status: 'FAILED', startTime: '2023-10-27 10:05:00', duration: '45ms' },
        { id: 'exec-3', status: 'RUNNING', startTime: '2023-10-27 10:10:00', duration: '-' },
    ];

    if (isLoading) return <div className="p-8 text-center"><span className="loading loading-spinner loading-lg"></span></div>;
    if (!workflow) return <div className="p-8 text-center">Workflow not found</div>;

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center gap-4">
                <button onClick={() => navigate('/workflows')} className="btn btn-ghost btn-circle">
                    <ArrowLeft size={20} />
                </button>
                <div className="flex-1">
                    <div className="flex items-center gap-3">
                        <h1 className="text-2xl font-bold">{workflow.name}</h1>
                        <StatusBadge status={workflow.status} />
                    </div>
                    <p className="text-base-content/60 text-sm font-mono mt-1">
                        Key: {workflow.key} | Version: {workflow.version}
                    </p>
                </div>
                <div className="flex gap-2">
                    {workflow.status === 'ENABLED' ? (
                        <button
                            className="btn btn-warning gap-2"
                            onClick={() => disableMutation.mutate()}
                            disabled={disableMutation.isPending}
                        >
                            <Pause size={16} /> Disable
                        </button>
                    ) : (
                        <button
                            className="btn btn-success gap-2"
                            onClick={() => enableMutation.mutate()}
                            disabled={enableMutation.isPending}
                        >
                            <Play size={16} /> Enable
                        </button>
                    )}
                    <button className="btn btn-error btn-outline gap-2">
                        <Trash2 size={16} /> Delete
                    </button>
                </div>
            </div>

            {/* Tabs */}
            <div role="tablist" className="tabs tabs-bordered">
                <a
                    role="tab"
                    className={`tab ${activeTab === 'overview' ? 'tab-active' : ''}`}
                    onClick={() => setActiveTab('overview')}
                >
                    Overview
                </a>
                <a
                    role="tab"
                    className={`tab ${activeTab === 'executions' ? 'tab-active' : ''}`}
                    onClick={() => setActiveTab('executions')}
                >
                    Executions
                </a>
                <a
                    role="tab"
                    className={`tab ${activeTab === 'definition' ? 'tab-active' : ''}`}
                    onClick={() => setActiveTab('definition')}
                >
                    Definition
                </a>
            </div>

            {/* Content */}
            <div className="bg-base-100 rounded-box shadow-sm p-6 min-h-[400px]">
                {activeTab === 'overview' && (
                    <div className="space-y-4">
                        <h3 className="text-lg font-semibold">Description</h3>
                        <p className="text-base-content/80">
                            {workflow.description || "No description provided."}
                        </p>
                        <div className="divider"></div>
                        <h3 className="text-lg font-semibold">Metadata</h3>
                        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
                            <div className="stat p-4 bg-base-200/50 rounded-lg">
                                <div className="stat-title">Created</div>
                                <div className="stat-value text-lg">{new Date(workflow.createdAt).toLocaleDateString()}</div>
                            </div>
                        </div>
                    </div>
                )}

                {activeTab === 'executions' && (
                    <div>
                        <div className="alert alert-info mb-6">
                            <Clock size={20} />
                            <span>Showing mock execution history. Backend integration pending.</span>
                        </div>
                        <DataTable
                            data={executions}
                            keyField="id"
                            columns={[
                                { key: 'id', header: 'Execution ID', className: 'font-mono text-xs' },
                                { key: 'status', header: 'Status', render: (item) => <StatusBadge status={item.status} /> },
                                { key: 'startTime', header: 'Start Time' },
                                { key: 'duration', header: 'Duration' },
                            ]}
                        />
                    </div>
                )}

                {activeTab === 'definition' && (
                    <div className="mockup-code">
                        <pre><code>{JSON.stringify({ input: workflow.inputSchema, output: workflow.outputSchema }, null, 2)}</code></pre>
                    </div>
                )}
            </div>
        </div>
    );
}
