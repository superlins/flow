import { GitBranch, Database, Zap, Activity, Plus } from 'lucide-react';
import { Link } from 'react-router-dom';

const StatCard = ({ title, value, icon: Icon, color, to }: any) => (
    <Link to={to} className="stats shadow hover:shadow-md transition-shadow cursor-pointer bg-base-100">
        <div className="stat">
            <div className={`stat-figure ${color}`}>
                <Icon size={32} />
            </div>
            <div className="stat-title">{title}</div>
            <div className={`stat-value ${color}`}>{value}</div>
            <div className="stat-desc">Total registered {title.toLowerCase()}</div>
        </div>
    </Link>
);

export function DashboardPage() {
    return (
        <div className="space-y-8">
            <div>
                <h1 className="text-3xl font-bold">Dashboard</h1>
                <p className="text-base-content/60 mt-2">Welcome to Flow Manager. Overview of your system.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
                <StatCard
                    title="Workflows"
                    value="12"
                    icon={GitBranch}
                    color="text-primary"
                    to="/workflows"
                />
                <StatCard
                    title="Datasources"
                    value="5"
                    icon={Database}
                    color="text-secondary"
                    to="/datasources"
                />
                <StatCard
                    title="Services"
                    value="8"
                    icon={Zap}
                    color="text-accent"
                    to="/services"
                />
                <StatCard
                    title="Executions"
                    value="1.2k"
                    icon={Activity}
                    color="text-info"
                    to="/workflows"
                />
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                <div className="card bg-base-100 shadow-sm">
                    <div className="card-body">
                        <h2 className="card-title">Recent Activity</h2>
                        <ul className="steps steps-vertical">
                            <li className="step step-primary">Workflow "OrderProcess" executed successfully</li>
                            <li className="step step-primary">New Datasource "MySQL-Prod" created</li>
                            <li className="step">Service "EmailGateway" updated</li>
                            <li className="step">Workflow "UserSignup" failed</li>
                        </ul>
                    </div>
                </div>

                <div className="card bg-base-100 shadow-sm">
                    <div className="card-body">
                        <h2 className="card-title mb-4">Quick Actions</h2>
                        <div className="grid grid-cols-2 gap-4">
                            <button className="btn btn-outline btn-primary h-auto py-4 flex flex-col gap-2">
                                <Plus size={24} />
                                <span>New Workflow</span>
                            </button>
                            <button className="btn btn-outline btn-secondary h-auto py-4 flex flex-col gap-2">
                                <Plus size={24} />
                                <span>New Datasource</span>
                            </button>
                            <button className="btn btn-outline btn-accent h-auto py-4 flex flex-col gap-2">
                                <Plus size={24} />
                                <span>New Service</span>
                            </button>
                            <button className="btn btn-outline btn-info h-auto py-4 flex flex-col gap-2">
                                <Activity size={24} />
                                <span>View Logs</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
