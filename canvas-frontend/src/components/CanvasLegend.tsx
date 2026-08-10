import { Box, Paper, Typography, Divider } from '@mui/material'

const LEGEND = [
  { cmd: 'C 20 4',          desc: 'Create canvas of width w and height h' },
  { cmd: 'L 1 2 6 2', desc: 'Draw line from (x1,y1) to (x2,y2)' },
  { cmd: 'R 2 2 8 8', desc: 'Draw rectangle' },
  { cmd: 'B 5 5 o',        desc: 'Bucket fill with color c starting at (x, y)' },
  { cmd: 'Q',              desc: 'Quit / reset canvas' },
]

export default function CanvasLegend() {
  return (
    <Paper sx={{ p: 2 }}>
      <Typography variant="subtitle2" color="primary" sx={{ mb: 1 }}>Legend</Typography>
      <Divider sx={{ mb: 1 }} />
      {LEGEND.map(item => (
        <Box key={item.cmd} sx={{ display: 'flex', gap: 1, mb: 0.75, alignItems: 'flex-start' }}>
          <Typography
            variant="caption"
            sx={{ fontFamily: 'monospace', fontWeight: 700, minWidth: 120 }}
          >
            {item.cmd}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            : {item.desc}
          </Typography>
        </Box>
      ))}
    </Paper>
  )
}
