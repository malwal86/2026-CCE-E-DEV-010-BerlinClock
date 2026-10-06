import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { BerlinClock } from './BerlinClock'
import type { BerlinClockRows } from './types'

const clock = (rows: Partial<BerlinClockRows>): BerlinClockRows => ({
  seconds: 'Y',
  fiveHours: 'OOOO',
  singleHours: 'OOOO',
  fiveMinutes: 'OOOOOOOOOOO',
  ...rows,
})

const lampsOf = (row: HTMLElement) => Array.from(row.children, (lamp) => lamp.getAttribute('data-lamp')).join('')

describe('BerlinClock', () => {
  it('lights the seconds lamp yellow on an even second', () => {
    render(<BerlinClock clock={clock({ seconds: 'Y' })} />)

    expect(screen.getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'Y')
  })

  it('turns the seconds lamp off on an odd second', () => {
    render(<BerlinClock clock={clock({ seconds: 'O' })} />)

    expect(screen.getByTestId('seconds-lamp')).toHaveAttribute('data-lamp', 'O')
  })

  it('shows the five hours row', () => {
    render(<BerlinClock clock={clock({ fiveHours: 'RRRO' })} />)

    expect(lampsOf(screen.getByTestId('five-hours-row'))).toBe('RRRO')
  })

  it('shows the single hours row', () => {
    render(<BerlinClock clock={clock({ singleHours: 'RRRR' })} />)

    expect(lampsOf(screen.getByTestId('single-hours-row'))).toBe('RRRR')
  })

  it('shows the five minutes row, eleven lamps with the quarter markers red when lit', () => {
    render(<BerlinClock clock={clock({ fiveMinutes: 'YYRYYRYYRYY' })} />)

    expect(lampsOf(screen.getByTestId('five-minutes-row'))).toBe('YYRYYRYYRYY')
  })
})
