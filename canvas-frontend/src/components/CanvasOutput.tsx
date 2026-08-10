import { Box, Typography, Paper } from '@mui/material'

interface CanvasOutputProps {
  canvasText: string
}

function getCanvasDimensions(text: string): string {
  if (!text) return ''
  const lines = text.split('\n').filter(l => l.length > 0)
  if (lines.length < 2) return ''
  const width = lines[0].length - 2
  const height = lines.length - 2
  return `Canvas (${width} x ${height})`
}

export default function CanvasOutput({ canvasText }: CanvasOutputProps) {
  const dimensions = getCanvasDimensions(canvasText)

  return (
    <Paper elevation={2} sx={{ p: 2.5 }}>
      <Typography variant="subtitle1" color="primary" sx={{ fontWeight: 600, mb: 1.5 }}>
        {dimensions || 'Canvas Output'}
      </Typography>
      <Box
        component="pre"
        sx={{
          fontFamily: '"Courier New", Courier, monospace',
          fontSize: { xs: '0.7rem', sm: '0.78rem', md: '0.85rem' },
          lineHeight: 1.4,
          bgcolor: '#f8f9fa',
          border: '1.5px dashed #b0b8c4',
          borderRadius: 1.5,
          p: { xs: 1.5, md: 2.5 },
          /* Large fixed height so the canvas has room to breathe */
          minHeight: { xs: 300, sm: 420, md: 520 },
          overflowX: 'auto',
          overflowY: 'auto',
          whiteSpace: 'pre',
          m: 0,
          color: '#1a1a2e',
        }}
      >
        {canvasText ||
          '// Canvas will appear here.\n// Start with:  C <width> <height>\n// Example:      C 20 10'}
      </Box>
    </Paper>
  )
}
