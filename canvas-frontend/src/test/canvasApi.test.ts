import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from 'axios'
import { executeCommand } from '../api/canvasApi'

// Mock the entire axios module — no real HTTP calls in unit tests
vi.mock('axios')

const mockedPost = vi.mocked(axios.post)

/**
 * Tests for canvasApi.executeCommand
 *
 * Strategy:
 *   - Mock axios.post so no real HTTP calls are made
 *   - Verify the correct endpoint and payload are sent for each command
 *   - Verify the returned data is forwarded as-is
 *
 * Pattern:
 *   Given  → stub axios.post to return a canned response
 *   When   → call executeCommand(cmd)
 *   Then   → assert correct URL + payload + return value
 */
describe('executeCommand', () => {

  const mockResponse = { canvas: '---\n| |\n---', message: 'OK', success: true }

  beforeEach(() => {
    // Reset call history before each test
    vi.clearAllMocks()
    mockedPost.mockResolvedValue({ data: mockResponse })
  })

  // ── C — Create canvas ─────────────────────────────────────────────

  describe('C command', () => {

    it('Given "C 20 10", When executed, Then calls /create with width and height', async () => {
      // Given
      const cmd = 'C 20 10'

      // When
      const result = await executeCommand(cmd)

      // Then
      expect(mockedPost).toHaveBeenCalledOnce()
      expect(mockedPost).toHaveBeenCalledWith('/api/canvas/create', { width: 20, height: 10 })
      expect(result).toEqual(mockResponse)
    })

    it('Given "C 1 1", When executed, Then passes numeric values (not strings)', async () => {
      await executeCommand('C 1 1')

      const [, payload] = mockedPost.mock.calls[0]
      expect(typeof (payload as { width: number }).width).toBe('number')
      expect(typeof (payload as { height: number }).height).toBe('number')
    })
  })

  // ── L — Draw line ──────────────────────────────────────────────────

  describe('L command', () => {

    it('Given "L 1 2 6 2", When executed, Then calls /line with x1 y1 x2 y2', async () => {
      // Given
      const cmd = 'L 1 2 6 2'

      // When
      await executeCommand(cmd)

      // Then
      expect(mockedPost).toHaveBeenCalledWith('/api/canvas/line', {
        x1: 1, y1: 2, x2: 6, y2: 2,
      })
    })
  })

  // ── R — Draw rectangle ─────────────────────────────────────────────

  describe('R command', () => {

    it('Given "R 14 1 18 3", When executed, Then calls /rectangle with x1 y1 x2 y2', async () => {
      // Given
      const cmd = 'R 14 1 18 3'

      // When
      await executeCommand(cmd)

      // Then
      expect(mockedPost).toHaveBeenCalledWith('/api/canvas/rectangle', {
        x1: 14, y1: 1, x2: 18, y2: 3,
      })
    })
  })

  // ── B — Bucket fill ────────────────────────────────────────────────

  describe('B command', () => {

    it('Given "B 10 3 o", When executed, Then calls /fill with x y color', async () => {
      // Given
      const cmd = 'B 10 3 o'

      // When
      await executeCommand(cmd)

      // Then
      expect(mockedPost).toHaveBeenCalledWith('/api/canvas/fill', {
        x: 10, y: 3, color: 'o',
      })
    })

    it('Given "B 1 1 *", When executed, Then passes color as string', async () => {
      await executeCommand('B 1 1 *')

      const [, payload] = mockedPost.mock.calls[0]
      expect((payload as { color: string }).color).toBe('*')
    })
  })

  // ── Q — Quit ────────────────────────────────────────────────────────

  describe('Q command', () => {

    it('Given "Q", When executed, Then calls /quit with no body', async () => {
      // Given
      const cmd = 'Q'

      // When
      await executeCommand(cmd)

      // Then
      expect(mockedPost).toHaveBeenCalledWith('/api/canvas/quit')
      expect(mockedPost).toHaveBeenCalledOnce()
    })
  })

  // ── Unknown command ─────────────────────────────────────────────────

  describe('unknown command', () => {

    it('Given an unknown command, When executed, Then throws an error', async () => {
      // Given
      const cmd = 'X 1 2'

      // When / Then
      await expect(executeCommand(cmd)).rejects.toThrow('Unknown command')
      expect(mockedPost).not.toHaveBeenCalled()
    })
  })

  // ── API error propagation ──────────────────────────────────────────

  describe('API error handling', () => {

    it('Given axios rejects, When executed, Then the error propagates to the caller', async () => {
      // Given
      mockedPost.mockRejectedValue(new Error('Network Error'))

      // When / Then
      await expect(executeCommand('C 5 5')).rejects.toThrow('Network Error')
    })
  })
})
