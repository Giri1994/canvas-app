import { useState } from 'react'
import type { HistoryEntry } from '../types/canvas.types'
import { validateCommand } from '../utils/canvasValidation'
import { executeCommand } from '../api/canvasApi'

export function useCanvas() {
  const [command,    setCommand]    = useState<string>('')
  const [canvasText, setCanvasText] = useState<string>('')
  const [history,    setHistory]    = useState<HistoryEntry[]>([])
  const [loading,    setLoading]    = useState<boolean>(false)
  const [error,      setError]      = useState<string>('')

  /** Called on every keystroke in the command input */
  const handleCommandChange = (value: string) => {
    setCommand(value)
    setError('')   // clear stale error as soon as user edits
  }

  /** Validates input, calls the backend, updates canvas + history */
  const handleExecute = async () => {
    const trimmed = command.trim()
    if (!trimmed) return

    // Step 1 — Client-side validation (instant, no network cost)
    const validationError = validateCommand(trimmed)
    if (validationError) {
      setError(validationError)
      return
    }

    setError('')
    setLoading(true)

    try {
      // Step 2 — Hit the REST API
      const response = await executeCommand(trimmed)

      // Step 3 — Q (quit) resets the canvas; all other commands update it
      setCanvasText(trimmed.toUpperCase() === 'Q' ? '' : response.canvas || '')

      // Step 4 — Append OK entry to history (immutable update)
      setHistory(prev => [...prev, { id: prev.length + 1, command: trimmed, status: 'OK' }])

    } catch (err: unknown) {
      // Step 5 — Extract the most descriptive error message available
      const msg =
        (err as { response?: { data?: { message?: string } } })?.response?.data?.message
        ?? (err as Error).message
        ?? 'API Error'

      setError(msg)
      setHistory(prev => [...prev, { id: prev.length + 1, command: trimmed, status: 'ERROR' }])

    } finally {
      setLoading(false)  // always re-enable button whether success or failure
    }
  }

  const clearHistory = () => setHistory([])

  return {
    // State values (read-only for consumers)
    command,
    canvasText,
    history,
    loading,
    error,
    // Action handlers (consumers call these to trigger state changes)
    handleCommandChange,
    handleExecute,
    clearHistory,
  }
}