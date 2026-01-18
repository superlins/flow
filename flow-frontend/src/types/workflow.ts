export type WorkflowStatus = 'DRAFT' | 'ENABLED' | 'DISABLED' | 'ARCHIVED';
export type ExecutionStatus = 'SUCCESS' | 'FAILED' | 'RUNNING';

export interface Workflow {
  id: string;
  key: string;
  version: number;
  name: string;
  description: string;
  status: WorkflowStatus;
  inputSchema: string;
  outputSchema: string;
  createdAt: string;
  updatedAt: string;
  nodes: Record<string, any>;
  connections: Record<string, any>;
}

export interface WorkflowExecution {
  executionId: string;
  workflowId: string;
  status: ExecutionStatus;
  input: any;
  output: any;
  errorMessage: string | null;
  startedAt: string;
  finishedAt: string;
  durationMs: number;
}

export interface UpdateWorkflowRequest {
  name: string;
  description?: string;
  inputSchema?: string;
  outputSchema?: string;
}

export interface CreateWorkflowRequest {
  key: string;
  name: string;
  description?: string;
  inputSchema?: string;
  outputSchema?: string;
}
