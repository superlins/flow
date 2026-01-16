# Flow - 前端设计文档

> **日期**: 2025-01-17
> **版本**: 1.0
> **目标**: 创建生产级、美观、易用的前端界面，支持Workflow编排、执行监控和数据源管理

---

## 目录

1. [设计理念](#1-设计理念)
2. [技术栈](#2-技术栈)
3. [项目结构](#3-项目结构)
4. [设计规范](#4-设计规范)
5. [组件库](#5-组件库)
6. [状态管理](#6-状态管理)
7. [API集成](#7-api集成)
8. [页面设计](#8-页面设计)
9. [Workflow编辑器](#9-workflow编辑器)
10. [开发指南](#10-开发指南)

---

## 1. 设计理念

### 1.1 核心价值观

- **专业性**: 面向运维和开发者，需要清晰、高效的信息展示
- **可操作性**: 快速配置、实时预览、即时反馈
- **可观测性**: 执行历史、监控指标、错误追踪一目了然
- **一致性**: 统一的视觉语言和交互模式

### 1.2 设计风格

**整体风格**: 现代化、专业、略带技术感

**参考对象**:
- GitLab CI/CD的可视化编辑
- Vercel的仪表盘设计
- GitHub Actions的执行界面
- Figma的工具栏布局

**设计关键词**:
```
Clean → 简洁明了，去除冗余
Functional → 功能优先，适度装饰
Responsive → 响应式布局，适配多端
Accessible → 无障碍设计，键盘导航
```

---

## 2. 技术栈

### 2.1 核心框架

```json
{
  "framework": "React 18.3+",
  "language": "TypeScript 5.3+",
  "bundler": "Vite 5.0+",
  "router": "React Router v6.22+"
}
```

### 2.2 UI框架与样式

```json
{
  "styling": "Tailwind CSS 3.4+",
  "component": "DaisyUI 4.10+",
  "icons": "lucide-react",
  "visualization": "react-flow 11.11+",
  "animation": "framer-motion 11.0+"
}
```

### 2.3 状态管理

```json
{
  "client": "Zustand 4.5+",
  "server": "@tanstack/react-query 5.28+",
  "form": "react-hook-form 7.51+",
  "validation": "zod 3.22+"
}
```

### 2.4 工具库

```json
{
  "http": "axios 1.6+",
  "date": "dayjs 1.11+",
  "utils": "lodash-es 4.17+",
  "copy": "copy-to-clipboard 3.3+"
}
```

---

## 3. 项目结构

```
flow-frontend/
├── src/
│   ├── components/
│   │   ├── layout/
│   │   │   ├── Sidebar.tsx
│   │   │   ├── SidebarHeader.tsx
│   │   │   ├── SidebarNavigation.tsx
│   │   │   ├── SidebarFooter.tsx
│   │   │   ├── Header.tsx
│   │   │   └── PageLayout.tsx
│   │   ├── workflow/
│   │   │   ├── WorkflowCanvas.tsx
│   │   │   ├── WorkflowControls.tsx
│   │   │   ├── WorkflowMiniMap.tsx
│   │   │   ├── NodePanel.tsx
│   │   │   ├── ConfigPanel.tsx
│   │   │   ├── NodeComponents/
│   │   │   │   ├── BaseNode.tsx
│   │   │   │   ├── StartNode.tsx
│   │   │   │   ├── EndNode.tsx
│   │   │   │   ├── DatasourceNode.tsx
│   │   │   │   ├── ConditionalNode.tsx
│   │   │   │   ├── LoopNode.tsx
│   │   │   │   └── TransformNode.tsx
│   │   │   ├── EdgeComponents/
│   │   │   │   ├── AnimatedEdge.tsx
│   │   │   │   └── ConditionalEdge.tsx
│   │   │   └── WorkflowConfigTabs.tsx
│   │   ├── ui/
│   │   │   ├── Button.tsx
│   │   │   ├── Input.tsx
│   │   │   ├── Select.tsx
│   │   │   ├── Modal.tsx
│   │   │   ├── Dropdown.tsx
│   │   │   ├── Badge.tsx
│   │   │   ├── StatusBadge.tsx
│   │   │   ├── Toast.tsx
│   │   │   └── ...
│   │   ├── charts/
│   │   │   ├── TrendChart.tsx
│   │   │   ├── PieChart.tsx
│   │   │   └── BarChart.tsx
│   │   └── common/
│   │       ├── DataTable/
│   │       │   ├── index.tsx
│   │       │   ├── TableRow.tsx
│   │       │   ├── TableHeader.tsx
│   │       │   └── TablePagination.tsx
│   │       ├── CodeEditor.tsx
│   │       ├── JsonViewer.tsx
│   │       └── TimeAgo.tsx
│   ├── pages/
│   │   ├── Dashboard/
│   │   │   ├── index.tsx
│   │   │   ├── StatsCards.tsx
│   │   │   ├── TrendChart.tsx
│   │   │   └── FailedExecutions.tsx
│   │   ├── Workflows/
│   │   │   ├── List.tsx
│   │   │   ├── Detail.tsx
│   │   │   ├── Editor.tsx
│   │   │   └── VersionHistory.tsx
│   │   ├── Executions/
│   │   │   ├── List.tsx
│   │   │   └── Detail.tsx
│   │   │       ├── ExecutionTimeline.tsx
│   │   │       └── NodeDetailsPanel.tsx
│   │   ├── Datasources/
│   │   │   ├── List.tsx
│   │   │   ├── Create.tsx
│   │   │   └── Detail.tsx
│   │   ├── Services/
│   │   │   ├── List.tsx
│   │   │   ├── Create.tsx
│   │   │   └── Detail.tsx
│   │   └── Plugins/
│   │       └── List.tsx
│   ├── hooks/
│   │   ├── useWorkflow.ts
│   │   ├── useExecution.ts
│   │   ├── useDatasource.ts
│   │   ├── useService.ts
│   │   ├── usePlugin.ts
│   │   └── useToast.ts
│   ├── stores/
│   │   ├── workflowStore.ts
│   │   ├── executionStore.ts
│   │   ├── uiStore.ts
│   │   └── layoutStore.ts
│   ├── lib/
│   │   ├── api/
│   │   │   ├── client.ts
│   │   │   ├── workflows.ts
│   │   │   ├── executions.ts
│   │   │   ├── datasources.ts
│   │   │   ├── services.ts
│   │   │   └── plugins.ts
│   │   ├── validations/
│   │   │   ├── workflow.ts
│   │   │   ├── datasource.ts
│   │   │   └── service.ts
│   │   ├── utils/
│   │   │   ├── date.ts
│   │   │   ├── format.ts
│   │   │   └── cn.ts
│   │   └── constants/
│   │       ├── colors.ts
│   │       └── fonts.ts
│   ├── types/
│   │   ├── workflow.ts
│   │   ├── execution.ts
│   │   ├── datasource.ts
│   │   ├── service.ts
│   │   └── api.ts
│   ├── App.tsx
│   ├── main.tsx
│   └── routes.tsx
├── public/
├── index.html
├── vite.config.ts
├── tailwind.config.ts
├── tsconfig.json
└── package.json
```

---

## 4. 设计规范

### 4.1 颜色系统

```typescript
// colors.ts
export const colors = {
  // 主色调 - 专业蓝紫
  primary: {
    50: '#eff6ff',
    100: '#dbeafe',
    200: '#bfdbfe',
    300: '#93c5fd',
    400: '#60a5fa',
    500: '#3b82f6',  // 主色
    600: '#2563eb',
    700: '#1d4ed8',
    800: '#1e40af',
    900: '#1e3a8a',
  },

  // 成功色
  success: {
    500: '#22c55e',
    600: '#16a34a',
  },

  // 警告色
  warning: {
    500: '#eab308',
    600: '#ca8a04',
  },

  // 错误色
  error: {
    500: '#ef4444',
    600: '#dc2626',
  },

  // 状态色
  status: {
    draft: '#6b7280',      // gray-500
    active: '#22c55e',     // green-500
    archived: '#9ca3af',  // gray-400
    running: '#eab308',   // yellow-500
    failed: '#ef4444',    // red-500
    cancelled: '#6b7280', // gray-500
    timeout: '#f97316',   // orange-500
  },

  // 节点类型色
  nodeType: {
    start: '#a855f7',      // purple-500
    end: '#6b7280',        // gray-500
    datasource: '#22c55e', // green-500
    conditional: '#f97316',// orange-500
    loop: '#3b82f6',       // blue-500
    transform: '#ec4899',  // pink-500
  },

  // 深色主题
  dark: {
    background: '#0f172a',  // slate-900
    surface: '#1e293b',     // slate-800
    border: '#334155',      // slate-700
  },
} as const;

// Tailwind配置
export const tailwindConfig = {
  theme: {
    extend: {
      colors: {
        primary: colors.primary,
        success: colors.success,
        warning: colors.warning,
        error: colors.error,
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', 'sans-serif'],
        mono: ['Fira Code', 'monospace'],
      },
    },
  },
};
```

### 4.2 字体系统

```typescript
// fonts.ts
export const fonts = {
  // 屏幕字体
  display: {
    family: 'Inter, system-ui, -apple-system, sans-serif',
    weights: [300, 400, 500, 600, 700],
  },

  // 正文字体
  body: {
    family: 'Inter, system-ui, sans-serif',
    size: '14px',
    lineHeight: '1.5',
  },

  // 代码字体
  mono: {
    family: 'Fira Code, monospace',
    size: '13px',
    lineHeight: '1.6',
  },
} as const;

// 字号规范
export const fontSize = {
  xs: '12px',   // 辅助文字
  sm: '14px',   // 正文
  base: '16px', // 标题小
  lg: '18px',   // 标题中
  xl: '24px',   // 标题大
  '2xl': '32px',// 页面标题
  '3xl': '48px',// 英雄标题
} as const;

// 行高规范
export const lineHeight = {
  tight: '1.25',
  normal: '1.5',
  relaxed: '1.75',
} as const;
```

### 4.3 间距系统

```typescript
// spacing.ts
export const spacing = {
  // 基础间距 (4px基准)
  xs: '4px',   // 0.25rem
  sm: '8px',   // 0.5rem
  md: '16px',  // 1rem
  lg: '24px',  // 1.5rem
  xl: '32px',  // 2rem
  '2xl': '48px', // 3rem
  '3xl': '64px', // 4rem

  // 组件内间距
  padding: {
    sm: '8px 16px',
    md: '12px 24px',
    lg: '16px 32px',
  },

  // 卡片间距
  card: '24px',

  // 布局间距
  section: '32px',
  page: '64px',
} as const;
```

### 4.4 圆角系统

```typescript
export const borderRadius = {
  xs: '4px',   // 小元素
  sm: '8px',   // 按钮、输入框
  md: '12px',  // 卡片
  lg: '16px',  // 大卡片
  xl: '24px',  // 模态框
  full: '9999px', // 圆形
} as const;
```

### 4.5 阴影系统

```typescript
export const shadows = {
  xs: '0 1px 2px 0 rgb(0 0 0 / 0.05)',
  sm: '0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1)',
  md: '0 4px 6px -1px rgb(0 0 0 / 0.1), 0 2px 4px -2px rgb(0 0 0 / 0.1)',
  lg: '0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1)',
  xl: '0 20px 25px -5px rgb(0 0 0 / 0.1), 0 8px 10px -6px rgb(0 0 0 / 0.1)',
} as const;
```

---

## 5. 组件库

### 5.1 Button 按钮

```typescript
// components/ui/Button.tsx
import { cva, type VariantProps } from 'class-variance-authority';

const buttonVariants = cva(
  'inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2 font-medium transition-all focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:pointer-events-none',
  {
    variants: {
      variant: {
        default: 'bg-primary-600 text-white hover:bg-primary-700',
        ghost: 'bg-transparent text-slate-700 hover:bg-slate-100',
        outline: 'border-2 border-slate-300 text-slate-700 hover:border-slate-400',
        link: 'text-primary-600 underline-offset-4 hover:underline',
        danger: 'bg-error-500 text-white hover:bg-error-600',
      },
      size: {
        sm: 'h-8 px-3 text-sm',
        md: 'h-10 px-4',
        lg: 'h-12 px-6 text-lg',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'md',
    },
  }
);

interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  loading?: boolean;
  icon?: React.ReactNode;
}

export function Button({
  variant,
  size,
  loading,
  icon,
  children,
  className,
  disabled,
  ...props
}: ButtonProps) {
  return (
    <button
      className={cn(buttonVariants({ variant, size }), className)}
      disabled={disabled || loading}
      {...props}
    >
      {loading && <Loader2 className="h-4 w-4 animate-spin" />}
      {!loading && icon && <span className="h-4 w-4">{icon}</span>}
      {children}
    </button>
  );
}
```

### 5.2 Input 输入框

```typescript
// components/ui/Input.tsx
interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  leftIcon?: React.ReactNode;
  rightIcon?: React.ReactNode;
}

export function Input({
  label,
  error,
  leftIcon,
  rightIcon,
  className,
  ...props
}: InputProps) {
  return (
    <div className="w-full">
      {label && (
        <label className="mb-2 block text-sm font-medium text-slate-700">
          {label}
        </label>
      )}
      <div className="relative">
        {leftIcon && (
          <div className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400">
            {leftIcon}
          </div>
        )}
        <input
          className={cn(
            'w-full rounded-lg border-2 bg-white px-4 py-2.5 text-sm text-slate-900 transition-all',
            'focus:border-primary-500 focus:ring-2 focus:ring-primary-500/20',
            'disabled:bg-slate-50 disabled:text-slate-400',
            leftIcon && 'pl-10',
            rightIcon && 'pr-10',
            error ? 'border-error-500' : 'border-slate-300',
            className
          )}
          {...props}
        />
        {rightIcon && (
          <div className="absolute right-3 top-1/2 -translate-y-1/2 text-slate-400">
            {rightIcon}
          </div>
        )}
      </div>
      {error && (
        <p className="mt-1 text-xs text-error-500">{error}</p>
      )}
    </div>
  );
}
```

### 5.3 Badge 徽章

```typescript
// components/ui/Badge.tsx
import { cva, type VariantProps } from 'class-variance-authority';

const badgeVariants = cva(
  'inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-medium',
  {
    variants: {
      variant: {
        default: 'bg-slate-100 text-slate-700',
        primary: 'bg-primary-100 text-primary-700',
        success: 'bg-success-100 text-success-700',
        warning: 'bg-warning-100 text-warning-700',
        error: 'bg-error-100 text-error-700',
        outline: 'border-2 border-slate-300 text-slate-700',
      },
      size: {
        xs: 'px-2 py-0.5 text-[11px]',
        sm: 'px-2.5 py-1',
        md: 'px-3 py-1 text-xs',
      },
    },
    defaultVariants: {
      variant: 'default',
      size: 'md',
    },
  }
);

interface BadgeProps
  extends React.HTMLAttributes<HTMLSpanElement>,
    VariantProps<typeof badgeVariants> {
  icon?: React.ReactNode;
}

export function Badge({ variant, size, icon, children, ...props }: BadgeProps) {
  return (
    <span className={cn(badgeVariants({ variant, size }))} {...props}>
      {icon && <span className="h-3 w-3">{icon}</span>}
      {children}
    </span>
  );
}
```

### 5.4 StatusBadge 状态徽章

```typescript
// components/ui/StatusBadge.tsx
import type { WorkflowStatus, ExecutionStatus, NodeExecutionStatus } from '@/types';

const statusConfig = {
  // Workflow status
  DRAFT: { label: 'Draft', color: 'gray', icon: FileText },
  ACTIVE: { label: 'Active', color: 'success', icon: CheckCircle2 },
  ARCHIVED: { label: 'Archived', color: 'gray', icon: ArchiveIcon },

  // Execution status
  RUNNING: { label: 'Running', color: 'warning', icon: Loader2 },
  COMPLETED: { label: 'Success', color: 'success', icon: CheckCircle2 },
  FAILED: { label: 'Failed', color: 'error', icon: XCircle },
  CANCELLED: { label: 'Cancelled', color: 'gray', icon: Ban },
  TIMEOUT: { label: 'Timeout', color: 'warning', icon: Clock },
} as const;

interface StatusBadgeProps {
  status: WorkflowStatus | ExecutionStatus | NodeExecutionStatus;
  showIcon?: boolean;
  animate?: boolean;
}

export function StatusBadge({ status, showIcon = true, animate }: StatusBadgeProps) {
  const config = statusConfig[status as keyof typeof statusConfig];
  const Icon = config.icon;

  return (
    <Badge
      variant={config.color as any}
      className={cn(
        'gap-1.5 items-center',
        animate && status === 'RUNNING' && 'animate-pulse'
      )}
    >
      {showIcon && (
        <Icon className={cn(
          'h-3 w-3',
          status === 'RUNNING' && 'animate-spin'
        )} />
      )}
      {config.label}
    </Badge>
  );
}
```

### 5.5 Modal 模态框

```typescript
// components/ui/Modal.tsx
interface ModalProps {
  open: boolean;
  onClose: () => void;
  children: React.ReactNode;
  title?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl' | 'full';
  showClose?: boolean;
}

export function Modal({
  open,
  onClose,
  children,
  title,
  size = 'md',
  showClose = true,
}: ModalProps) {
  const sizeClasses = {
    sm: 'max-w-md',
    md: 'max-w-lg',
    lg: 'max-w-3xl',
    xl: 'max-w-5xl',
    full: 'max-w-7xl',
  };

  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent className={cn('p-0', sizeClasses[size])}>
        {title && (
          <div className="flex items-center justify-between border-b border-slate-200 p-6">
            <h2 className="text-lg font-semibold text-slate-900">{title}</h2>
            {showClose && (
              <Button variant="ghost" size="sm" onClick={onClose}>
                <X className="h-4 w-4" />
              </Button>
            )}
          </div>
        )}
        <div className="p-6">{children}</div>
      </DialogContent>
    </Dialog>
  );
}
```

### 5.6 Toast 通知

```typescript
// components/ui/Toast.tsx
import toast from 'react-hot-toast';

export const toast = {
  success: (message: string, options?: ToastOptions) => {
    toast.success(message, {
      icon: <CheckCircle2 className="h-5 w-5 text-success-500" />,
      ...options,
    });
  },

  error: (message: string, options?: ToastOptions) => {
    toast.error(message, {
      icon: <XCircle className="h-5 w-5 text-error-500" />,
      ...options,
    });
  },

  warning: (message: string, options?: ToastOptions) => {
    (toast as any).custom(message, {
      icon: <AlertCircle className="h-5 w-5 text-warning-500" />,
      ...options,
    });
  },

  info: (message: string, options?: ToastOptions) => {
    (toast as any).custom(message, {
      icon: <Info className="h-5 w-5 text-primary-500" />,
      ...options,
    });
  },

  loading: (message: string) => {
    return toast.loading(message);
  },

  promise: <T,>(
    promise: Promise<T>,
    {
      loading,
      success,
      error
    }: {
      loading: string;
      success: string | ((data: T) => string);
      error: string | ((error: Error) => string);
    }
  ) => {
    return toast.promise(promise, { loading, success, error });
  },
};
```

---

## 6. 状态管理

### 6.1 Zustand Stores

```typescript
// stores/workflowStore.ts
interface WorkflowState {
  // Workflow数据
  workflow: Workflow | null;
  nodes: Node[];
  edges: Edge[];

  // 编辑状态
  isDirty: boolean;
  selectedNodeId: string | null;

  // 操作方法
  setWorkflow: (workflow: Workflow) => void;
  setNodes: (nodes: Node[]) => void;
  setEdges: (edges: Edge[]) => void;
  addNode: (node: Node) => void;
  removeNode: (nodeId: string) => void;
  updateNode: (nodeId: string, updates: Partial<Node>) => void;
  selectNode: (nodeId: string | null) => void;
  reset: () => void;
}

export const useWorkflowStore = create<WorkflowState>((set) => ({
  workflow: null,
  nodes: [],
  edges: [],
  isDirty: false,
  selectedNodeId: null,

  setWorkflow: (workflow) => set({ workflow }),

  setNodes: (nodes) => set({ nodes, isDirty: true }),

  setEdges: (edges) => set({ edges, isDirty: true }),

  addNode: (node) => set((state) => ({
    nodes: [...state.nodes, node],
    isDirty: true,
  })),

  removeNode: (nodeId) => set((state) => ({
    nodes: state.nodes.filter((n) => n.id !== nodeId),
    edges: state.edges.filter(
      (e) => e.source !== nodeId && e.target !== nodeId
    ),
    isDirty: true,
  })),

  updateNode: (nodeId, updates) => set((state) => ({
    nodes: state.nodes.map((n) =>
      n.id === nodeId ? { ...n, ...updates } : n
    ),
    isDirty: true,
  })),

  selectNode: (nodeId) => set({ selectedNodeId: nodeId }),

  reset: () => set({
    workflow: null,
    nodes: [],
    edges: [],
    isDirty: false,
    selectedNodeId: null,
  }),
}));

// stores/layoutStore.ts
interface LayoutState {
  sidebarCollapsed: boolean;
  configPanelOpen: boolean;
  toggleSidebar: () => void;
  toggleConfigPanel: () => void;
  setSidebarCollapsed: (collapsed: boolean) => void;
  setConfigPanelOpen: (open: boolean) => void;
}

export const useLayoutStore = create<LayoutState>((set) => ({
  sidebarCollapsed: false,
  configPanelOpen: true,

  toggleSidebar: () => set((state) => ({
    sidebarCollapsed: !state.sidebarCollapsed,
  })),

  toggleConfigPanel: () => set((state) => ({
    configPanelOpen: !state.configPanelOpen,
  })),

  setSidebarCollapsed: (collapsed) => set({ sidebarCollapsed: collapsed }),

  setConfigPanelOpen: (open) => set({ configPanelOpen: open }),
}));
```

### 6.2 React Query Hooks

```typescript
// hooks/useWorkflow.ts
export function useWorkflow(key: string, version: number) {
  return useQuery({
    queryKey: ['workflow', key, version],
    queryFn: () => api.workflows.get(key, version),
  });
}

export function useWorkflows(filters?: WorkflowFilters) {
  return useQuery({
    queryKey: ['workflows', filters],
    queryFn: () => api.workflows.list(filters),
  });
}

export function useCreateWorkflow() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: api.workflows.create,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflows'] });
      toast.success('Workflow created successfully');
    },
    onError: (error) => {
      toast.error('Failed to create workflow');
    },
  });
}

export function useUpdateWorkflow() {
  const queryClient = useQueryClient();
  const { workflow } = useWorkflowStore();

  return useMutation({
    mutationFn: () => api.workflows.update(workflow!.key, workflow!.version, workflow!),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['workflow'] });
      toast.success('Workflow updated successfully');
    },
  });
}

// hooks/useExecution.ts
export function useExecution(id: string) {
  return useQuery({
    queryKey: ['execution', id],
    queryFn: () => api.executions.get(id),
    refetchInterval: 2000, // 2秒轮询
    enabled: !!id,
  });
}

export function useExecutions(filters?: ExecutionFilters) {
  return useQuery({
    queryKey: ['executions', filters],
    queryFn: () => api.executions.list(filters),
  });
}

export function useExecuteWorkflow() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (input: ExecuteWorkflowInput) =>
      api.executions.execute(input),
    onSuccess: (execution) => {
      queryClient.invalidateQueries({ queryKey: ['executions'] });
      toast.success('Workflow execution started');
      // 可以跳转到执行详情页
      window.location.href = `/executions/${execution.id}`;
    },
  });
}
```

---

## 7. API集成

### 7.1 API客户端配置

```typescript
// lib/api/client.ts
import axios from 'axios';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// 请求拦截器
apiClient.interceptors.request.use((config) => {
  // 添加认证token
  const token = localStorage.getItem('auth_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // API错误
      switch (error.response.status) {
        case 401:
          toast.error('Unauthorized: Please login again');
          // 跳转到登录页
          break;
        case 403:
          toast.error('Forbidden: You don\'t have permission');
          break;
        case 404:
          toast.error('Resource not found');
          break;
        case 500:
          toast.error('Server error. Please try again later');
          break;
        default:
          toast.error(error.response.data?.message || 'An error occurred');
      }
    } else if (error.request) {
      // 网络错误
      toast.error('Network error. Please check your connection');
    }
    return Promise.reject(error);
  }
);
```

### 7.2 API模块

```typescript
// lib/api/workflows.ts
export const workflows = {
  // 获取Workflow列表
  list: (filters?: WorkflowFilters) =>
    apiClient.get<Workflow[]>('/workflows', { params: filters })
      .then(res => res.data),

  // 获取单个Workflow
  get: (key: string, version: number) =>
    apiClient.get<Workflow>(`/workflows/${key}/${version}`)
      .then(res => res.data),

  // 创建Workflow
  create: (data: CreateWorkflowInput) =>
    apiClient.post<Workflow>('/workflows', data)
      .then(res => res.data),

  // 更新Workflow
  update: (key: string, version: number, data: UpdateWorkflowInput) =>
    apiClient.put<Workflow>(`/workflows/${key}/${version}`, data)
      .then(res => res.data),

  // 删除Workflow
  delete: (key: string, version: number) =>
    apiClient.delete(`/workflows/${key}/${version}`)
      .then(res => res.data),

  // 激活Workflow
  activate: (key: string, version: number) =>
    apiClient.post(`/workflows/${key}/${version}/activate`)
      .then(res => res.data),

  // 归档Workflow
  archive: (key: string, version: number) =>
    apiClient.post(`/workflows/${key}/${version}/archive`)
      .then(res => res.data),

  // 执行Workflow
  execute: (key: string, version: number, input: JsonNode) =>
    apiClient.post<Execution>(`/workflows/${key}/${version}/execute`, input)
      .then(res => res.data),
};

// lib/api/executions.ts
export const executions = {
  // 获取执行历史
  list: (filters?: ExecutionFilters) =>
    apiClient.get<Execution[]>('/executions', { params: filters })
      .then(res => res.data),

  // 获取单个执行详情
  get: (id: string) =>
    apiClient.get<ExecutionDetail>(`/executions/${id}`)
      .then(res => res.data),

  // 取消执行
  cancel: (id: string, reason: string) =>
    apiClient.post(`/executions/${id}/cancel`, { reason })
      .then(res => res.data),

  // 获取执行统计
  stats: (workflowId: WorkflowId, startTime: string, endTime: string) =>
    apiClient.get<ExecutionStatistics>(
      `/executions/stats`,
      { params: { workflowId: `${workflowId.key}:${workflowId.version}`, startTime, endTime } }
    )
      .then(res => res.data),
};

// lib/api/datasources.ts
export const datasources = {
  list: () =>
    apiClient.get<ApiDatasource[]>('/datasources')
      .then(res => res.data),

  get: (key: string, version: number) =>
    apiClient.get<ApiDatasource>(`/datasources/${key}/${version}`)
      .then(res => res.data),

  create: (data: CreateDatasourceInput) =>
    apiClient.post<ApiDatasource>('/datasources', data)
      .then(res => res.data),

  update: (key: string, version: number, data: UpdateDatasourceInput) =>
    apiClient.patch<ApiDatasource>(`/datasources/${key}/${version}`, data)
      .then(res => res.data),

  enable: (key: string, version: number) =>
    apiClient.post(`/datasources/${key}/${version}/enable`)
      .then(res => res.data),

  disable: (key: string, version: number) =>
    apiClient.post(`/datasources/${key}/${version}/disable`)
      .then(res => res.data),
};
```

---

## 8. 页面设计

### 8.1 Dashboard - 仪表盘

**特点**:
- 深色渐变背景
- 统计卡片悬浮效果
- SVG折线图（渐变填充）
- 失败列表实时更新

```typescript
// pages/Dashboard/index.tsx
export function Dashboard() {
  const { data: stats, isLoading } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: fetchDashboardStats,
    refetchInterval: 30000, // 30秒刷新
  });

  if (isLoading) {
    return <DashboardSkeleton />;
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-900 via-slate-800 to-slate-900">
      <PageLayout>
        {/* Hero Section */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          className="mb-8"
        >
          <h1 className="text-5xl font-light tracking-tight text-white/95">
            Dashboard
          </h1>
          <p className="mt-2 text-xl text-slate-400">
            Monitor and manage your workflow orchestration
          </p>
        </motion.div>

        {/* Statistics Cards */}
        <div className="mb-8 grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          {[
            { label: 'Total Executions', value: stats.totalExecutions, icon: Activity, gradient: 'from-blue-500/20 to-purple-500/20' },
            { label: 'Failed', value: stats.failed, icon: AlertCircle, gradient: 'from-red-500/20 to-orange-500/20' },
            { label: 'Avg Duration', value: `${stats.avgDuration}ms`, icon: Clock, gradient: 'from-green-500/20 to-emerald-500/20' },
            { label: 'Success Rate', value: `${stats.successRate}%`, icon: TrendingUp, gradient: 'from-indigo-500/20 to-blue-500/20' },
          ].map((card, i) => (
            <motion.div
              key={card.label}
              initial={{ opacity: 0, y: 40 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.1 + i * 0.1 }}
              className="group relative overflow-hidden rounded-2xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm transition-all duration-300 hover:border-white/20 hover:bg-white/10 hover:shadow-2xl hover:shadow-white/5"
            >
              <div className={cn('absolute inset-0 bg-gradient-to-br transition-opacity duration-300', 'opacity-0 group-hover:opacity-100', card.gradient)} />
              <div className="relative">
                <card.icon className="mb-4 h-8 w-8 text-white/60 group-hover:text-white transition-colors" />
                <div className="text-4xl font-light text-white">{card.value}</div>
                <div className="mt-1 text-sm text-slate-400">{card.label}</div>
              </div>
            </motion.div>
          ))}
        </div>

        {/* Charts Section */}
        <div className="mb-8 grid grid-cols-1 lg:grid-cols-2 gap-6">
          <motion.div
            initial={{ opacity: 0, y: 40 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.5 }}
            className="rounded-2xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm"
          >
            <div className="mb-6 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-white/90">Execution Trend</h3>
              <select className="rounded-lg border border-white/20 bg-white/5 px-3 py-1.5 text-sm text-white/70">
                <option value="24h">Last 24h</option>
                <option value="7d">Last 7 days</option>
                <option value="30d">Last 30 days</option>
              </select>
            </div>
            <ExecutionTrendChart data={stats.trend} />
          </motion.div>

          <motion.div
            initial={{ opacity: 0, y: 40 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.6 }}
            className="rounded-2xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm"
          >
            <h3 className="mb-6 text-lg font-semibold text-white/90">Trending Workflows</h3>
            <div className="space-y-4">
              {stats.trendingWorkflows?.map((wf, i) => (
                <TrendingWorkflowItem key={wf.id} workflow={wf} index={i} />
              ))}
            </div>
          </motion.div>
        </div>

        {/* Failed Executions */}
        <motion.div
          initial={{ opacity: 0, y: 40 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: 0.7 }}
          className="rounded-2xl border border-white/10 bg-white/5 p-6 backdrop-blur-sm"
        >
          <div className="mb-6 flex items-center justify-between">
            <h3 className="text-lg font-semibold text-white/90">Recent Failures</h3>
            <Link to="/executions" className="text-primary-400 hover:text-primary-300 transition-colors">
              View all →
            </Link>
          </div>
          <FailedExecutionsList executions={stats.failedExecutions} />
        </motion.div>
      </PageLayout>
    </div>
  );
}
```

### 8.2 WorkflowList - Workflow列表

```typescript
// pages/Workflows/List.tsx
export function WorkflowList() {
  const { data: workflows, isLoading } = useWorkflows();
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<WorkflowStatus | 'all'>('all');

  const filteredWorkflows = useMemo(() => {
    return workflows?.filter(wf => {
      const matchesSearch = wf.name.toLowerCase().includes(search.toLowerCase()) ||
                          wf.key.toLowerCase().includes(search.toLowerCase());
      const matchesStatus = statusFilter === 'all' || wf.status === statusFilter;
      return matchesSearch && matchesStatus;
    });
  }, [workflows, search, statusFilter]);

  return (
    <PageLayout>
      <div className="mb-8 flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-semibold text-slate-900">Workflows</h1>
          <p className="mt-1 text-slate-500">
            Manage and orchestrate your workflows
          </p>
        </div>
        <Button>
          <Plus className="h-4 w-4" />
          Create Workflow
        </Button>
      </div>

      {/* Filters */}
      <div className="mb-6 flex gap-4">
        <Input
          placeholder="Search workflows..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          leftIcon={<Search className="h-4 w-4" />}
          className="max-w-md"
        />
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="outline">
              Status: {statusFilter === 'all' ? 'All' : statusFilter}
              <ChevronDown className="ml-2 h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent>
            <DropdownMenuItem onClick={() => setStatusFilter('all')}>
              All
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setStatusFilter('DRAFT')}>
              Draft
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setStatusFilter('ACTIVE')}>
              Active
            </DropdownMenuItem>
            <DropdownMenuItem onClick={() => setStatusFilter('ARCHIVED')}>
              Archived
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      {/* Table */}
      <div className="rounded-xl border border-slate-200 bg-white shadow-lg overflow-hidden">
        <Table>
          <TableHeader className="bg-slate-50">
            <TableRow>
              <TableHead>Workflow</TableHead>
              <TableHead>Status</TableHead>
              <TableHead>Version</TableHead>
              <TableHead>Last Execution</TableHead>
              <TableHead>Success Rate</TableHead>
              <TableHead>Actions</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              <WorkflowSkeleton />
            ) : (
              filteredWorkflows?.map((workflow) => (
                <WorkflowRow key={`${workflow.key}:${workflow.version}`} workflow={workflow} />
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </PageLayout>
  );
}

function WorkflowRow({ workflow }: { workflow: Workflow }) {
  const navigate = useNavigate();

  return (
    <TableRow className="group border-b-slate-100 hover:bg-slate-50 transition-colors">
      <TableCell>
        <button
          onClick={() => navigate(`/workflows/${workflow.key}/${workflow.version}`)}
          className="flex items-center gap-3 text-left"
        >
          <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-to-br from-indigo-500 to-purple-600 text-white shadow-lg">
            <WorkflowIcon className="h-5 w-5" />
          </div>
          <div>
            <div className="font-medium text-slate-900 group-hover:text-indigo-600 transition-colors">
              {workflow.name}
            </div>
            <div className="text-xs text-slate-500">
              {workflow.key}
            </div>
          </div>
        </button>
      </TableCell>
      <TableCell>
        <StatusBadge status={workflow.status} />
      </TableCell>
      <TableCell>
        <Badge variant="outline">v{workflow.version}</Badge>
      </TableCell>
      <TableCell>
        {workflow.lastExecution ? (
          <div className="text-sm">
            <div className="text-slate-900">
              {formatDistanceToNow(new Date(workflow.lastExecution), { addSuffix: true })}
            </div>
            <div className="text-xs text-slate-500">
              {formatDuration(workflow.lastExecutionDuration)}
            </div>
          </div>
        ) : (
          <span className="text-slate-400">Never</span>
        )}
      </TableCell>
      <TableCell>
        <SuccessRateIndicator rate={workflow.successRate} />
      </TableCell>
      <TableCell>
        <WorkflowActionsDropdown workflow={workflow} />
      </TableCell>
    </TableRow>
  );
}
```

### 8.3 ExecutionDetail - 执行详情

```typescript
// pages/Executions/Detail.tsx
export function ExecutionDetail() {
  const { id } = useParams<{ id: string }>();
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
  const { data: execution } = useExecution(id);

  if (!execution) {
    return <ExecutionDetailSkeleton />;
  }

  return (
    <PageLayout>
      <div className="mb-8 flex items-center gap-4">
        <Button variant="ghost" size="sm" onClick={() => navigate(-1)}>
          <ArrowLeft className="h-4 w-4" />
        </Button>
        <div>
          <h1 className="text-2xl font-semibold text-slate-900">
            Execution Details
          </h1>
          <p className="mt-1 text-slate-500">
            #{execution.id}
          </p>
        </div>
        <StatusBadge status={execution.status} />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Execution Timeline */}
        <div className="lg:col-span-2">
          <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-lg">
            <div className="mb-6 flex items-center justify-between">
              <h3 className="text-lg font-semibold text-slate-900">Execution Timeline</h3>
              <div className="flex gap-2">
                <Badge variant="outline">
                  <Clock className="h-3 w-3 mr-1" />
                  {formatDuration(execution.duration)}
                </Badge>
                {execution.error && (
                  <Badge variant="error">
                    <AlertCircle className="h-3 w-3 mr-1" />
                    Failed
                  </Badge>
                )}
              </div>
            </div>

            <ExecutionTimeline
              nodes={execution.nodeExecutions}
              selectedNodeId={selectedNodeId}
              onSelectNode={setSelectedNodeId}
            />
          </div>
        </div>

        {/* Node Details Panel */}
        <div className="lg:col-span-1">
          <div className="sticky top-4">
            {selectedNodeId ? (
              <NodeDetailsPanel
                nodeId={selectedNodeId}
                execution={execution}
                onClose={() => setSelectedNodeId(null)}
              />
            ) : (
              <div className="rounded-xl border border-slate-200 bg-white p-6 shadow-lg">
                <div className="flex flex-col items-center justify-center py-12 text-center">
                  <Info className="h-12 w-12 text-slate-300 mb-4" />
                  <h3 className="text-slate-900 font-medium">Select a Node</h3>
                  <p className="mt-1 text-slate-500">
                    Click on any node to view its details
                  </p>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </PageLayout>
  );
}

// ExecutionTimeline组件
function ExecutionTimeline({
  nodes,
  selectedNodeId,
  onSelectNode
}: ExecutionTimelineProps) {
  return (
    <div className="space-y-4">
      {nodes.map((node, index) => (
        <motion.div
          key={node.nodeId}
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ delay: index * 0.05 }}
          onClick={() => onSelectNode(node.nodeId)}
          className={cn(
            "group flex items-start gap-4 rounded-xl p-4 cursor-pointer transition-all",
            "border-2",
            selectedNodeId === node.nodeId
              ? "border-primary-500 bg-primary-50"
              : "border-transparent hover:border-slate-300 hover:bg-slate-50"
          )}
        >
          {/* Node Icon */}
          <div className={cn(
            "flex h-12 w-12 shrink-0 items-center justify-center rounded-xl border-2",
            nodeStatusColors[node.status]
          )}>
            {getNodeIcon(node.nodeType)}
          </div>

          {/* Node Info */}
          <div className="flex-1 min-w-0">
            <div className="flex items-center justify-between">
              <h4 className="font-medium text-slate-900 truncate">
                {node.nodeName}
              </h4>
              <NodeStatusBadge status={node.status} />
            </div>
            <p className="mt-1 text-sm text-slate-500 truncate">
              {node.nodeType}
            </p>
            {node.duration && (
              <div className="mt-2 inline-flex items-center gap-1 text-xs text-slate-400">
                <Clock className="h-3 w-3" />
                {formatDuration(node.duration)}
              </div>
            )}
            {node.error && (
              <div className="mt-2 text-xs text-error-600 truncate">
                {node.error}
              </div>
            )}
          </div>

          {/* Arrow (if not last) */}
          {index < nodes.length - 1 && (
            <div className="h-12 w-8 flex items-end justify-end pb-4 text-slate-300">
              <ChevronDown className="h-5 w-5" />
            </div>
          )}
        </motion.div>
      ))}
    </div>
  );
}
```

---

## 9. Workflow编辑器

### 9.1 WorkflowCanvas 画布

```typescript
// components/workflow/WorkflowCanvas.tsx
export function WorkflowCanvas() {
  const { nodes, edges, onNodesChange, onEdgesChange, onConnect } = useWorkflowCanvas();
  const workflowStore = useWorkflowStore();

  // 自定义节点类型
  const nodeTypes = useMemo(() => ({
    start: (props: NodeProps) => <StartNode {...props} />,
    end: (props: NodeProps) => <EndNode {...props} />,
    datasource: (props: NodeProps) => <DatasourceNode {...props} />,
    conditional: (props: NodeProps) => <ConditionalNode {...props} />,
    loop: (props: NodeProps) => <LoopNode {...props} />,
    transform: (props: NodeProps) => <TransformNode {...props} />,
  }), []);

  // 自定义连线类型
  const edgeTypes = useMemo(() => ({
    default: AnimatedEdge,
    conditional: ConditionalEdge,
  }), []);

  return (
    <div className="h-full bg-slate-50">
      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        onConnect={onConnect}
        nodeTypes={nodeTypes}
        edgeTypes={edgeTypes}
        defaultEdgeOptions={{
          type: 'default',
          animated: true,
          style: { stroke: '#94a3b8', strokeWidth: 2 },
        }}
        fitView
        className="workflow-canvas"
      >
        <Background
          pattern="dots"
          gap={20}
          size={1}
          color="#cbd5e1"
        />
        <Controls
          className="!bg-white !border !border-slate-200 !rounded-lg !shadow-lg"
        />
        <MiniMap
          className="!bg-white !border !border-slate-200 !rounded-lg"
          maskColor="rgba(0, 0, 0, 0.1)"
        />
      </ReactFlow>
    </div>
  );

  function useWorkflowCanvas() {
    const workflowStore = useWorkflowStore();
    const [reactFlowInstance, setReactFlowInstance] = useState<ReactFlowInstance>();

    const onNodesChange: OnNodesChange = useCallback((changes) => {
      workflowStore.setNodes(
        applyNodeChanges(changes, workflowStore.nodes)
      );
    }, [workflowStore]);

    const onEdgesChange: OnEdgesChange = useCallback((changes) => {
      workflowStore.setEdges(
        applyEdgeChanges(changes, workflowStore.edges)
      );
    }, [workflowStore]);

    const onConnect: OnConnect = useCallback((connection) => {
      workflowStore.setEdges([
        ...workflowStore.edges,
        { ...connection, animated: true },
      ]);
    }, [workflowStore]);

    return {
      nodes: workflowStore.nodes,
      edges: workflowStore.edges,
      onNodesChange,
      onEdgesChange,
      onConnect,
    };
  }
}
```

### 9.2 NodePanel 节点面板

```typescript
// components/workflow/NodePanel.tsx
const nodeTypes = [
  { type: 'start', label: 'Start', icon: Play, color: 'purple' },
  { type: 'end', label: 'End', icon: Stop, color: 'gray' },
  { type: 'datasource', label: 'Datasource', icon: Database, color: 'green' },
  { type: 'conditional', label: 'If/Else', icon: GitBranch, color: 'orange' },
  { type: 'loop', label: 'Loop', icon: RotateCcw, color: 'blue' },
  { type: 'transform', label: 'Transform', icon: RefreshCw, color: 'pink' },
];

export function NodePanel() {
  const workflowStore = useWorkflowStore();
  const reactFlowInstance = useReactFlowContext();

  const onDragStart = (event: React.DragEvent, nodeType: NodeType) => {
    event.dataTransfer.setData('application/reactflow', nodeType);
    event.dataTransfer.effectAllowed = 'move';
  };

  const onDragOver = (event: React.DragEvent) => {
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
  };

  const onDrop = (event: React.DragEvent) => {
    event.preventDefault();

    const nodeType = event.dataTransfer.getData('application/reactflow') as NodeType;
    const position = reactFlowInstance.project({
      x: event.clientX,
      y: event.clientY,
    });

    const newNode: Node = {
      id: `node_${Date.now()}`,
      type: nodeType,
      position,
      data: {
        label: nodeTypes.find(nt => nt.type === nodeType)?.label || nodeType,
        type: nodeType.toUpperCase(),
      },
    };

    workflowStore.addNode(newNode);
  };

  return (
    <div
      className="w-64 border-r border-slate-200 bg-white p-4"
      onDragOver={onDragOver}
      onDrop={onDrop}
    >
      <h3 className="mb-4 text-sm font-semibold text-slate-700">
        Node Library
      </h3>

      <div className="space-y-2">
        {nodeTypes.map((node) => (
          <div
            key={node.type}
            draggable
            onDragStart={(e) => onDragStart(e, node.type as NodeType)}
            className={cn(
              "group flex items-center gap-3 rounded-xl border-2 p-3 cursor-move",
              "transition-all duration-200",
              "border-slate-200 bg-white",
              "hover:border-slate-300 hover:shadow-md hover:-translate-y-0.5"
            )}
          >
            <div className={cn(
              "flex h-10 w-10 items-center justify-center rounded-lg text-white",
              `bg-${node.color}-500`
            )}>
              <node.icon className="h-5 w-5" />
            </div>
            <span className="font-medium text-slate-700">
              {node.label}
            </span>
          </div>
        ))}
      </div>

      <div className="mt-8 rounded-xl bg-slate-50 p-4">
        <p className="mb-3 text-xs font-semibold text-slate-600 uppercase">
          Tip
        </p>
        <p className="text-xs text-slate-500">
          Drag nodes from this panel to the canvas to add them to your workflow.
        </p>
      </div>
    </div>
  );
}
```

### 9.3 BaseNode 节点基础组件

```typescript
// components/workflow/NodeComponents/BaseNode.tsx
export function BaseNode({ data, selected }: NodeProps) {
  const isSelected = selected;
  const status = data.status; // from execution

  return (
    <div className={cn(
      "group relative min-w-[200px] rounded-xl border-2 shadow-sm transition-all duration-200",
      isSelected && "border-primary-500 ring-2 ring-primary-500/20 ring-offset-2",
      !isSelected && "border-slate-200 hover:border-slate-300 hover:shadow-md",
      status === 'RUNNING' && "animate-pulse",
    )}>
      {/* Node Header */}
      <div className={cn(
        "flex items-center gap-2 rounded-t-xl px-4 py-3",
        nodeHeaderColors[data.type]
      )}>
        {getNodeIcon(data.type)}
        <span className="font-medium text-sm">{data.label}</span>
        {status && (
          <StatusBadge status={status} showIcon animate />
        )}
        {/* Quick Actions */}
        <div className="ml-auto flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <QuickActionButton type="edit" />
          <QuickActionButton type="delete" />
        </div>
      </div>

      {/* Node Content */}
      <div className="bg-white px-4 py-3">
        {data.description && (
          <p className="mb-2 text-xs text-slate-500">
            {data.description}
          </p>
        )}

        {/* Node-specific info */}
        {data.datasourceKey && (
          <div className="text-xs font-medium text-slate-700">
            {data.datasourceKey}:{data.datasourceVersion}
          </div>
        )}

        {data.condition && (
          <div className="flex items-center gap-1 text-xs text-slate-600">
            <Code className="h-3 w-3" />
            <span className="truncate">{data.condition}</span>
          </div>
        )}
      </div>

      {/* Handles */}
      <Handle
        type="target"
        position={Position.Top}
        className="!border-slate-300 !bg-slate-400 !w-3 !h-3"
      />
      <Handle
        type="source"
        position={Position.Bottom}
        className="!border-slate-300 !bg-slate-400 !w-3 !h-3"
      />
    </div>
  );
}

const nodeHeaderColors = {
  START: 'bg-purple-100 text-purple-700',
  END: 'bg-gray-100 text-gray-700',
  DATASOURCE: 'bg-green-100 text-green-700',
  CONDITIONAL: 'bg-orange-100 text-orange-700',
  LOOP: 'bg-blue-100 text-blue-700',
  TRANSFORM: 'bg-pink-100 text-pink-700',
};
```

---

## 10. 开发指南

### 10.1 环境配置

```bash
# 安装依赖
npm install

# 开发模式
npm run dev

# 构建生产版本
npm run build

# 预览生产构建
npm run preview

# 代码格式化
npm run format

# 代码检查
npm run lint
```

### 10.2 环境变量

```bash
# .env.development
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_WS_URL=ws://localhost:8080/ws

# .env.production
VITE_API_BASE_URL=https://api.flow.example.com/v1
VITE_WS_URL=wss://api.flow.example.com/ws
```

### 10.3 组件开发规范

```typescript
// 组件模板
import { type ComponentProps } from '@/types';

interface MyComponentProps extends ComponentProps {
  // Prop 定义
  variant?: 'primary' | 'secondary';
  size?: 'sm' | 'md' | 'lg';
  children?: React.ReactNode;
}

export function MyComponent({
  variant = 'primary',
  size = 'md',
  children,
  className,
  ...props
}: MyComponentProps) {
  return (
    <div
      className={cn(
        'base-styles',
        variantClasses[variant],
        sizeClasses[size],
        className
      )}
      {...props}
    >
      {children}
    </div>
  );
}
```

### 10.4 测试规范

```typescript
// 组件测试示例
import { render, screen } from '@testing-library/react';
import { vi } from 'vitest';
import { expect } from 'vitest';
import { Button } from '@/components/ui/Button';

describe('Button', () => {
  it('renders with default props', () => {
    render(<Button>Click me</Button>);
    expect(screen.getByRole('button')).toHaveTextContent('Click me');
  });

  it('shows loading state', () => {
    render(<Button loading>Loading...</Button>);
    expect(screen.getByRole('button')).toBeDisabled();
  });
});

// Hook测试示例
import { renderHook, waitFor } from '@testing-library/react';
import { useWorkflows } from '@/hooks/useWorkflow';

vi.mock('@/lib/api/workflows');

describe('useWorkflows', () => {
  it('fetches workflows successfully', async () => {
    const mockWorkflows = [{ id: '1', name: 'Test Workflow' }];
    vi.mocked(workflows.list).mockResolvedValue(mockWorkflows);

    const { result } = renderHook(() => useWorkflows());

    expect(result.current.isLoading).toBe(true);

    await waitFor(() => {
      expect(result.current.data).toEqual(mockWorkflows);
    });
  });
});
```

---

## 设计原则总结

1. **一致性**: 统一的视觉语言和交互模式
2. **可用性**: 清晰的反馈、明确的标签、合理的默认值
3. **可访问性**: 键盘导航、ARIA标签、对比度
4. **性能**: 代码分割、懒加载、图片优化
5. **响应式**: 移动端优先、断点适配
6. **国际化**: 支持多语言切换（预留）

---

**文档版本**: 1.0
**最后更新**: 2025-01-17
