export type DatasourceType = 'HTTP' | 'R2DBC' | 'CASSANDRA';
export type DatasourceStatus = 'ENABLED' | 'DISABLED';

export interface ApiDatasource {
  id: string;
  key: string;
  version: number;
  name: string;
  description: string;
  type: DatasourceType;
  status: DatasourceStatus;
  inputSchema: string;
  outputSchema: string;
  strict: boolean;
  connection: string;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateDatasourceRequest {
  name: string;
  description?: string;
  inputSchema?: string;
  outputSchema?: string;
  strict?: boolean;
  connection?: string;
}

export interface CreateDatasourceRequest {
  key: string;
  name: string;
  description?: string;
  type?: DatasourceType;
  version?: number;
  inputSchema?: string;
  outputSchema?: string;
  strict?: boolean;
  connection?: string;
  operation?: string;
}

export interface ListDatasourcesResponse {
  datasources: ApiDatasource[];
  total: number;
}
