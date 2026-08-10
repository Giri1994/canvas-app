import {
  Box, Paper, Typography, Divider, List, ListItem, ListItemText,
  Chip, Button
} from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import type { HistoryEntry } from '../types/canvas.types'

interface CommandHistoryProps {
  history: HistoryEntry[]
  onClear: () => void
}

/**
 * Displays list of past commands with OK / ERROR status chips.
 * Receives history array and clear handler from parent (App.tsx).
 */
export default function CommandHistory({ history, onClear }: CommandHistoryProps) {
  return (
    <Paper sx={{ p: 2, mb: 2 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 1 }}>
        <Typography variant="subtitle2" color="primary">Command History</Typography>
        <Button
          size="small"
          startIcon={<DeleteIcon />}
          onClick={onClear}
          disabled={history.length === 0}
        >
          Clear History
        </Button>
      </Box>
      <Divider sx={{ mb: 1 }} />

      {history.length === 0 ? (
        <Typography variant="caption" color="text.secondary">No commands yet</Typography>
      ) : (
        <List dense disablePadding>
          {history.map(entry => (
            <ListItem key={entry.id} disablePadding sx={{ py: 0.5 }}>
              <Typography
                variant="body2"
                sx={{ mr: 1, color: 'text.secondary', minWidth: 22, fontWeight: 600 }}
              >
                {entry.id}
              </Typography>
              <ListItemText
                primary={<Typography component="span" sx={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>{entry.command}</Typography>}
              />
              <Chip
                label={entry.status}
                size="small"
                color={entry.status === 'OK' ? 'success' : 'error'}
                sx={{ fontSize: '0.7rem', ml: 1 }}
              />
            </ListItem>
          ))}
        </List>
      )}
    </Paper>
  )
}
