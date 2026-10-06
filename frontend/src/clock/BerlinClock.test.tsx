import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { BerlinClock } from './BerlinClock'
import type { BerlinClockRows } from './types'

const clock = (rows: Partial<BerlinClockRows>): BerlinClockRows => ({
  seconds: 'Y',
  fiveHours: 'OOOO',
  singleHours: 'OOOO',
  fiveMinutes: 'OOOOOOOOOOO',
  singleMinutes: 'OOOO',
  clock: 'YOOOOOOOOOOOOOOOOOOOOOOO',
  ...rows,
})

const lampsOf = (row: HTMLElement) => Array.from(row.children, (lamp) => lamp.getAttribute('data-lamp')).join('')

describe('BerlinClock', () => {
  it('lights the seconds lamp yellow on an even second', () => {
    render(<BerlinClock time="00:00:00" clock={clock({ seconds: 'Y' })} />)

    expect(screen.getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'Y')
  })

  it('turns the seconds lamp off on an odd second', () => {
    render(<BerlinClock time="00:00:00" clock={clock({ seconds: 'O' })} />)

    expect(screen.getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'O')
  })

  it('shows the five hours row', () => {
    render(<BerlinClock time="00:00:00" clock={clock({ fiveHours: 'RRRO' })} />)

    expect(lampsOf(screen.getByTestId('five-hours-row'))).toBe('RRRO')
  })

  it('shows the single hours row', () => {
    render(<BerlinClock time="00:00:00" clock={clock({ singleHours: 'RRRR' })} />)

    expect(lampsOf(screen.getByTestId('single-hours-row'))).toBe('RRRR')
  })

  it('shows the five minutes row, eleven lamps with the quarter markers red when lit', () => {
    render(<BerlinClock time="00:00:00" clock={clock({ fiveMinutes: 'YYRYYRYYRYY' })} />)

    expect(lampsOf(screen.getByTestId('five-minutes-row'))).toBe('YYRYYRYYRYY')
  })

  it('shows the single minutes row', () => {
    render(<BerlinClock time="00:00:00" clock={clock({ singleMinutes: 'YYYY' })} />)

    expect(lampsOf(screen.getByTestId('single-minutes-row'))).toBe('YYYY')
  })

  it('is exposed as an image labelled with the time it shows, so it reads without seeing colours', () => {
    render(<BerlinClock time="16:50:06" clock={clock({ fiveHours: 'RRRO' })} />)

    expect(screen.getByRole('img', { name: 'Berlin Clock showing 16:50:06' })).toBeInTheDocument()
  })
})
