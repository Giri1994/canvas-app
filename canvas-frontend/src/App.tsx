import { Box, Container, Grid } from '@mui/material'
import { useCanvas } from './hooks/useCanvas'
import CanvasHeader from './components/CanvasHeader'
import CommandInput from './components/CommandInput'
import CanvasOutput from './components/CanvasOutput'
import CommandHistory from './components/CommandHistory'
import CanvasLegend from './components/CanvasLegend'

function App() {
  const { command, canvasText, history, loading, error,
          handleCommandChange, handleExecute, clearHistory } = useCanvas()

  return (
    <Box sx={{ minHeight: '100vh', bgcolor: '#eef1f5', display: 'flex', flexDirection: 'column' }}>
      <CanvasHeader />

      {/* Centred content area */}
      <Box sx={{ flex: 1, py: 4, px: 2 }}>
        <Container maxWidth="xl">
          {/* MUI v9 Grid — use `size` prop instead of deprecated `item xs/md/lg` */}
          <Grid container spacing={3} sx={{ alignItems: 'flex-start' }}>

            {/* ── LEFT: Command input + large canvas output ── */}
            <Grid size={{ xs: 12, md: 8 }}>
              <CommandInput
                command={command}
                error={error}
                loading={loading}
                onChange={handleCommandChange}
                onExecute={handleExecute}
              />
              <CanvasOutput canvasText={canvasText} />
            </Grid>

            {/* ── RIGHT: History + Legend stacked ── */}
            <Grid size={{ xs: 12, md: 4 }}>
              <CommandHistory history={history} onClear={clearHistory} />
              <CanvasLegend />
            </Grid>

          </Grid>
        </Container>
      </Box>
    </Box>
  )
}

export default App
