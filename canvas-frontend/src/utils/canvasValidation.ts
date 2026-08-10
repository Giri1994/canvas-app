/**
 * Validates a canvas command string.
 * Rules:
 *  - Must start with a known command: C, L, R, B, Q
 *  - Negative numbers are NOT allowed in any argument
 *  - Correct number of arguments must be provided
 *
 * Returns an error message string, or null if valid.
 */
export function validateCommand(cmd: string): string | null {
  const parts = cmd.trim().split(/\s+/)
  const op = parts[0]?.toUpperCase()

  if (!['C', 'L', 'R', 'B', 'Q'].includes(op)) {
    return `Unknown command "${op}". Use C, L, R, B, or Q.`
  }

  // Reject any negative numbers
  const numericArgs = parts.slice(1).filter(p => /^-?\d+$/.test(p))
  for (const n of numericArgs) {
    if (Number(n) < 1) return `Coordinates must be >= 1, got:"${n}"`
  }

  if (op === 'C' && parts.length !== 3) return 'Usage: C <width> <height>'
  if (op === 'L' && parts.length !== 5) return 'Usage: L <x1> <y1> <x2> <y2>'
  if (op === 'R' && parts.length !== 5) return 'Usage: R <x1> <y1> <x2> <y2>'
  if (op === 'B' && parts.length !== 4) return 'Usage: B <x> <y> <color>'

  return null
}
