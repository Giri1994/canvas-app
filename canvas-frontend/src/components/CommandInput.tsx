import {
  Box, Paper, Typography, TextField, Button, Chip, CircularProgress
} from '@mui/material'
import PlayArrowIcon from '@mui/icons-material/PlayArrow'

const QUICK_COMMANDS = [
  { label: 'C 20 10',        value: 'C 20 10' },
  { label: 'L x1 y1 x2 y2', value: 'L 1 2 6 2' },
  { label: 'R x1 y1 x2 y2', value: 'R 14 1 18 3' },
  { label: 'B x y c',        value: 'B 10 3 o' },
  { label: 'Q',               value: 'Q' },
]

interface CommandInputProps {
  command: string
  error: string
  loading: boolean
  onChange: (value: string) => void
  onExecute: () => void
}

export default function CommandInput({
  command, error, loading, onChange, onExecute
}: CommandInputProps) {

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter') onExecute()
  }

  return (
    <Paper sx={{ p: 2, mb: 2 }}>
      <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>Command</Typography>

      {/* Wrapper keeps input + button on ONE row always */}
      <Box sx={{ display: 'flex', alignItems: 'flex-start', gap: 1, width: '100%' }}>
        <Box sx={{ flex: 1, minWidth: 0 }}>
          {/* minWidth:0 lets flex child shrink below its content size */}
          <TextField
            fullWidth
            size="small"
            placeholder="e.g. C 20 10"
            value={command}
            onChange={e => onChange(e.target.value)}
            onKeyDown={handleKeyDown}
            error={!!error}
            helperText={error || ' '}
            slotProps={{ htmlInput: { 'aria-label': 'Drawing command' } }}
          />
        </Box>

        <Button
          variant="contained"
          onClick={onExecute}
          disabled={loading}
          startIcon={loading ? <CircularProgress size={16} color="inherit" /> : <PlayArrowIcon />}
          sx={{
            whiteSpace: 'nowrap',
            flexShrink: 0,        // never shrink — always stays visible
            height: 40,
            mt: '1px',
            bgcolor: '#1565c0',
            '&:hover': { bgcolor: '#1976d2' },
          }}
        >
          Execute
        </Button>
      </Box>

      <Box sx={{ mt: 0.5 }}>
        <Typography variant="caption" color="text.secondary">Quick Commands</Typography>
        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap', mt: 0.5 }}>
          {QUICK_COMMANDS.map(qc => (
            <Chip
              key={qc.label}
              label={qc.value}
              variant="outlined"
              size="small"
              clickable
              onClick={() => onChange(qc.value)}
              sx={{ fontFamily: 'monospace', fontSize: '0.75rem' }}
            />
          ))}
        </Box>
      </Box>
    </Paper>
  )
}

