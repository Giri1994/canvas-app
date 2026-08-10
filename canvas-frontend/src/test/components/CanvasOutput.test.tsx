import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import CanvasOutput from '../../components/CanvasOutput'

/**
 * Tests for CanvasOutput component.
 *
 * Pattern:
 *   Given  → provide canvasText prop
 *   When   → render the component
 *   Then   → assert the displayed content
 */
describe('CanvasOutput', () => {

  // ── Empty / placeholder state ──────────────────────────────────

  describe('when canvasText is empty', () => {

    it('Given no canvas text, When rendered, Then shows placeholder hint', () => {
      // Given / When
      render(<CanvasOutput canvasText="" />)

      // Then
      expect(screen.getByText(/Canvas will appear here/i)).toBeInTheDocument()
      expect(screen.getByText(/C <width> <height>/i)).toBeInTheDocument()
    })

    it('Given no canvas text, When rendered, Then title shows "Canvas Output"', () => {
      // Given / When
      render(<CanvasOutput canvasText="" />)

      // Then
      expect(screen.getByText('Canvas Output')).toBeInTheDocument()
    })
  })

  // ── Canvas with content ───────────────────────────────────────

  describe('when canvasText has content', () => {

    it('Given a canvas string, When rendered, Then canvas text is displayed', () => {
      // Given
      const canvasText = '------\n|    |\n|    |\n------'

      // When
      render(<CanvasOutput canvasText={canvasText} />)

      // Then
      expect(screen.getByText(/------/)).toBeInTheDocument()
    })

    it('Given a 4x2 canvas, When rendered, Then title shows correct dimensions', () => {
      // Given — 4-wide, 2-tall canvas: border=6 dashes, 2 inner rows
      const canvasText = '------\n|    |\n|    |\n------'

      // When
      render(<CanvasOutput canvasText={canvasText} />)

      // Then — title should read "Canvas (4 x 2)"
      expect(screen.getByText('Canvas (4 x 2)')).toBeInTheDocument()
    })

    it('Given a 20x10 canvas, When rendered, Then title shows correct dimensions', () => {
      // Given — 20-wide, 10-tall: top border = 22 dashes, 10 inner rows
      const topBorder   = '-'.repeat(22)
      const innerRow    = '|' + ' '.repeat(20) + '|'
      const canvasText  = [topBorder, ...Array(10).fill(innerRow), topBorder].join('\n')

      // When
      render(<CanvasOutput canvasText={canvasText} />)

      // Then
      expect(screen.getByText('Canvas (20 x 10)')).toBeInTheDocument()
    })
  })
})
