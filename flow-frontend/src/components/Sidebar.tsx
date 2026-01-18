export type View = 'workflows' | 'datasources' | 'services' | 'plugins';
import { GitBranch, Database, Zap, X, Puzzle } from 'lucide-react';

interface SidebarProps {
  currentView: View;
  setCurrentView: (view: View) => void;
  isOpen: boolean;
  onToggle: () => void;
}

export function Sidebar({ currentView, setCurrentView, isOpen, onToggle }: SidebarProps) {
  const navItems = [
    { id: 'workflows' as View, label: 'Workflows', icon: GitBranch },
    { id: 'datasources' as View, label: 'Datasources', icon: Database },
    { id: 'services' as View, label: 'Services', icon: Zap },
    { id: 'plugins' as View, label: 'Plugins', icon: Puzzle },
  ];

  return (
    <>
      {/* Mobile overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 bg-black/50 z-20 lg:hidden"
          onClick={onToggle}
        ></div>
      )}

      {/* Sidebar */}
      <aside
        className={`
          fixed left-0 top-0 h-full w-64 bg-base-100 shadow-xl z-30
          transform transition-transform duration-300 ease-in-out
          ${isOpen ? 'translate-x-0' : '-translate-x-full'}
          lg:translate-x-0 lg:static lg:transform-none
        `}
      >
        <div className="flex flex-col h-full">
          {/* Header */}
          <div className="flex items-center justify-between p-4 border-b border-base-300">
            <div className="flex items-center gap-2">
              <GitBranch className="text-primary" size={24} />
              <span className="text-xl font-semibold">Flow Manager</span>
            </div>
            <button
              onClick={onToggle}
              className="lg:hidden btn btn-ghost btn-sm btn-circle"
            >
              <X size={20} />
            </button>
          </div>

          {/* Navigation */}
          <nav className="flex-1 p-4">
            <ul className="menu menu-lg w-full gap-2">
              {navItems.map((item) => {
                const Icon = item.icon;
                return (
                  <li key={item.id}>
                    <button
                      onClick={() => {
                        setCurrentView(item.id);
                        if (window.innerWidth < 1024) {
                          onToggle();
                        }
                      }}
                      className={currentView === item.id ? 'active' : ''}
                    >
                      <Icon size={18} />
                      {item.label}
                    </button>
                  </li>
                );
              })}
            </ul>
          </nav>

          {/* Footer */}
          <div className="p-4 border-t border-base-300">
            <div className="text-xs text-base-content/60">
              <div>Flow Platform v1.0</div>
              <div className="mt-1">Declarative API System</div>
            </div>
          </div>
        </div>
      </aside>
    </>
  );
}
