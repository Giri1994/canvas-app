import { describe, it, expect } from 'vitest'
import { validateCommand } from '../utils/canvasValidation'

/**
 * Tests for validateCommand utility.
 *
 * Pattern:
 *   Given  → the command string input
 *   When   → validateCommand(cmd) is called
 *   Then   → null (valid) or an error message string (invalid)
 */
describe('validateCommand', () => {

  // ── Valid commands — expect null (no error) ──────────────────────

  describe('valid commands', () => {

    it('Given a valid C command, When validated, Then returns null', () => {
      // Given
      const cmd = 'C 20 10'
      // When
      const result = validateCommand(cmd)
      // Then
      expect(result).toBeNull()
    })

    it('Given a valid L command, When validated, Then returns null', () => {
      expect(validateCommand('L 1 1 10 1')).toBeNull()
    })

    it('Given a valid R command, When validated, Then returns null', () => {
      expect(validateCommand('R 2 2 8 8')).toBeNull()
    })

    it('Given a valid B command, When validated, Then returns null', () => {
      expect(validateCommand('B 5 5 o')).toBeNull()
    })

    it('Given a Q command (no args), When validated, Then returns null', () => {
      expect(validateCommand('Q')).toBeNull()
    })

    it('Given lowercase command letter, When validated, Then returns null (case-insensitive)', () => {
      expect(validateCommand('c 20 10')).toBeNull()
      expect(validateCommand('l 1 1 10 1')).toBeNull()
      expect(validateCommand('r 2 2 8 8')).toBeNull()
      expect(validateCommand('b 5 5 o')).toBeNull()
      expect(validateCommand('q')).toBeNull()
    })

    it('Given extra whitespace around command, When validated, Then returns null', () => {
      expect(validateCommand('  C 20 10  ')).toBeNull()
    })
  })

  // ── Unknown command ───────────────────────────────────────────────

  describe('unknown command', () => {

    it('Given command "X", When validated, Then returns unknown command error', () => {
      // Given
      const cmd = 'X 1 2'
      // When
      const result = validateCommand(cmd)
      // Then
      expect(result).toMatch(/Unknown command/)
      expect(result).toContain('X')
    })

    it('Given empty string, When validated, Then returns unknown command error', () => {
      const result = validateCommand('   ')
      expect(result).toMatch(/Unknown command/)
    })
  })

  // ── Negative / zero coordinates ───────────────────────────────────

  describe('negative or zero coordinates', () => {

    it('Given C command with negative width, When validated, Then returns coordinate error', () => {
      // Given
      const cmd = 'C -5 10'
      // When / Then
      expect(validateCommand(cmd)).toMatch(/Coordinates must be >= 1/)
    })

    it('Given C command with zero width, When validated, Then returns coordinate error', () => {
      expect(validateCommand('C 0 10')).toMatch(/Coordinates must be >= 1/)
    })

    it('Given L command with negative y1, When validated, Then returns coordinate error', () => {
      expect(validateCommand('L 1 -1 10 1')).toMatch(/Coordinates must be >= 1/)
    })

    it('Given R command with negative x1, When validated, Then returns coordinate error', () => {
      expect(validateCommand('R -2 2 8 8')).toMatch(/Coordinates must be >= 1/)
    })

    it('Given B command with zero x coordinate, When validated, Then returns coordinate error', () => {
      expect(validateCommand('B 0 5 o')).toMatch(/Coordinates must be >= 1/)
    })
  })

  // ── Wrong argument count ──────────────────────────────────────────

  describe('wrong argument count', () => {

    it('Given C command with only one arg, When validated, Then returns usage hint', () => {
      // Given
      const cmd = 'C 20'
      // When
      const result = validateCommand(cmd)
      // Then
      expect(result).toMatch(/Usage: C/)
    })

    it('Given C command with too many args, When validated, Then returns usage hint', () => {
      expect(validateCommand('C 20 10 extra')).toMatch(/Usage: C/)
    })

    it('Given L command with only 3 coords, When validated, Then returns usage hint', () => {
      expect(validateCommand('L 1 2 3')).toMatch(/Usage: L/)
    })

    it('Given L command with too many coords, When validated, Then returns usage hint', () => {
      expect(validateCommand('L 1 2 3 4 5')).toMatch(/Usage: L/)
    })

    it('Given R command with missing coord, When validated, Then returns usage hint', () => {
      expect(validateCommand('R 1 2 3')).toMatch(/Usage: R/)
    })

    it('Given B command without color, When validated, Then returns usage hint', () => {
      expect(validateCommand('B 5 5')).toMatch(/Usage: B/)
    })

    it('Given B command with too many args, When validated, Then returns usage hint', () => {
      expect(validateCommand('B 5 5 o extra')).toMatch(/Usage: B/)
    })
  })
})
