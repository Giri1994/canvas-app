import { describe, it, expect, vi, beforeEach } from 'vitest'
import { renderHook, act } from '@testing-library/react'
import { useCanvas } from '../hooks/useCanvas'

// Mock executeCommand — we don't want real HTTP calls in hook tests
vi.mock('../api/canvasApi', () => ({
  executeCommand: vi.fn(),
}))

import { executeCommand } from '../api/canvasApi'
const mockedExecute = vi.mocked(executeCommand)

/**
 * Tests for useCanvas custom hook.
 *
 * renderHook() mounts the hook inside a minimal React tree so we can
 * call its returned functions and inspect updated state.
 *
 * Pattern:
 *   Given  → set up mock return values + initial hook state
 *   When   → call a handler via act()
 *   Then   → assert updated state values
 */
describe('useCanvas', () => {

  beforeEach(() => {
    vi.clearAllMocks()
  })

  // ── Initial state ───────────────────────────────────────────────

  describe('initial state', () => {

    it('Given hook is mounted, Then all state fields have correct defaults', () => {
      // Given / When
      const { result } = renderHook(() => useCanvas())

      // Then
      expect(result.current.command).toBe('')
      expect(result.current.canvasText).toBe('')
      expect(result.current.history).toEqual([])
      expect(result.current.loading).toBe(false)
      expect(result.current.error).toBe('')
    })
  })

  // ── handleCommandChange ──────────────────────────────────────────

  describe('handleCommandChange', () => {

    it('Given a value is typed, When handleCommandChange is called, Then command updates', () => {
      // Given
      const { result } = renderHook(() => useCanvas())

      // When
      act(() => result.current.handleCommandChange('C 10 5'))

      // Then
      expect(result.current.command).toBe('C 10 5')
    })

    it('Given an error is showing, When user types, Then error is cleared', () => {
      // Given
      const { result } = renderHook(() => useCanvas())
      // trigger a validation error first
      act(() => result.current.handleCommandChange('INVALID'))
      act(() => { result.current.handleExecute() })

      // When — user starts editing again
      act(() => result.current.handleCommandChange('C'))

      // Then
      expect(result.current.error).toBe('')
    })
  })

  // ── handleExecute — validation path ─────────────────────────────

  describe('handleExecute — client-side validation', () => {

    it('Given command is empty, When executed, Then no API call is made', async () => {
      // Given
      const { result } = renderHook(() => useCanvas())
      // command is '' by default

      // When
      await act(async () => { await result.current.handleExecute() })

      // Then
      expect(mockedExecute).not.toHaveBeenCalled()
    })

    it('Given command is whitespace-only, When executed, Then no API call is made', async () => {
      // Given
      const { result } = renderHook(() => useCanvas())
      act(() => result.current.handleCommandChange('   '))

      // When
      await act(async () => { await result.current.handleExecute() })

      // Then
      expect(mockedExecute).not.toHaveBeenCalled()
    })

    it('Given an invalid command, When executed, Then error is set and no API call is made', async () => {
      // Given
      const { result } = renderHook(() => useCanvas())
      act(() => result.current.handleCommandChange('X 1 2'))  // unknown command

      // When
      await act(async () => { await result.current.handleExecute() })

      // Then
      expect(result.current.error).toMatch(/Unknown command/)
      expect(mockedExecute).not.toHaveBeenCalled()
    })

    it('Given C with wrong arg count, When executed, Then usage error is set', async () => {
      // Given
      const { result } = renderHook(() => useCanvas())
      act(() => result.current.handleCommandChange('C 20'))

      // When
      await act(async () => { await result.current.handleExecute() })

      // Then
      expect(result.current.error).toMatch(/Usage: C/)
    })
  })

  // ── handleExecute — success path ────────────────────────────────

  describe('handleExecute — success', () => {

    it('Given valid C command, When executed successfully, Then canvasText and history update', async () => {
      // Given
      mockedExecute.mockResolvedValue({ canvas: '----\n|  |\n----', success: true })
      const { result } = renderHook(() => useCanvas())
      act(() => result.current.handleCommandChange('C 2 1'))

      // When
      await act(async () => { await result.current.handleExecute() })

      // Then
      expect(result.current.canvasText).toBe('----\n|  |\n----')
      expect(result.current.history).toHaveLength(1)
      expect(result.current.history[0]).toMatchObject({ command: 'C 2 1', status: 'OK' })
      expect(result.current.error).toBe('')
    })

    it('Given Q command, When executed, Then canvasText is reset to empty string', async () => {
      // Given — first create a canvas
      mockedExecute.mockResolvedValue({ canvas: '----\n|  |\n----', success: true })
      const { result } = renderHook(() => useCanvas())
      act(() => result.current.handleCommandChange('C 2 1'))
      await act(async () => { await result.current.handleExecute() })

      // When — quit
      mockedExecute.mockResolvedValue({ canvas: '', success: true })
      act(() => result.current.handleCommandChange('Q'))
      await act(async () => { await result.current.handleExecute() })

      // Then — canvas cleared
      expect(result.current.canvasText).toBe('')
      expect(result.current.history).toHaveLength(2)
    })

    it('Given two successful commands, When both executed, Then history has two OK entries', async () => {
      // Given
      mockedExecute.mockResolvedValue({ canvas: 'canvas', success: true })
      const { result } = renderHook(() => useCanvas())

      // When
      act(() => result.current.handleCommandChange('C 5 5'))
      await act(async () => { await result.current.handleExecute() })

      act(() => result.current.handleCommandChange('L 1 1 5 1'))
      await act(async () => { await result.current.handleExecute() })

      // Then
      expect(result.current.history).toHaveLength(2)
      expect(result.current.history.every(e => e.status === 'OK')).toBe(true)
    })

    it('Given loading starts, When execute is called, Then loading is false after completion', async () => {
      // Given
      mockedExecute.mockResolvedValue({ canvas: 'c', success: true })
      const { result } = renderHook(() => useCanvas())
      act(() => result.current.handleCommandChange('C 1 1'))

      // When
      await act(async () => { await result.current.handleExecute() })

      // Then — loading resets to false after finally block
      expect(result.current.loading).toBe(false)
    })
  })

  // ── handleExecute — error path ───────────────────────────────────

  describe('handleExecute — API error', () => {

    it('Given API returns an error response, When executed, Then error message is set', async () => {
      // Given
      mockedExecute.mockRejectedValue({
        response: { data: { message: 'Canvas not created' } },
      })
      const { result } = renderHook(() => useCanvas())
      act(() => result.current.handleCommandChange('L 1 1 5 1'))

      // When
      await act(async () => { await result.current.handleExecute() })

      // Then
      expect(result.current.error).toBe('Canvas not created')
      expect(result.current.history[0]).toMatchObject({ status: 'ERROR' })
    })

    it('Given API throws a generic Error, When executed, Then error.message is shown', async () => {
      // Given
      mockedExecute.mockRejectedValue(new Error('Network Error'))
      const { result } = renderHook(() => useCanvas())
      act(() => result.current.handleCommandChange('C 5 5'))

      // When
      await act(async () => { await result.current.handleExecute() })

      // Then
      expect(result.current.error).toBe('Network Error')
      expect(result.current.loading).toBe(false)
    })
  })

  // ── clearHistory ─────────────────────────────────────────────────

  describe('clearHistory', () => {

    it('Given history has entries, When clearHistory is called, Then history becomes empty', async () => {
      // Given
      mockedExecute.mockResolvedValue({ canvas: 'c', success: true })
      const { result } = renderHook(() => useCanvas())
      act(() => result.current.handleCommandChange('C 1 1'))
      await act(async () => { await result.current.handleExecute() })
      expect(result.current.history).toHaveLength(1)

      // When
      act(() => result.current.clearHistory())

      // Then
      expect(result.current.history).toHaveLength(0)
    })
  })
})
