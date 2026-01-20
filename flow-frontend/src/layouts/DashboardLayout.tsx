import { Outlet, useLocation } from 'react-router-dom';
import { useState } from 'react';
import { Sidebar } from '../components/layout/Sidebar';
import { Menu, Bell, Search, User } from 'lucide-react';

export function DashboardLayout() {
    const [isSidebarOpen, setSidebarOpen] = useState(false);
    const location = useLocation();

    // Simple breadcrumb logic
    const pathSegments = location.pathname.split('/').filter(Boolean);
    const breadcrumbs = pathSegments.length > 0
        ? pathSegments
        : ['Dashboard'];

    return (
        <div className="flex h-screen bg-base-200/50 font-sans">
            <Sidebar
                isOpen={isSidebarOpen}
                onClose={() => setSidebarOpen(false)}
            />

            <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
                {/* Top Navbar */}
                <header className="navbar bg-base-100 border-b border-base-200 px-4 h-16 flex-none">
                    <div className="flex-none lg:hidden">
                        <button
                            onClick={() => setSidebarOpen(true)}
                            className="btn btn-square btn-ghost"
                        >
                            <Menu size={20} />
                        </button>
                    </div>

                    <div className="flex-1 px-4">
                        {/* Breadcrumbs */}
                        <div className="text-sm breadcrumbs hidden md:block">
                            <ul>
                                <li><span className="opacity-50">Flow</span></li>
                                {breadcrumbs.map((segment, index) => (
                                    <li key={index} className="capitalize font-medium">{segment}</li>
                                ))}
                            </ul>
                        </div>
                        {/* Mobile Title */}
                        <span className="text-lg font-semibold md:hidden capitalize">
                            {breadcrumbs[breadcrumbs.length - 1]}
                        </span>
                    </div>

                    <div className="flex-none gap-2">
                        <div className="form-control hidden sm:block">
                            <div className="relative">
                                <input
                                    type="text"
                                    placeholder="Search..."
                                    className="input input-bordered input-sm w-24 md:w-64 pl-9"
                                />
                                <Search size={16} className="absolute left-3 top-2.5 text-base-content/50" />
                            </div>
                        </div>

                        <button className="btn btn-ghost btn-circle btn-sm">
                            <div className="indicator">
                                <Bell size={20} />
                                <span className="badge badge-xs badge-primary indicator-item"></span>
                            </div>
                        </button>

                        <div className="dropdown dropdown-end">
                            <div tabIndex={0} role="button" className="btn btn-ghost btn-circle avatar">
                                <div className="w-9 h-9 rounded-full bg-primary/10 flex items-center justify-center text-primary">
                                    <User size={20} />
                                </div>
                            </div>
                            <ul tabIndex={0} className="mt-3 z-[1] p-2 shadow menu menu-sm dropdown-content bg-base-100 rounded-box w-52">
                                <li><a>Profile</a></li>
                                <li><a>Settings</a></li>
                                <li><a>Logout</a></li>
                            </ul>
                        </div>
                    </div>
                </header>

                {/* Main Content */}
                <main className="flex-1 overflow-auto p-4 md:p-6 lg:p-8">
                    <div className="max-w-7xl mx-auto w-full">
                        <Outlet />
                    </div>
                </main>
            </div>
        </div>
    );
}
