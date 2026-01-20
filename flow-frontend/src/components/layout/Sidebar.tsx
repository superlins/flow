import { NavLink } from 'react-router-dom';
import { LayoutDashboard, GitBranch, Database, Zap, Puzzle, X, ChevronLeft, ChevronRight } from 'lucide-react';
import { useState } from 'react';

interface SidebarProps {
    isOpen: boolean;
    onClose: () => void;
}

export function Sidebar({ isOpen, onClose }: SidebarProps) {
    const [collapsed, setCollapsed] = useState(false);

    const navItems = [
        { to: '/', label: 'Dashboard', icon: LayoutDashboard },
        { to: '/workflows', label: 'Workflows', icon: GitBranch },
        { to: '/datasources', label: 'Datasources', icon: Database },
        { to: '/services', label: 'Services', icon: Zap },
        { to: '/plugins', label: 'Plugins', icon: Puzzle },
    ];

    const sidebarWidth = collapsed ? 'w-20' : 'w-64';

    return (
        <>
            {/* Mobile overlay */}
            {isOpen && (
                <div
                    className="fixed inset-0 bg-black/50 z-20 lg:hidden"
                    onClick={onClose}
                ></div>
            )}

            <aside
                className={`
          fixed lg:static inset-y-0 left-0 z-30
          bg-base-100 border-r border-base-200
          transition-all duration-300 ease-in-out
          ${isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
          ${sidebarWidth}
        `}
            >
                <div className="flex flex-col h-full">
                    {/* Header */}
                    <div className="flex items-center h-16 px-4 border-b border-base-200 justify-between">
                        {!collapsed && (
                            <div className="flex items-center gap-2 font-bold text-xl text-primary">
                                <GitBranch className="w-8 h-8" />
                                <span>FlowManager</span>
                            </div>
                        )}
                        {collapsed && (
                            <div className="flex justify-center w-full">
                                <GitBranch className="w-8 h-8 text-primary" />
                            </div>
                        )}

                        <button
                            onClick={onClose}
                            className="lg:hidden btn btn-ghost btn-sm btn-circle"
                        >
                            <X size={20} />
                        </button>
                    </div>

                    {/* Navigation */}
                    <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
                        {navItems.map((item) => (
                            <NavLink
                                key={item.to}
                                to={item.to}
                                className={({ isActive }) => `
                  flex items-center gap-3 px-3 py-3 rounded-lg transition-all duration-200
                  ${isActive
                                        ? 'bg-primary text-primary-content shadow-md'
                                        : 'text-base-content/70 hover:bg-base-200 hover:text-base-content'}
                  ${collapsed ? 'justify-center' : ''}
                `}
                                onClick={() => {
                                    if (window.innerWidth < 1024) onClose();
                                }}
                                title={collapsed ? item.label : undefined}
                            >
                                <item.icon size={20} />
                                {!collapsed && <span className="font-medium">{item.label}</span>}
                            </NavLink>
                        ))}
                    </nav>

                    {/* Collapse Toggle (Desktop only) */}
                    <div className="hidden lg:flex p-4 border-t border-base-200 justify-end">
                        <button
                            onClick={() => setCollapsed(!collapsed)}
                            className="btn btn-ghost btn-circle btn-sm"
                        >
                            {collapsed ? <ChevronRight size={20} /> : <ChevronLeft size={20} />}
                        </button>
                    </div>
                </div>
            </aside>
        </>
    );
}
