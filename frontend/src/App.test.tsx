import { render, screen, waitForElementToBeRemoved, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { http, HttpResponse } from 'msw'
import { act } from 'react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'
import type { BerlinClockRows } from './clock/types'
import type { Conversion } from './conversion/types'
import { server } from './test/server'

/** What the real API answers for the times these tests convert (the rules live in the backend). */
const BERLIN_CLOCK: Record<string, Omit<BerlinClockRows, 'clock'>> = {
  '00:00:00': { seconds: 'Y', fiveHours: 'OOOO', singleHours: 'OOOO', fiveMinutes: 'OOOOOOOOOOO', singleMinutes: 'OOOO' },
  '23:59:59': { seconds: 'O', fiveHours: 'RRRR', singleHours: 'RRRO', fiveMinutes: 'YYRYYRYYRYY', singleMinutes: 'YYYY' },
  '16:35:00': { seconds: 'Y', fiveHours: 'RRRO', singleHours: 'ROOO', fiveMinutes: 'YYRYYRYOOOO', singleMinutes: 'OOOO' },
  '14:35:00': { seconds: 'Y', fiveHours: 'RROO', singleHours: 'RRRR', fiveMinutes: 'YYRYYRYOOOO', singleMinutes: 'OOOO' },
  '12:35:00': { seconds: 'Y', fiveHours: 'RROO', singleHours: 'RROO', fiveMinutes: 'YYRYYRYOOOO', singleMinutes: 'OOOO' },
  '12:34:00': { seconds: 'Y', fiveHours: 'RROO', singleHours: 'RROO', fiveMinutes: 'YYRYYROOOOO', singleMinutes: 'YYYY' },
  '12:32:00': { seconds: 'Y', fiveHours: 'RROO', singleHours: 'RROO', fiveMinutes: 'YYRYYROOOOO', singleMinutes: 'YYOO' },
  '16:50:06': { seconds: 'Y', fiveHours: 'RRRO', singleHours: 'ROOO', fiveMinutes: 'YYRYYRYYRYO', singleMinutes: 'OOOO' },
  '12:00:00': { seconds: 'Y', fiveHours: 'RROO', singleHours: 'RROO', fiveMinutes: 'OOOOOOOOOOO', singleMinutes: 'OOOO' },
  '11:37:01': { seconds: 'O', fiveHours: 'RROO', singleHours: 'ROOO', fiveMinutes: 'YYRYYRYOOOO', singleMinutes: 'YYOO' },
}

function conversion(id: number, time: string): Conversion {
  const rows = BERLIN_CLOCK[time]
  const clock = rows.seconds + rows.fiveHours + rows.singleHours + rows.fiveMinutes + rows.singleMinutes
  return { id, time, ...rows, clock, convertedAt: '2026-10-05T14:03:12Z' }
}

const lampsOf = (row: HTMLElement) => Array.from(row.children, (lamp) => lamp.getAttribute('data-lamp')).join('')

/** Times the fake API rejects, with the problem detail it answers. */
const rejections = new Map<string, string>()

/** How many times the fake API was asked to convert, to prove that opening a conversion saves nothing. */
let conversions = 0

/** How many times the fake API was asked to clear the history. */
let clears = 0

/** How many times the live clock asked the fake API for the time. */
let ticks = 0

/** What is down behind the fake API: nothing, only the database, or the whole backend. */
let outage: 'database' | 'backend' | undefined

/** The fake API's answer while something is down, if anything is. */
function outageAnswer(request: Request) {
  if (outage === 'backend') return HttpResponse.error()
  if (outage === 'database' && new URL(request.url).pathname.startsWith('/api/conversions')) {
    const detail = request.method === 'POST'
      ? 'History is temporarily unavailable. Your conversion was not saved.'
      : 'History is temporarily unavailable.'
    return HttpResponse.json(
      { title: 'History unavailable', status: 503, detail, instance: new URL(request.url).pathname },
      { status: 503, headers: { 'Content-Type': 'application/problem+json' } },
    )
  }
}

/** A fake API backed by an in-memory history, newest first. */
function givenTheApiHasHistory(...initial: Conversion[]) {
  const history = [...initial]
  rejections.clear()
  conversions = 0
  clears = 0
  ticks = 0
  outage = undefined
  server.use(
    http.all('*', ({ request }) => outageAnswer(request)),
    // The live clock: any time reads as 16:50:06's lamps, the page only needs an answer.
    http.get('/api/berlin-clock', ({ request }) => {
      ticks++
      const time = new URL(request.url).searchParams.get('time')
      const { id: _id, convertedAt: _convertedAt, ...reading } = conversion(0, '16:50:06')
      return HttpResponse.json({ ...reading, time })
    }),
    http.get('/api/conversions', () => HttpResponse.json(history)),
    http.delete('/api/conversions', () => {
      clears++
      history.length = 0
      return new HttpResponse(null, { status: 204 })
    }),
    http.get('/api/conversions/:id', ({ params }) => {
      const found = history.find((conversion) => conversion.id === Number(params.id))
      if (!found) {
        return HttpResponse.json(
          { title: 'Conversion not found', status: 404, detail: `Conversion ${params.id} not found`, instance: `/api/conversions/${params.id}` },
          { status: 404, headers: { 'Content-Type': 'application/problem+json' } },
        )
      }
      return HttpResponse.json(found)
    }),
    http.post('/api/conversions', async ({ request }) => {
      conversions++
      const { time } = (await request.json()) as { time: string }
      const detail = rejections.get(time)
      if (detail) {
        return HttpResponse.json(
          { title: 'Invalid time', status: 400, detail, instance: '/api/conversions' },
          { status: 400, headers: { 'Content-Type': 'application/problem+json' } },
        )
      }
      const created = conversion(history.length + 1, time)
      history.unshift(created)
      return HttpResponse.json(created, { status: 201 })
    }),
  )
}

function givenTheDatabaseIsDown() {
  outage = 'database'
}

function givenTheBackendIsDown() {
  outage = 'backend'
}

function givenEverythingIsBackUp() {
  outage = undefined
}

function givenTheApiRejects(time: string, detail: string) {
  rejections.set(time, detail)
}

function recentConversions() {
  return within(screen.getByRole('region', { name: 'Recent conversions' }))
}

function givenTheAddressIs(path: string) {
  window.history.replaceState(null, '', path)
}

beforeEach(() => givenTheAddressIs('/'))

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

  it('shows the five hours row for the converted time', async () => {
    givenTheApiHasHistory()
    render(<App />)

    await userEvent.type(screen.getByLabelText('Time (HH:mm:ss)'), '16:35:00{Enter}')

    const result = within(await screen.findByRole('region', { name: 'Result' }))
    expect(lampsOf(result.getByTestId('five-hours-row'))).toBe('RRRO')
  })

  it('shows the single hours row under the five hours row', async () => {
    givenTheApiHasHistory()
    render(<App />)

    await userEvent.type(screen.getByLabelText('Time (HH:mm:ss)'), '14:35:00{Enter}')

    const result = within(await screen.findByRole('region', { name: 'Result' }))
    expect(lampsOf(result.getByTestId('five-hours-row'))).toBe('RROO')
    expect(lampsOf(result.getByTestId('single-hours-row'))).toBe('RRRR')
  })

  it('shows the five minutes row under the single hours row', async () => {
    givenTheApiHasHistory()
    render(<App />)

    await userEvent.type(screen.getByLabelText('Time (HH:mm:ss)'), '12:35:00{Enter}')

    const result = within(await screen.findByRole('region', { name: 'Result' }))
    expect(lampsOf(result.getByTestId('five-minutes-row'))).toBe('YYRYYRYOOOO')
  })

  it('shows the single minutes row at the bottom, completing the clock', async () => {
    givenTheApiHasHistory()
    render(<App />)

    await userEvent.type(screen.getByLabelText('Time (HH:mm:ss)'), '12:34:00{Enter}')

    const result = within(await screen.findByRole('region', { name: 'Result' }))
    expect(lampsOf(result.getByTestId('single-minutes-row'))).toBe('YYYY')
  })

  it('shows the entire clock as an image labelled with its time, and as its 24-character code', async () => {
    givenTheApiHasHistory()
    render(<App />)

    await userEvent.type(screen.getByLabelText('Time (HH:mm:ss)'), '16:50:06{Enter}')

    const result = within(await screen.findByRole('region', { name: 'Result' }))
    expect(result.getByRole('img', { name: 'Berlin Clock showing 16:50:06' })).toBeInTheDocument()
    expect(result.getByText('YRRROROOOYYRYYRYYRYOOOOO')).toBeInTheDocument()
  })

  it('copies the code of the converted time', async () => {
    const user = userEvent.setup()
    givenTheApiHasHistory()
    render(<App />)
    await user.type(screen.getByLabelText('Time (HH:mm:ss)'), '16:50:06{Enter}')
    const result = within(await screen.findByRole('region', { name: 'Result' }))

    await user.click(result.getByRole('button', { name: 'Copy code for 16:50:06' }))

    expect(await navigator.clipboard.readText()).toBe('YRRROROOOYYRYYRYYRYOOOOO')
  })

  it('submits with the Enter key', async () => {
    givenTheApiHasHistory()
    render(<App />)

    await userEvent.type(screen.getByLabelText('Time (HH:mm:ss)'), '23:59:59{Enter}')

    const result = within(await screen.findByRole('region', { name: 'Result' }))
    expect(result.getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'O')
  })

  it('adds the conversion to the top of the recent conversions', async () => {
    givenTheApiHasHistory(conversion(1, '00:00:00'))
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
    givenTheApiHasHistory(conversion(2, '23:59:59'), conversion(1, '00:00:00'))
    render(<App />)

    await recentConversions().findByText('23:59:59')
    const [newest, oldest] = recentConversions().getAllByRole('listitem')
    expect(within(newest).getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'O')
    expect(within(oldest).getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'Y')
  })

  it('shows the five hours row of each conversion, including earlier ones', async () => {
    givenTheApiHasHistory(conversion(1, '23:59:59'))
    render(<App />)

    const entry = (await recentConversions().findAllByRole('listitem'))[0]
    expect(lampsOf(within(entry).getByTestId('five-hours-row'))).toBe('RRRR')
  })

  it('shows the single hours row of each conversion, including earlier ones', async () => {
    givenTheApiHasHistory(conversion(1, '23:59:59'))
    render(<App />)

    const entry = (await recentConversions().findAllByRole('listitem'))[0]
    expect(lampsOf(within(entry).getByTestId('single-hours-row'))).toBe('RRRO')
  })

  it('shows the five minutes row of each conversion, including earlier ones', async () => {
    givenTheApiHasHistory(conversion(1, '23:59:59'))
    render(<App />)

    const entry = (await recentConversions().findAllByRole('listitem'))[0]
    expect(lampsOf(within(entry).getByTestId('five-minutes-row'))).toBe('YYRYYRYYRYY')
  })

  it('shows the single minutes row of each conversion, including earlier ones', async () => {
    givenTheApiHasHistory(conversion(1, '12:32:00'))
    render(<App />)

    const entry = (await recentConversions().findAllByRole('listitem'))[0]
    expect(lampsOf(within(entry).getByTestId('single-minutes-row'))).toBe('YYOO')
  })

  it('shows the code of each conversion, including earlier ones, with a copy button', async () => {
    givenTheApiHasHistory(conversion(1, '11:37:01'))
    render(<App />)

    const entry = within((await recentConversions().findAllByRole('listitem'))[0])
    expect(entry.getByRole('img', { name: 'Berlin Clock showing 11:37:01' })).toBeInTheDocument()
    expect(entry.getByText('ORROOROOOYYRYYRYOOOOYYOO')).toBeInTheDocument()
    expect(entry.getByRole('button', { name: 'Copy code for 11:37:01' })).toBeInTheDocument()
  })

  it('shows when each conversion was made', async () => {
    givenTheApiHasHistory(conversion(1, '00:00:00'))
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

describe('Revisiting a conversion', () => {
  it('opens a conversion from the history in the result panel, without converting it again', async () => {
    const user = userEvent.setup()
    givenTheApiHasHistory(conversion(2, '23:59:59'), conversion(1, '00:00:00'))
    render(<App />)

    await user.click(await recentConversions().findByRole('link', { name: '23:59:59' }))

    const result = within(await screen.findByRole('region', { name: 'Result' }))
    expect(result.getByRole('img', { name: 'Berlin Clock showing 23:59:59' })).toBeInTheDocument()
    expect(result.getByText('ORRRRRRROYYRYYRYYRYYYYYY')).toBeInTheDocument()
    expect(window.location.pathname).toBe('/conversions/2')
    expect(conversions).toBe(0)
    expect(recentConversions().getAllByRole('listitem')).toHaveLength(2)
  })

  it('links each history entry to its own address', async () => {
    givenTheApiHasHistory(conversion(2, '23:59:59'), conversion(1, '00:00:00'))
    render(<App />)

    expect(await recentConversions().findByRole('link', { name: '23:59:59' })).toHaveAttribute('href', '/conversions/2')
    expect(recentConversions().getByRole('link', { name: '00:00:00' })).toHaveAttribute('href', '/conversions/1')
  })

  it('marks the open conversion in the history', async () => {
    const user = userEvent.setup()
    givenTheApiHasHistory(conversion(2, '23:59:59'), conversion(1, '00:00:00'))
    render(<App />)

    await user.click(await recentConversions().findByRole('link', { name: '00:00:00' }))

    await screen.findByRole('region', { name: 'Result' })
    expect(recentConversions().getByRole('link', { name: '00:00:00' })).toHaveAttribute('aria-current', 'page')
    expect(recentConversions().getByRole('link', { name: '23:59:59' })).not.toHaveAttribute('aria-current')
  })

  it('leaves a click with a modifier key to the browser, to open the conversion in a new tab', async () => {
    const user = userEvent.setup()
    givenTheApiHasHistory(conversion(1, '23:59:59'))
    render(<App />)
    const link = await recentConversions().findByRole('link', { name: '23:59:59' })
    link.addEventListener('click', (event) => event.preventDefault())

    await user.keyboard('{Meta>}')
    await user.click(link)
    await user.keyboard('{/Meta}')

    expect(screen.queryByRole('region', { name: 'Result' })).not.toBeInTheDocument()
    expect(window.location.pathname).toBe('/')
  })

  it('opens the conversion in the address bar when the page loads, as from a shared link', async () => {
    givenTheApiHasHistory(conversion(2, '16:50:06'), conversion(1, '11:37:01'))
    givenTheAddressIs('/conversions/1')
    render(<App />)

    const result = within(await screen.findByRole('region', { name: 'Result' }))
    expect(result.getByRole('img', { name: 'Berlin Clock showing 11:37:01' })).toBeInTheDocument()
    expect(result.getByText('ORROOROOOYYRYYRYOOOOYYOO')).toBeInTheDocument()
    expect(conversions).toBe(0)
  })

  it('says so when the conversion does not exist', async () => {
    givenTheApiHasHistory(conversion(1, '00:00:00'))
    givenTheAddressIs('/conversions/999999')
    render(<App />)

    const result = within(await screen.findByRole('region', { name: 'Result' }))
    expect(await result.findByRole('alert')).toHaveTextContent('Conversion 999999 not found')
    expect(result.queryByRole('img')).not.toBeInTheDocument()
  })

  it('puts a converted time in the address bar, so the result can be shared', async () => {
    givenTheApiHasHistory(conversion(1, '00:00:00'))
    render(<App />)

    await userEvent.type(screen.getByLabelText('Time (HH:mm:ss)'), '16:50:06{Enter}')

    await screen.findByRole('region', { name: 'Result' })
    expect(window.location.pathname).toBe('/conversions/2')
  })

  it('goes back to the previously opened conversion with the browser Back button', async () => {
    const user = userEvent.setup()
    givenTheApiHasHistory(conversion(2, '23:59:59'), conversion(1, '00:00:00'))
    render(<App />)
    await user.click(await recentConversions().findByRole('link', { name: '00:00:00' }))
    await within(await screen.findByRole('region', { name: 'Result' })).findByText('YOOOOOOOOOOOOOOOOOOOOOOO')
    await user.click(recentConversions().getByRole('link', { name: '23:59:59' }))
    await within(screen.getByRole('region', { name: 'Result' })).findByText('ORRRRRRROYYRYYRYYRYYYYYY')

    window.history.back()

    const result = within(screen.getByRole('region', { name: 'Result' }))
    expect(await result.findByRole('img', { name: 'Berlin Clock showing 00:00:00' })).toBeInTheDocument()
    expect(window.location.pathname).toBe('/conversions/1')
  })

  it('closes the result when going back to the start page', async () => {
    const user = userEvent.setup()
    givenTheApiHasHistory(conversion(1, '00:00:00'))
    render(<App />)
    await user.click(await recentConversions().findByRole('link', { name: '00:00:00' }))
    await screen.findByRole('region', { name: 'Result' })

    window.history.back()

    await waitForElementToBeRemoved(() => screen.queryByRole('region', { name: 'Result' }))
    expect(window.location.pathname).toBe('/')
  })
})

describe('Clearing the history', () => {
  const QUESTION = 'Delete all conversions? This cannot be undone.'

  it('asks for confirmation inside the page', async () => {
    const user = userEvent.setup()
    givenTheApiHasHistory(conversion(1, '00:00:00'))
    render(<App />)

    await user.click(await recentConversions().findByRole('button', { name: 'Clear history' }))

    const confirmation = within(recentConversions().getByRole('group', { name: QUESTION }))
    expect(confirmation.getByText(QUESTION)).toBeInTheDocument()
    expect(confirmation.getByRole('button', { name: 'Delete' })).toBeInTheDocument()
    expect(confirmation.getByRole('button', { name: 'Cancel' })).toHaveFocus()
    expect(clears).toBe(0)
  })

  it('empties the history when confirmed, also after a refresh', async () => {
    const user = userEvent.setup()
    givenTheApiHasHistory(conversion(2, '23:59:59'), conversion(1, '00:00:00'))
    const { unmount } = render(<App />)
    await user.click(await recentConversions().findByRole('button', { name: 'Clear history' }))

    await user.click(recentConversions().getByRole('button', { name: 'Delete' }))

    expect(await recentConversions().findByText('No conversions yet')).toBeInTheDocument()
    expect(recentConversions().queryByRole('listitem')).not.toBeInTheDocument()
    expect(recentConversions().queryByRole('group', { name: QUESTION })).not.toBeInTheDocument()
    expect(clears).toBe(1)

    unmount()
    render(<App />)
    expect(await recentConversions().findByText('No conversions yet')).toBeInTheDocument()
  })

  it('keeps everything when cancelled', async () => {
    const user = userEvent.setup()
    givenTheApiHasHistory(conversion(2, '23:59:59'), conversion(1, '00:00:00'))
    render(<App />)
    const clear = await recentConversions().findByRole('button', { name: 'Clear history' })
    await user.click(clear)

    await user.click(recentConversions().getByRole('button', { name: 'Cancel' }))

    expect(recentConversions().queryByRole('group', { name: QUESTION })).not.toBeInTheDocument()
    expect(recentConversions().getAllByRole('listitem')).toHaveLength(2)
    expect(clear).toHaveFocus()
    expect(clears).toBe(0)
  })

  it('cannot clear an empty history', async () => {
    givenTheApiHasHistory()
    render(<App />)

    await recentConversions().findByText('No conversions yet')
    expect(recentConversions().getByRole('button', { name: 'Clear history' })).toBeDisabled()
  })

  it('closes an opened conversion and returns to the home page, without a new Back step', async () => {
    const user = userEvent.setup()
    givenTheApiHasHistory(conversion(1, '00:00:00'))
    givenTheAddressIs('/conversions/1')
    render(<App />)
    await within(await screen.findByRole('region', { name: 'Result' })).findByText('YOOOOOOOOOOOOOOOOOOOOOOO')
    const steps = window.history.length

    await user.click(recentConversions().getByRole('button', { name: 'Clear history' }))
    await user.click(recentConversions().getByRole('button', { name: 'Delete' }))

    await recentConversions().findByText('No conversions yet')
    expect(screen.queryByRole('region', { name: 'Result' })).not.toBeInTheDocument()
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    expect(window.location.pathname).toBe('/')
    expect(window.history.length).toBe(steps)
  })

  it('still says not found when a cleared conversion is opened from its old link', async () => {
    const user = userEvent.setup()
    givenTheApiHasHistory(conversion(1, '00:00:00'))
    render(<App />)
    await recentConversions().findByText('00:00:00')
    await user.click(recentConversions().getByRole('button', { name: 'Clear history' }))
    await user.click(recentConversions().getByRole('button', { name: 'Delete' }))
    await recentConversions().findByText('No conversions yet')

    act(() => {
      window.history.pushState(null, '', '/conversions/1')
      window.dispatchEvent(new PopStateEvent('popstate'))
    })

    const result = within(await screen.findByRole('region', { name: 'Result' }))
    expect(await result.findByRole('alert')).toHaveTextContent('Conversion 1 not found')
  })
})

describe('Invalid times', () => {
  const INVALID = "Invalid time '25:00:00': expected HH:mm:ss between 00:00:00 and 23:59:59"

  it('shows the reason next to the input and clears the old result', async () => {
    givenTheApiHasHistory()
    givenTheApiRejects('25:00:00', INVALID)
    render(<App />)
    const input = screen.getByLabelText('Time (HH:mm:ss)')
    await userEvent.type(input, '00:00:00{Enter}')
    await screen.findByRole('region', { name: 'Result' })

    await userEvent.clear(input)
    await userEvent.type(input, '25:00:00{Enter}')

    expect(await screen.findByRole('alert')).toHaveTextContent(INVALID)
    expect(input).toHaveAttribute('aria-invalid', 'true')
    expect(input).toHaveAccessibleDescription(INVALID)
    expect(screen.queryByRole('region', { name: 'Result' })).not.toBeInTheDocument()
    expect(window.location.pathname).toBe('/')
  })

  it('leaves the recent conversions unchanged', async () => {
    givenTheApiHasHistory(conversion(1, '00:00:00'))
    givenTheApiRejects('12-00-00', "Invalid time '12-00-00': expected HH:mm:ss between 00:00:00 and 23:59:59")
    render(<App />)
    await recentConversions().findByText('00:00:00')

    await userEvent.type(screen.getByLabelText('Time (HH:mm:ss)'), '12-00-00{Enter}')

    await screen.findByRole('alert')
    expect(recentConversions().getAllByRole('listitem')).toHaveLength(1)
  })

  it('asks for a time when the field is empty', async () => {
    givenTheApiHasHistory()
    givenTheApiRejects('', 'A time is required (HH:mm:ss)')
    render(<App />)

    await userEvent.click(screen.getByRole('button', { name: 'Convert' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('A time is required (HH:mm:ss)')
  })

  it('clears the message once a valid time is converted', async () => {
    givenTheApiHasHistory()
    givenTheApiRejects('25:00:00', INVALID)
    render(<App />)
    const input = screen.getByLabelText('Time (HH:mm:ss)')
    await userEvent.type(input, '25:00:00{Enter}')
    await screen.findByRole('alert')

    await userEvent.clear(input)
    await userEvent.type(input, '00:00:00{Enter}')

    await screen.findByRole('region', { name: 'Result' })
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    expect(input).not.toHaveAttribute('aria-invalid')
  })
})

describe('The live clock', () => {
  beforeEach(() => {
    vi.useFakeTimers({ toFake: ['Date', 'setTimeout', 'clearTimeout', 'setInterval', 'clearInterval'] })
    vi.setSystemTime(new Date(2026, 9, 6, 16, 50, 6))
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('is shown above the converter', async () => {
    givenTheApiHasHistory()
    render(<App />)
    await act(() => vi.advanceTimersByTimeAsync(0))

    const live = screen.getByRole('region', { name: 'Live' })
    expect(within(live).getByRole('img', { name: 'Berlin Clock showing 16:50:06' })).toBeInTheDocument()
    const converter = screen.getByRole('region', { name: 'Convert a time' })
    expect(live.compareDocumentPosition(converter) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy()
  })

  it('does not save its ticks in the history', async () => {
    givenTheApiHasHistory(conversion(1, '00:00:00'))
    render(<App />)
    await act(() => vi.advanceTimersByTimeAsync(0))

    await act(() => vi.advanceTimersByTimeAsync(60_000))

    expect(ticks).toBe(61)
    expect(conversions).toBe(0)
    expect(within(screen.getByRole('region', { name: 'Live' })).getByText('16:51:06')).toBeInTheDocument()
    expect(recentConversions().getAllByRole('listitem')).toHaveLength(1)
  })
})

describe('When the database is down', () => {
  const NOT_SAVED = 'History is temporarily unavailable. Your conversion was not saved.'

  it('says a conversion was not saved, without blaming the time typed', async () => {
    givenTheApiHasHistory(conversion(1, '00:00:00'))
    render(<App />)
    await recentConversions().findByText('00:00:00')
    givenTheDatabaseIsDown()

    const input = screen.getByLabelText('Time (HH:mm:ss)')
    await userEvent.type(input, '12:00:00{Enter}')

    expect(await screen.findByRole('alert')).toHaveTextContent(NOT_SAVED)
    expect(input).not.toHaveAttribute('aria-invalid')
    expect(screen.queryByRole('region', { name: 'Result' })).not.toBeInTheDocument()
    expect(window.location.pathname).toBe('/')
  })

  it('shows "History unavailable" instead of the recent conversions', async () => {
    givenTheApiHasHistory(conversion(1, '00:00:00'))
    render(<App />)
    await recentConversions().findByText('00:00:00')
    givenTheDatabaseIsDown()

    await userEvent.type(screen.getByLabelText('Time (HH:mm:ss)'), '12:00:00{Enter}')

    await recentConversions().findByText('History unavailable')
    expect(recentConversions().queryByRole('listitem')).not.toBeInTheDocument()
    expect(recentConversions().queryByText('No conversions yet')).not.toBeInTheDocument()
    expect(recentConversions().getByRole('button', { name: 'Clear history' })).toBeDisabled()
  })

  it('shows "History unavailable" when the page opens', async () => {
    givenTheApiHasHistory(conversion(1, '00:00:00'))
    givenTheDatabaseIsDown()
    render(<App />)

    expect(await recentConversions().findByText('History unavailable')).toBeInTheDocument()
  })

  it('shows "History unavailable" when clearing fails, rather than an empty history', async () => {
    const user = userEvent.setup()
    givenTheApiHasHistory(conversion(1, '00:00:00'))
    render(<App />)
    await recentConversions().findByText('00:00:00')
    givenTheDatabaseIsDown()

    await user.click(recentConversions().getByRole('button', { name: 'Clear history' }))
    await user.click(recentConversions().getByRole('button', { name: 'Delete' }))

    expect(await recentConversions().findByText('History unavailable')).toBeInTheDocument()
    expect(recentConversions().queryByText('No conversions yet')).not.toBeInTheDocument()
  })

  it('keeps the live clock ticking, without a banner', async () => {
    givenTheApiHasHistory()
    givenTheDatabaseIsDown()
    render(<App />)

    const live = within(screen.getByRole('region', { name: 'Live' }))
    expect(await live.findByRole('img', { name: /^Berlin Clock showing/ })).toBeInTheDocument()
    expect(screen.queryByText('Backend unavailable. Retrying…')).not.toBeInTheDocument()
  })

  it('saves conversions again once it is back, and the history returns', async () => {
    givenTheApiHasHistory(conversion(1, '00:00:00'))
    givenTheDatabaseIsDown()
    render(<App />)
    const input = screen.getByLabelText('Time (HH:mm:ss)')
    await userEvent.type(input, '12:00:00{Enter}')
    await screen.findByRole('alert')

    givenEverythingIsBackUp()
    await userEvent.click(screen.getByRole('button', { name: 'Convert' }))

    await screen.findByRole('region', { name: 'Result' })
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    await recentConversions().findByText('12:00:00')
    expect(recentConversions().queryByText('History unavailable')).not.toBeInTheDocument()
    expect(recentConversions().getAllByRole('listitem')).toHaveLength(2)
  })
})

describe('When the backend is down', () => {
  const BANNER = 'Backend unavailable. Retrying…'

  it('shows a banner', async () => {
    givenTheApiHasHistory()
    givenTheBackendIsDown()
    render(<App />)

    expect(await screen.findByText(BANNER)).toBeInTheDocument()
  })

  it('says a conversion could not be made', async () => {
    givenTheApiHasHistory()
    render(<App />)
    await recentConversions().findByText('No conversions yet')
    givenTheBackendIsDown()

    await userEvent.type(screen.getByLabelText('Time (HH:mm:ss)'), '12:00:00{Enter}')

    expect(await screen.findByText('Backend unavailable. Your conversion was not saved.')).toBeInTheDocument()
    expect(screen.queryByRole('region', { name: 'Result' })).not.toBeInTheDocument()
  })

  it('removes the banner once the backend answers again', async () => {
    givenTheApiHasHistory()
    givenTheBackendIsDown()
    render(<App />)
    await screen.findByText(BANNER)

    givenEverythingIsBackUp()

    await waitForElementToBeRemoved(() => screen.queryByText(BANNER), { timeout: 3000 })
  })
})
