import axios from 'axios';
import type { Plugin, ListPluginsResponse } from '../types/plugin';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

const api = axios.create({
  baseURL: `${API_BASE_URL}/api/plugins`,
  headers: {
    'Content-Type': 'application/json',
  },
});

export const pluginApi = {
  list: async (): Promise<ListPluginsResponse> => {
    const response = await api.get(``);
    return response.data;
  },

  get: async (pluginId: string): Promise<Plugin> => {
    const response = await api.get(`/${pluginId}`);
    return response.data;
  },

  start: async (pluginId: string): Promise<Plugin> => {
    const response = await api.post(`/${pluginId}/start`);
    return response.data;
  },

  stop: async (pluginId: string): Promise<Plugin> => {
    const response = await api.post(`/${pluginId}/stop`);
    return response.data;
  },

  reload: async (pluginId: string): Promise<Plugin> => {
    const response = await api.post(`/${pluginId}/reload`);
    return response.data;
  },

  unload: async (pluginId: string): Promise<{ message: string; pluginId: string }> => {
    const response = await api.delete(`/${pluginId}`);
    return response.data;
  },
};
