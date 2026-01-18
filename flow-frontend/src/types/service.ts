export type ServiceStatus = 'ENABLED' | 'DISABLED';
export type ServiceMode = 'DATASOURCE' | 'WORKFLOW';

export interface ApiService {
  id: string;
  key: string;
  name: string;
  description: string;
  status: ServiceStatus;
  mode: ServiceMode;
  datasourceId: string | null;
  datasourceVersion: number | null;
  workflowId: string | null;
  workflowVersion: number | null;
  inputSchema: string;
  outputSchema: string;
  inputMapping: string;
  outputMapping: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateServiceRequest {
  key: string;
  name: string;
  description?: string;
  mode: ServiceMode;
  datasourceKey?: string;
  datasourceVersion?: number;
  workflowKey?: string;
  workflowVersion?: number;
  inputSchema?: string;
  outputSchema?: string;
  inputMapping?: any;
  outputMapping?: any;
}

export interface UpdateServiceRequest {
  name: string;
  description?: string;
  inputSchema?: string;
  outputSchema?: string;
  inputMapping?: any;
  outputMapping?: any;
}

export interface UpdateServiceMetadataRequest {
  name?: string;
  description?: string;
}

export interface ListServicesResponse {
  services: ApiService[];
  total: number;
}
