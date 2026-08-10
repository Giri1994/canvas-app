// Shared TypeScript interfaces used across the entire Canvas app

export interface CanvasResponse {
  canvas: string      // multiline ASCII canvas returned by the backend
  message?: string
  success?: boolean
}

export interface HistoryEntry {
  id: number
  command: string
  status: 'OK' | 'ERROR'
}
