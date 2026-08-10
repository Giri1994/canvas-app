import { describe, it, expect } from 'vitest'
import { render, screen } from '@testing-library/react'
import CanvasLegend from '../../components/CanvasLegend'

/**
 * Tests for CanvasLegend component.
 *
 * Pattern:
 *   Given  → component with no props
 *   When   → rendered
 *   Then   → assert all legend items are present
 */
describe('CanvasLegend', () => {

  it('Given the legend, When rendered, Then the "Legend" title is visible', () => {
    // Given / When
    render(<CanvasLegend />)

    // Then
    expect(screen.getByText('Legend')).toBeInTheDocument()
  })

  it('Given the legend, When rendered, Then all 5 command examples are shown', () => {
    // Given / When
    render(<CanvasLegend />)

    // Then
    expect(screen.getByText('C 20 4')).toBeInTheDocument()
    expect(screen.getByText('L 1 2 6 2')).toBeInTheDocument()
    expect(screen.getByText('R 2 2 8 8')).toBeInTheDocument()
    expect(screen.getByText('B 5 5 o')).toBeInTheDocument()
    expect(screen.getByText('Q')).toBeInTheDocument()
  })

  it('Given the legend, When rendered, Then command descriptions are visible', () => {
    // Given / When
    render(<CanvasLegend />)

    // Then
    expect(screen.getByText(/Create canvas/i)).toBeInTheDocument()
    expect(screen.getByText(/Draw line/i)).toBeInTheDocument()
    expect(screen.getByText(/Draw rectangle/i)).toBeInTheDocument()
    expect(screen.getByText(/Bucket fill/i)).toBeInTheDocument()
    expect(screen.getByText(/Quit/i)).toBeInTheDocument()
  })
})
