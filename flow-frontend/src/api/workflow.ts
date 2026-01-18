import axios from 'axios';
import type { Workflow, WorkflowExecution, CreateWorkflowRequest, UpdateWorkflowRequest } from '../types/workflow';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: `${API_BASE_URL}/api/workflows`,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const workflowApi = {
  list: async (key?: string, status?: string): Promise<{ workflows: Workflow[]; total: number }> => {
    const params = new URLSearchParams();
    if (key) params.append('key', key);
    if (status) params.append('status', status);
    const response = await api.get(``, { params });
    return response.data;
  },

  get: async (key: string, version: number): Promise<Workflow> => {
    const response = await api.get(`/${key}/${version}`);
    return response.data;
  },

  create: async (data: CreateWorkflowRequest): Promise<Workflow> => {
    const response = await api.post(``, data);
    return response.data;
  },

  enable: async (key: string, version: number): Promise<Workflow> => {
    const response = await api.post(`/${key}/${version}/enable`);
    return response.data;
  },

  disable: async (key: string, version: number): Promise<Workflow> => {
    const response = await api.post(`/${key}/${version}/disable`);
    return response.data;
  },

  archive: async (key: string, version: number): Promise<Workflow> => {
    const response = await api.post(`/${key}/${version}/archive`);
    return response.data;
  },

  execute: async (key: string, version: number, input: any): Promise<WorkflowExecution> => {
    const response = await api.post(`/${key}/${version}/execute`, input);
    return response.data;
  },

  getExecution: async (executionId: string): Promise<WorkflowExecution> => {
    const response = await api.get(`/executions/${executionId}`);
    return response.data;
  },

  update: async (key: string, version: number, data: UpdateWorkflowRequest): Promise<Workflow> => {
    const response = await api.patch(`/${key}/${version}`, data);
    return response.data;
  },

  delete: async (key: string, version: number): Promise<void> => {
    await api.delete(`/${key}/${version}`);
  },
};
