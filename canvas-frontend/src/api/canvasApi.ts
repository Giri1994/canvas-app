import axios from 'axios'
import type { CanvasResponse } from '../types/canvas.types'

const API_BASE = '/api/canvas'

/**
 * Parses the command string and calls the appropriate REST endpoint.
 * The backend expects separate JSON fields — not a raw command string.
 */
export async function executeCommand(cmd: string): Promise<CanvasResponse> {
  const parts = cmd.trim().split(/\s+/)
  const op = parts[0].toUpperCase()

  switch (op) {
    case 'C':
      return (await axios.post(`${API_BASE}/create`, {
        width: Number(parts[1]),
        height: Number(parts[2]),
      })).data

    case 'L':
      return (await axios.post(`${API_BASE}/line`, {
        x1: Number(parts[1]), y1: Number(parts[2]),
        x2: Number(parts[3]), y2: Number(parts[4]),
      })).data

    case 'R':
      return (await axios.post(`${API_BASE}/rectangle`, {
        x1: Number(parts[1]), y1: Number(parts[2]),
        x2: Number(parts[3]), y2: Number(parts[4]),
      })).data

    case 'B':
      return (await axios.post(`${API_BASE}/fill`, {
        x: Number(parts[1]), y: Number(parts[2]), color: parts[3],
      })).data

    case 'Q':
      return (await axios.post(`${API_BASE}/quit`)).data

    default:
      throw new Error('Unknown command')
  }
}
