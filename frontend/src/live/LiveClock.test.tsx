import { act, render, screen, within } from '@testing-library/react'
import { http, HttpResponse } from 'msw'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import type { BerlinClockRows } from '../clock/types'
import { server } from '../test/server'
import { LiveClock } from './LiveClock'

/** What the real API answers for the times these tests tick through (the rules live in the backend). */
const BERLIN_CLOCK: Record<string, BerlinClockRows> = {
  '16:50:06': {
    seconds: 'Y', fiveHours: 'RRRO', singleHours: 'ROOO', fiveMinutes: 'YYRYYRYYRYO', singleMinutes: 'OOOO',
    clock: 'YRRROROOOYYRYYRYYRYOOOOO',
  },
  '16:50:07': {
    seconds: 'O', fiveHours: 'RRRO', singleHours: 'ROOO', fiveMinutes: 'YYRYYRYYRYO', singleMinutes: 'OOOO',
    clock: 'ORRROROOOYYRYYRYYRYOOOOO',
  },
  '16:50:08': {
    seconds: 'Y', fiveHours: 'RRRO', singleHours: 'ROOO', fiveMinutes: 'YYRYYRYYRYO', singleMinutes: 'OOOO',
    clock: 'YRRROROOOYYRYYRYYRYOOOOO',
  },
}

/** The times the fake API was asked for, in order. */
let asked: string[] = []

/** Times whose answer is held back until the test releases it. */
const held = new Map<string, () => void>()

/** How the fake backend fails, if it does. */
let down: 'network' | 'server error' | undefined

function givenTheApiAnswers() {
  asked = []
  held.clear()
  down = undefined
  server.use(
    http.get('/api/berlin-clock', async ({ request }) => {
      const time = new URL(request.url).searchParams.get('time')!
      asked.push(time)
      if (held.has(time)) {
        await new Promise<void>((release) => held.set(time, release))
      }
      if (down === 'network') return HttpResponse.error()
      if (down === 'server error') return new HttpResponse('Bad Gateway', { status: 502 })
      return HttpResponse.json({ time, ...BERLIN_CLOCK[time] })
    }),
  )
}

/** The answer for `time` waits until {@link release} is called. */
function holdTheAnswerFor(time: string) {
  held.set(time, () => {})
}

async function release(time: string) {
  await act(async () => {
    held.get(time)!()
    await vi.advanceTimersByTimeAsync(0)
  })
}

/** Lets the clock move on, and any answer that is ready arrive. */
async function passes(ms: number) {
  await act(() => vi.advanceTimersByTimeAsync(ms))
}

function liveClock() {
  return within(screen.getByRole('region', { name: 'Live' }))
}

const BANNER = 'Backend unavailable. Retrying…'

beforeEach(() => {
  // Only the clock and timers are faked: fetch and promises run for real, so MSW answers as usual.
  vi.useFakeTimers({ toFake: ['Date', 'setTimeout', 'clearTimeout', 'setInterval', 'clearInterval'] })
  vi.setSystemTime(new Date(2026, 9, 6, 16, 50, 6))
  givenTheApiAnswers()
})

afterEach(() => {
  vi.useRealTimers()
})

describe('The live clock', () => {
  it("shows the browser's local time as a Berlin Clock", async () => {
    render(<LiveClock />)
    await passes(0)

    expect(asked).toEqual(['16:50:06'])
    expect(liveClock().getByRole('img', { name: 'Berlin Clock showing 16:50:06' })).toBeInTheDocument()
    expect(liveClock().getByText('16:50:06')).toBeInTheDocument()
    expect(liveClock().getByText('YRRROROOOYYRYYRYYRYOOOOO')).toBeInTheDocument()
    expect(liveClock().getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'Y')
  })

  it('ticks every second, and the seconds lamp blinks', async () => {
    render(<LiveClock />)
    await passes(0)

    await passes(1000)

    expect(asked).toEqual(['16:50:06', '16:50:07'])
    expect(liveClock().getByText('16:50:07')).toBeInTheDocument()
    expect(liveClock().getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'O')
  })

  it('ticks on the second, not a second after the page opened', async () => {
    vi.setSystemTime(new Date(2026, 9, 6, 16, 50, 6, 700))
    render(<LiveClock />)
    await passes(0)

    await passes(300)

    expect(asked).toEqual(['16:50:06', '16:50:07'])
  })

  it('never moves backwards when an answer arrives late', async () => {
    holdTheAnswerFor('16:50:07')
    render(<LiveClock />)
    await passes(0)

    await passes(1000)
    await passes(1000)
    expect(liveClock().getByText('16:50:08')).toBeInTheDocument()

    await release('16:50:07')

    expect(liveClock().getByText('16:50:08')).toBeInTheDocument()
    expect(liveClock().queryByText('16:50:07')).not.toBeInTheDocument()
  })

  it('stops asking once it is gone', async () => {
    const { unmount } = render(<LiveClock />)
    await passes(0)

    unmount()
    await passes(5000)

    expect(asked).toEqual(['16:50:06'])
    expect(vi.getTimerCount()).toBe(0)
  })
})

describe('The live clock when the backend is unavailable', () => {
  it.each(['network', 'server error'] as const)(
    'says so after a %s, and keeps the last known time, dimmed',
    async (failure) => {
      render(<LiveClock />)
      await passes(0)

      down = failure
      await passes(1000)

      expect(screen.getByRole('alert')).toHaveTextContent(BANNER)
      expect(liveClock().getByText('16:50:06')).toBeInTheDocument()
      expect(screen.getByRole('region', { name: 'Live' })).toHaveClass('live--stale')
    },
  )

  it('keeps retrying every second, and recovers on its own', async () => {
    render(<LiveClock />)
    await passes(0)
    down = 'network'
    await passes(1000)

    down = undefined
    await passes(1000)

    expect(asked).toEqual(['16:50:06', '16:50:07', '16:50:08'])
    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    expect(liveClock().getByText('16:50:08')).toBeInTheDocument()
    expect(screen.getByRole('region', { name: 'Live' })).not.toHaveClass('live--stale')
  })

  it('is not taken for down by a failure older than the answer shown', async () => {
    holdTheAnswerFor('16:50:07')
    render(<LiveClock />)
    await passes(0)
    await passes(1000)
    await passes(1000)

    down = 'network'
    await release('16:50:07')

    expect(screen.queryByRole('alert')).not.toBeInTheDocument()
    expect(liveClock().getByText('16:50:08')).toBeInTheDocument()
  })
})
