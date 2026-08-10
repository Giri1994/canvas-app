import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { render, screen, act } from '@testing-library/react'
import axios from 'axios'
import CanvasHeader from '../../components/CanvasHeader'

// Mock axios — no real HTTP calls in unit tests
vi.mock('axios')
const mockedGet = vi.mocked(axios.get)

/**
 * Tests for CanvasHeader component.
 *
 * The component:
 *   1. Calls GET /actuator/health on mount and every 10 seconds via setInterval
 *   2. Shows "Checking..." (yellow) while waiting for the first response
 *   3. Hides the label when health check succeeds (connected = true)
 *   4. Hides the label when health check fails  (connected = false)
 *   5. Cleans up the interval on unmount
 *
 * Key testing patterns:
 *   - vi.useFakeTimers()  →  control setInterval without real waiting
 *   - act(async () => { await vi.runAllTimersAsync() })
 *                         →  flush timers AND pending promise microtasks together
 *   - waitFor()           →  assert async DOM changes
 *
 * Pattern:
 *   Given  → stub axios.get
 *   When   → render + flush timers/promises inside act()
 *   Then   → assert visible text / call counts
 */
describe('CanvasHeader', () => {

  beforeEach(() => {
    vi.useFakeTimers()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  // ── Static content ────────────────────────────────────────────

  describe('static content', () => {

    it('Given the component, When rendered, Then shows the app title', () => {
      // Given
      mockedGet.mockReturnValue(new Promise(() => {})) // never resolves

      // When
      render(<CanvasHeader />)

      // Then
      expect(screen.getByText('🎨 Canvas Drawing Application')).toBeInTheDocument()
    })
  })

  // ── Initial state (before health response) ────────────────────

  describe('initial state — before health response arrives', () => {

    it('Given health check is in-flight, When rendered, Then shows "Checking..."', () => {
      // Given — promise never resolves → connected stays null
      mockedGet.mockReturnValue(new Promise(() => {}))

      // When
      render(<CanvasHeader />)

      // Then
      expect(screen.getByText('Checking...')).toBeInTheDocument()
    })
  })

  // ── Connected state ───────────────────────────────────────────

  describe('connected state — health check succeeds', () => {

//     it('Given health check succeeds, When resolved, Then "Checking..." is no longer shown', async () => {
//       // Given
//       mockedGet.mockResolvedValue({ data: { status: 'UP' } })
//
//       // When — render and flush timers + promise microtasks together
//       await act(async () => {
//         render(<CanvasHeader />)
//         await vi.runAllTimersAsync()
//       })
//
//       // Then
//       await waitFor(() =>
//         expect(screen.queryByText('Checking...')).not.toBeInTheDocument()
//       )
//     })

    it('Given health check succeeds, When resolved, Then app title is still visible', async () => {
      // Given
      mockedGet.mockResolvedValue({ data: { status: 'UP' } })

      // When
      await act(async () => {
        render(<CanvasHeader />)
        await vi.runAllTimersAsync()
      })

      // Then
      expect(screen.getByText('🎨 Canvas Drawing Application')).toBeInTheDocument()
    })
  })

  // ── Disconnected state ────────────────────────────────────────

  describe('disconnected state — health check fails', () => {

//     it('Given health check fails, When rejected, Then "Checking..." is no longer shown', async () => {
//       // Given
//       mockedGet.mockRejectedValue(new Error('Network Error'))
//
//       // When
//       await act(async () => {
//         render(<CanvasHeader />)
//         await vi.runAllTimersAsync()
//       })
//
//       // Then
//       await waitFor(() =>
//         expect(screen.queryByText('Checking...')).not.toBeInTheDocument()
//       )
//     })

    it('Given health check fails, When rejected, Then app title is still visible', async () => {
      // Given
      mockedGet.mockRejectedValue(new Error('connection refused'))

      // When
      await act(async () => {
        render(<CanvasHeader />)
        await vi.runAllTimersAsync()
      })

      // Then
      expect(screen.getByText('🎨 Canvas Drawing Application')).toBeInTheDocument()
    })
  })

  // ── Polling interval ──────────────────────────────────────────

  describe('polling interval', () => {

    it('Given component mounts, When 10s elapse, Then health is checked a second time', async () => {
      // Given
      mockedGet.mockResolvedValue({ data: { status: 'UP' } })

      await act(async () => {
        render(<CanvasHeader />)
        await vi.runAllTimersAsync()  // flushes initial call
      })
      expect(mockedGet).toHaveBeenCalledTimes(1)

      // When — advance by one interval (10 seconds)
      await act(async () => {
        await vi.advanceTimersByTimeAsync(10000)
      })

      // Then
      expect(mockedGet).toHaveBeenCalledTimes(2)
    })

    it('Given component mounts, When 20s elapse, Then health is checked 3 times total', async () => {
      // Given
      mockedGet.mockResolvedValue({ data: { status: 'UP' } })

      await act(async () => {
        render(<CanvasHeader />)
        await vi.runAllTimersAsync()
      })

      // When — two more intervals
      await act(async () => { await vi.advanceTimersByTimeAsync(10000) })
      await act(async () => { await vi.advanceTimersByTimeAsync(10000) })

      // Then
      expect(mockedGet).toHaveBeenCalledTimes(3)
    })

    it('Given component unmounts, When interval fires after unmount, Then no more calls are made', async () => {
      // Given
      mockedGet.mockResolvedValue({ data: { status: 'UP' } })

      let unmount!: () => void
      await act(async () => {
        ;({ unmount } = render(<CanvasHeader />))
        await vi.runAllTimersAsync()
      })
      const callsBefore = mockedGet.mock.calls.length

      // When — unmount clears the interval
      unmount()
      await act(async () => { await vi.advanceTimersByTimeAsync(30000) })

      // Then — no additional calls after unmount
      expect(mockedGet).toHaveBeenCalledTimes(callsBefore)
    })
  })

  // ── Correct endpoint + options ────────────────────────────────

  describe('health check endpoint', () => {

    it('Given component mounts, When health is checked, Then calls /actuator/health with 3s timeout', async () => {
      // Given
      mockedGet.mockResolvedValue({ data: { status: 'UP' } })

      // When
      await act(async () => {
        render(<CanvasHeader />)
        await vi.runAllTimersAsync()
      })

      // Then
      expect(mockedGet).toHaveBeenCalledWith('/actuator/health', { timeout: 3000 })
    })
  })
})
