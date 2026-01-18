export type PluginState = 'STARTED' | 'STOPPED' | 'UNLOADED' | 'DISABLED' | 'UNKNOWN';

export interface Plugin {
  id: string;
  name: string;
  version: string;
  description: string;
  author: string;
  state: PluginState;
  supportedTypes: string[];
  configSchema: Record<string, string>;
  requiresAuth: boolean;
  ready: boolean;
  pluginId: string;
  pluginVersion: string;
}

export interface ListPluginsResponse {
  plugins: Plugin[];
  total: number;
}
