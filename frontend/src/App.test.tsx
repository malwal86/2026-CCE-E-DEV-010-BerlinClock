import { render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { describe, expect, it } from 'vitest'
import App from './App'
import type { Conversion } from './conversion/types'
import { server } from './test/server'

function conversion(id: number, time: string, seconds: Conversion['seconds']): Conversion {
  return { id, time, seconds, convertedAt: '2026-10-05T14:03:12Z' }
}

/** A fake API backed by an in-memory history, newest first. */
function givenTheApiHasHistory(...initial: Conversion[]) {
  const history = [...initial]
  server.use(
    http.get('/api/conversions', () => HttpResponse.json(history)),
    http.post('/api/conversions', async ({ request }) => {
      const { time } = (await request.json()) as { time: string }
      const created = conversion(history.length + 1, time, Number(time.slice(-2)) % 2 === 0 ? 'Y' : 'O')
      history.unshift(created)
      return HttpResponse.json(created, { status: 201 })
    }),
  )
}

function recentConversions() {
  return within(screen.getByRole('region', { name: 'Recent conversions' }))
}

describe('Converting a time', () => {
  it('shows the seconds lamp for the converted time', async () => {
    givenTheApiHasHistory()
    render(<App />)

    await userEvent.type(screen.getByLabelText('Time (HH:mm:ss)'), '00:00:00')
    await userEvent.click(screen.getByRole('button', { name: 'Convert' }))

    const result = within(await screen.findByRole('region', { name: 'Result' }))
    expect(result.getByText('00:00:00')).toBeInTheDocument()
    expect(result.getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'Y')
  })

  it('submits with the Enter key', async () => {
    givenTheApiHasHistory()
    render(<App />)

    await userEvent.type(screen.getByLabelText('Time (HH:mm:ss)'), '23:59:59{Enter}')

    const result = within(await screen.findByRole('region', { name: 'Result' }))
    expect(result.getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'O')
  })

  it('adds the conversion to the top of the recent conversions', async () => {
    givenTheApiHasHistory(conversion(1, '00:00:00', 'Y'))
    render(<App />)
    await recentConversions().findByText('00:00:00')

    await userEvent.type(screen.getByLabelText('Time (HH:mm:ss)'), '23:59:59{Enter}')

    await recentConversions().findByText('23:59:59')
    const entries = recentConversions().getAllByRole('listitem')
    expect(entries.map((entry) => within(entry).getByTestId('time').textContent)).toEqual(['23:59:59', '00:00:00'])
  })
})

describe('Recent conversions', () => {
  it('lists the saved conversions when the page opens, each with its seconds lamp', async () => {
    givenTheApiHasHistory(conversion(2, '23:59:59', 'O'), conversion(1, '00:00:00', 'Y'))
    render(<App />)

    await recentConversions().findByText('23:59:59')
    const [newest, oldest] = recentConversions().getAllByRole('listitem')
    expect(within(newest).getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'O')
    expect(within(oldest).getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'Y')
  })

  it('shows when each conversion was made', async () => {
    givenTheApiHasHistory(conversion(1, '00:00:00', 'Y'))
    render(<App />)

    const entry = (await recentConversions().findAllByRole('listitem'))[0]
    expect(within(entry).getByText((_, element) => element?.tagName === 'TIME' && element.getAttribute('datetime') === '2026-10-05T14:03:12Z')).toBeInTheDocument()
  })

  it('says so when there are no conversions yet', async () => {
    givenTheApiHasHistory()
    render(<App />)

    expect(await recentConversions().findByText('No conversions yet')).toBeInTheDocument()
  })
})
