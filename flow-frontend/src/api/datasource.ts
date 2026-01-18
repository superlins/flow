import axios from 'axios';
import type { ApiDatasource, CreateDatasourceRequest, UpdateDatasourceRequest, ListDatasourcesResponse } from '../types/datasource';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: `${API_BASE_URL}/api/datasources`,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const datasourceApi = {
  list: async (key?: string): Promise<ListDatasourcesResponse> => {
    const params = key ? { key } : {};
    const response = await api.get(``, { params });
    return response.data;
  },

  get: async (key: string, version: number): Promise<ApiDatasource> => {
    const response = await api.get(`/${key}/${version}`);
    return response.data;
  },

  create: async (data: CreateDatasourceRequest): Promise<ApiDatasource> => {
    const response = await api.post(``, data);
    return response.data;
  },

  enable: async (key: string, version: number): Promise<ApiDatasource> => {
    const response = await api.post(`/${key}/${version}/enable`);
    return response.data;
  },

  disable: async (key: string, version: number): Promise<ApiDatasource> => {
    const response = await api.post(`/${key}/${version}/disable`);
    return response.data;
  },

  update: async (key: string, version: number, data: UpdateDatasourceRequest): Promise<ApiDatasource> => {
    const response = await api.patch(`/${key}/${version}`, data);
    return response.data;
  },

  delete: async (key: string, version: number): Promise<void> => {
    await api.delete(`/${key}/${version}`);
  },
};
