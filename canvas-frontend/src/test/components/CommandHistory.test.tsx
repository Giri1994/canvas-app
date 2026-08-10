import { describe, it, expect, vi } from 'vitest'
import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CommandHistory from '../../components/CommandHistory'
import type { HistoryEntry } from '../../types/canvas.types'

/**
 * Tests for CommandHistory component.
 *
 * Pattern:
 *   Given  → provide history array and onClear callback
 *   When   → render / interact
 *   Then   → assert rendered output or callback calls
 */
describe('CommandHistory', () => {

  const sampleHistory: HistoryEntry[] = [
    { id: 1, command: 'C 20 10', status: 'OK' },
    { id: 2, command: 'L 1 1 5 1', status: 'OK' },
    { id: 3, command: 'X bad', status: 'ERROR' },
  ]

  // ── Empty state ───────────────────────────────────────────────

  describe('when history is empty', () => {

    it('Given empty history, When rendered, Then shows "No commands yet"', () => {
      // Given / When
      render(<CommandHistory history={[]} onClear={vi.fn()} />)

      // Then
      expect(screen.getByText(/No commands yet/i)).toBeInTheDocument()
    })

    it('Given empty history, When rendered, Then "Clear History" button is disabled', () => {
      // Given / When
      render(<CommandHistory history={[]} onClear={vi.fn()} />)

      // Then
      expect(screen.getByRole('button', { name: /clear history/i })).toBeDisabled()
    })
  })

  // ── With entries ──────────────────────────────────────────────

  describe('when history has entries', () => {

    it('Given 3 entries, When rendered, Then all commands are displayed', () => {
      // Given / When
      render(<CommandHistory history={sampleHistory} onClear={vi.fn()} />)

      // Then
      expect(screen.getByText('C 20 10')).toBeInTheDocument()
      expect(screen.getByText('L 1 1 5 1')).toBeInTheDocument()
      expect(screen.getByText('X bad')).toBeInTheDocument()
    })

    it('Given entries with OK status, When rendered, Then "OK" chip is shown', () => {
      // Given / When
      render(<CommandHistory history={sampleHistory} onClear={vi.fn()} />)

      // Then — two OK chips
      const okChips = screen.getAllByText('OK')
      expect(okChips).toHaveLength(2)
    })

    it('Given an entry with ERROR status, When rendered, Then "ERROR" chip is shown', () => {
      // Given / When
      render(<CommandHistory history={sampleHistory} onClear={vi.fn()} />)

      // Then
      expect(screen.getByText('ERROR')).toBeInTheDocument()
    })

    it('Given history has entries, When rendered, Then "Clear History" button is enabled', () => {
      // Given / When
      render(<CommandHistory history={sampleHistory} onClear={vi.fn()} />)

      // Then
      expect(screen.getByRole('button', { name: /clear history/i })).not.toBeDisabled()
    })

    it('Given history has entries, When "Clear History" is clicked, Then onClear is called', async () => {
      // Given
      const onClear = vi.fn()
      render(<CommandHistory history={sampleHistory} onClear={onClear} />)

      // When
      await userEvent.click(screen.getByRole('button', { name: /clear history/i }))

      // Then
      expect(onClear).toHaveBeenCalledOnce()
    })

    it('Given entries, When rendered, Then sequential IDs are shown', () => {
      // Given / When
      render(<CommandHistory history={sampleHistory} onClear={vi.fn()} />)

      // Then
      expect(screen.getByText('1')).toBeInTheDocument()
      expect(screen.getByText('2')).toBeInTheDocument()
      expect(screen.getByText('3')).toBeInTheDocument()
    })
  })
})
