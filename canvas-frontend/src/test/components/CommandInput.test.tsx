import { describe, it, expect, vi } from 'vitest'
import { render, screen, fireEvent } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import CommandInput from '../../components/CommandInput'

/**
 * Tests for CommandInput component.
 *
 * Pattern:
 *   Given  → set up props (command, error, loading, callbacks)
 *   When   → render and interact
 *   Then   → assert rendered output or callback calls
 */
describe('CommandInput', () => {

  const defaultProps = {
    command: '',
    error: '',
    loading: false,
    onChange: vi.fn(),
    onExecute: vi.fn(),
  }

  // ── Rendering ─────────────────────────────────────────────────

  describe('rendering', () => {

    it('Given default props, When rendered, Then Execute button is visible', () => {
      // Given / When
      render(<CommandInput {...defaultProps} />)

      // Then
      expect(screen.getByRole('button', { name: /execute/i })).toBeInTheDocument()
    })

    it('Given default props, When rendered, Then text field with placeholder is visible', () => {
      // Given / When
      render(<CommandInput {...defaultProps} />)

      // Then
      expect(screen.getByPlaceholderText(/e\.g\. C 20 10/i)).toBeInTheDocument()
    })

    it('Given default props, When rendered, Then all 5 quick-command chips are shown', () => {
      // Given / When
      render(<CommandInput {...defaultProps} />)

      // Then
      expect(screen.getByText('C 20 10')).toBeInTheDocument()
      expect(screen.getByText('L 1 2 6 2')).toBeInTheDocument()
      expect(screen.getByText('R 14 1 18 3')).toBeInTheDocument()
      expect(screen.getByText('B 10 3 o')).toBeInTheDocument()
      expect(screen.getByText('Q')).toBeInTheDocument()
    })

    it('Given an error message, When rendered, Then error text is displayed', () => {
      // Given
      const props = { ...defaultProps, error: 'Usage: C <width> <height>' }

      // When
      render(<CommandInput {...props} />)

      // Then
      expect(screen.getByText('Usage: C <width> <height>')).toBeInTheDocument()
    })

    it('Given loading=true, When rendered, Then Execute button is disabled', () => {
      // Given
      const props = { ...defaultProps, loading: true }

      // When
      render(<CommandInput {...props} />)

      // Then
      expect(screen.getByRole('button', { name: /execute/i })).toBeDisabled()
    })

    it('Given loading=false, When rendered, Then Execute button is enabled', () => {
      // Given / When
      render(<CommandInput {...defaultProps} />)

      // Then
      expect(screen.getByRole('button', { name: /execute/i })).not.toBeDisabled()
    })

    it('Given a command value, When rendered, Then text field shows that value', () => {
      // Given
      const props = { ...defaultProps, command: 'C 10 5' }

      // When
      render(<CommandInput {...props} />)

      // Then
      expect(screen.getByDisplayValue('C 10 5')).toBeInTheDocument()
    })
  })

  // ── User interactions ─────────────────────────────────────────

  describe('interactions', () => {

    it('Given the text field, When user types, Then onChange is called with new value', async () => {
      // Given
      const onChange = vi.fn()
      render(<CommandInput {...defaultProps} onChange={onChange} />)
      const input = screen.getByPlaceholderText(/e\.g\. C 20 10/i)

      // When
      await userEvent.type(input, 'C')

      // Then
      expect(onChange).toHaveBeenCalled()
    })

    it('Given Execute button, When clicked, Then onExecute is called', async () => {
      // Given
      const onExecute = vi.fn()
      render(<CommandInput {...defaultProps} onExecute={onExecute} />)

      // When
      await userEvent.click(screen.getByRole('button', { name: /execute/i }))

      // Then
      expect(onExecute).toHaveBeenCalledOnce()
    })

    it('Given the text field, When Enter key is pressed, Then onExecute is called', () => {
      // Given
      const onExecute = vi.fn()
      render(<CommandInput {...defaultProps} onExecute={onExecute} />)
      const input = screen.getByPlaceholderText(/e\.g\. C 20 10/i)

      // When
      fireEvent.keyDown(input, { key: 'Enter' })

      // Then
      expect(onExecute).toHaveBeenCalledOnce()
    })

    it('Given the text field, When non-Enter key is pressed, Then onExecute is NOT called', () => {
      // Given
      const onExecute = vi.fn()
      render(<CommandInput {...defaultProps} onExecute={onExecute} />)
      const input = screen.getByPlaceholderText(/e\.g\. C 20 10/i)

      // When
      fireEvent.keyDown(input, { key: 'a' })

      // Then
      expect(onExecute).not.toHaveBeenCalled()
    })

    it('Given a quick command chip, When clicked, Then onChange is called with chip value', async () => {
      // Given
      const onChange = vi.fn()
      render(<CommandInput {...defaultProps} onChange={onChange} />)

      // When
      await userEvent.click(screen.getByText('C 20 10'))

      // Then
      expect(onChange).toHaveBeenCalledWith('C 20 10')
    })
  })
})
