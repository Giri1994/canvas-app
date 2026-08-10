import { useEffect, useState } from 'react'
import { AppBar, Toolbar, Typography, Box } from '@mui/material'
import axios from 'axios'

export default function CanvasHeader() {
  const [connected, setConnected] = useState<boolean | null>(null)

  useEffect(() => {
    const check = async () => {
      try {
        await axios.get('/actuator/health', { timeout: 3000 })
        setConnected(true)
      } catch {
        setConnected(false)
      }
    }
    check()
    const interval = setInterval(check, 10000)
    return () => clearInterval(interval)
  }, [])

  const statusColor =
    connected === null ? '#facc15' :
    connected          ? '#22c55e' :
                         '#ef4444'

  const statusLabel =
    connected === null ? 'Checking...' :
    connected          ? '' :
                         ''

  return (
    <AppBar position="static" sx={{ bgcolor: '#1a2332' }}>
      <Toolbar sx={{ position: 'relative' }}>

        {/* Truly centered title */}
        <Typography
          variant="h6"
          sx={{
            position: 'absolute',
            left: '50%',
            transform: 'translateX(-50%)',
            fontWeight: 700,
            whiteSpace: 'nowrap',
          }}
        >
          🎨 Canvas Drawing Application
        </Typography>

        {/* Status dot pushed to the right */}
        <Box sx={{ ml: 'auto', display: 'flex', alignItems: 'center', gap: 1 }}>
          <Box
            sx={{
              width: 11,
              height: 11,
              borderRadius: '50%',
              bgcolor: statusColor,
              boxShadow: `0 0 6px ${statusColor}`,
            }}
          />
          <Typography variant="body2" sx={{ color: statusColor, fontWeight: 500 }}>
            {statusLabel}
          </Typography>
        </Box>

      </Toolbar>
    </AppBar>
  )
}
