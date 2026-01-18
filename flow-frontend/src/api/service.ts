import axios from 'axios';
import type {
  ApiService,
  CreateServiceRequest,
  UpdateServiceRequest,
  UpdateServiceMetadataRequest,
  ListServicesResponse,
} from '../types/service';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: `${API_BASE_URL}/api/services`,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const serviceApi = {
  list: async (datasourceKey?: string, mode?: string): Promise<ListServicesResponse> => {
    const params: any = {};
    if (datasourceKey) {
      params.datasourceKey = datasourceKey;
    }
    if (mode) {
      params.mode = mode;
    }
    const response = await api.get(``, { params });
    return response.data;
  },

  get: async (key: string): Promise<ApiService> => {
    const response = await api.get(`/${key}`);
    return response.data;
  },

  create: async (data: CreateServiceRequest): Promise<ApiService> => {
    const response = await api.post(``, data);
    return response.data;
  },

  updateMetadata: async (key: string, data: UpdateServiceMetadataRequest): Promise<ApiService> => {
    const response = await api.patch(`/${key}`, data);
    return response.data;
  },

  enable: async (key: string): Promise<ApiService> => {
    const response = await api.post(`/${key}/enable`);
    return response.data;
  },

  disable: async (key: string): Promise<ApiService> => {
    const response = await api.post(`/${key}/disable`);
    return response.data;
  },

  update: async (key: string, data: UpdateServiceRequest): Promise<ApiService> => {
    const response = await api.patch(`/${key}`, data);
    return response.data;
  },

  delete: async (key: string): Promise<void> => {
    await api.delete(`/${key}`);
  },
};
